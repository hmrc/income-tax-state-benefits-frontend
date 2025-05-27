/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package actions

import config.{AppConfig, ErrorHandler}
import controllers.errors.routes.{AgentAuthErrorController, IndividualAuthErrorController, UnauthorisedUserErrorController}
import models.User
import models.authorisation.Enrolment.{Agent, Individual, Nino}
import models.errors.MissingAgentClientDetails
import models.requests.AuthorisationRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc._
import services.{AuthorisationService, SessionDataService}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.EnrolmentHelper.{agentAuthPredicate, secondaryAgentPredicate}
import utils.{EnrolmentHelper, SessionHelper}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(errorHandler: ErrorHandler,
                                 sessionDataService: SessionDataService)
                                (implicit val authService: AuthorisationService,
                                 val appConfig: AppConfig,
                                 cc: ControllerComponents)
  extends ActionBuilder[AuthorisationRequest, AnyContent] with I18nSupport with SessionHelper {

  implicit val executionContext: ExecutionContext = cc.executionContext
  implicit val messagesApi: MessagesApi = cc.messagesApi

  override def parser: BodyParser[AnyContent] = cc.parsers.default

  private val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level
  private lazy val agentErrorRedirectResult: Result = Redirect(AgentAuthErrorController.show)

  override def invokeBlock[A](request: Request[A], block: AuthorisationRequest[A] => Future[Result]): Future[Result] = {
    implicit val req: Request[A] = request
    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    withSessionId { sessionId =>
      authService.authorised().retrieve(affinityGroup) {
        case Some(AffinityGroup.Agent) => agentAuthentication(block, sessionId)(request, headerCarrier)
        case Some(affinityGroup) => individualAuthentication(block, affinityGroup, sessionId)(request, headerCarrier)
      } recover {
        case _: NoActiveSession => redirectToSignInPage()
        case _: AuthorisationException => redirectToUnauthorisedUserErrorPage()
        case e =>
          logger.error(s"-------------------[AuthorisedAction][invokeBlock] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
          errorHandler.internalServerError()(request)
      }
    }
  }

  private[actions] def individualAuthentication[A](block: AuthorisationRequest[A] => Future[Result], affinityGroup: AffinityGroup, sessionId: String
                                                  )(implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authService.authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        (
          EnrolmentHelper.getEnrolmentValueOpt(Individual.key, Individual.value, enrolments),
          EnrolmentHelper.getEnrolmentValueOpt(Nino.key, Nino.value, enrolments)
        ) match {
          case (Some(mtdItId), Some(nino)) =>
            block(AuthorisationRequest(User(mtdItId, None, nino, sessionId, affinityGroup.toString), request))
          case (_, None) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - No active session. Redirecting to ${appConfig.signInUrl}")
            Future.successful(Redirect(appConfig.signInUrl))
          case (None, _) =>
            val logMessage = s"[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment. Redirecting user to sign up for MTD."
            logger.info(logMessage)
            Future.successful(Redirect(IndividualAuthErrorController.show))
        }
      case _ =>
        logger.info("[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift.")
        Future(Redirect(appConfig.incomeTaxSubmissionIvRedirect))
    }
  }

  private val agentAuthLogString: String = "[AuthorisedAction][agentAuthentication]"

  private def agentRecovery[A](block: AuthorisationRequest[A] => Future[Result],
                               mtdItId: String,
                               nino: String,
                               sessionId: String
                              )(implicit request: Request[A], hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      logger.info(s"$agentAuthLogString - No active session. Redirecting to ${appConfig.signInUrl}")
      Future.successful(redirectToSignInPage())
    case _: AuthorisationException =>
      authService.authorised(secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments)(
          enrolments => handleForValidAgent(block, mtdItId, nino, enrolments, isSupportingAgent = true, sessionId)
        )
        .recover {
          case _: AuthorisationException =>
            logger.warn(s"$agentAuthLogString - Agent does not have delegated authority for Client.")
            agentErrorRedirectResult
          case e =>
            logger.error(s"???????????$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
            errorHandler.internalServerError()
        }
    case e =>
      logger.error(s"111111111111$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      Future.successful(errorHandler.internalServerError())
  }

  private def handleForValidAgent[A](block: AuthorisationRequest[A] => Future[Result],
                                     mtdItId: String,
                                     nino: String,
                                     enrolments: Enrolments,
                                     isSupportingAgent: Boolean,
                                     sessionId: String)
                                    (implicit request: Request[A]): Future[Result] = {
    if (isSupportingAgent) {
      logger.warn(s"$agentAuthLogString - Secondary agent unauthorised")
      Future.successful(Redirect(controllers.errors.routes.SupportingAgentAuthErrorController.show))
    } else {
      EnrolmentHelper.getEnrolmentValueOpt(Agent.key, Agent.value, enrolments) match {
        case Some(arn) =>
          block(AuthorisationRequest(
            user = models.User(mtdItId, Some(arn), nino, sessionId, AffinityGroup.Agent.toString, isSupportingAgent),
            request = request
          ))
        case None =>
          logger.info(s"$agentAuthLogString - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
          Future.successful(Redirect(controllers.errors.routes.YouNeedAgentServicesController.show))
      }
    }
  }

  private[actions] def agentAuthentication[A](block: AuthorisationRequest[A] => Future[Result], sessionId: String)
                                             (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    sessionDataService.getSessionData(sessionId).flatMap { sessionData =>
      authService
        .authorised(agentAuthPredicate(sessionData.mtditid))
        .retrieve(allEnrolments)(
          enrollments => handleForValidAgent(block, sessionData.mtditid, sessionData.nino, enrollments, isSupportingAgent = false, sessionId)
        )
        .recoverWith(agentRecovery(block, sessionData.mtditid, sessionData.nino, sessionId))
    }.recover {
        case _: MissingAgentClientDetails =>
          Redirect(appConfig.viewAndChangeEnterUtrUrl)
      }
  }

  private[actions] def enrolmentGetIdentifierValue(checkedKey: String,
                                                   checkedIdentifier: String,
                                                   enrolments: Enrolments
                                                  ): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
      case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
    }
  }.flatten

  private def redirectToUnauthorisedUserErrorPage(): Result = {
    logger.info(s"[AuthorisedAction][invokeBlock] - User failed to authenticate")
    Redirect(UnauthorisedUserErrorController.show)
  }

  private def redirectToSignInPage(): Result = {
    logger.info(s"[AuthorisedAction][invokeBlock] - No active session. Redirecting to ${appConfig.signInUrl}")
    Redirect(appConfig.signInUrl)
  }
}
