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
import models.authorisation.Enrolment.{Agent, Individual, Nino, SupportingAgent}
import models.authorisation.{DelegatedAuthRules, SessionValues}
import models.requests.AuthorisationRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import services.AuthorisationService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(authService: AuthorisationService,
                                 appConfig: AppConfig,
                                 cc: ControllerComponents,
                                 errorHandler: ErrorHandler)
                                (implicit ec: ExecutionContext)
  extends ActionBuilder[AuthorisationRequest, AnyContent] with Logging {

  private val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level

  override protected[actions] def executionContext: ExecutionContext = ec

  override def parser: BodyParser[AnyContent] = cc.parsers.default

  private lazy val signInRedirectFutureResult = Future.successful(Redirect(appConfig.signInUrl))
  private lazy val agentErrorRedirectResult: Result = Redirect(AgentAuthErrorController.show)

  override def invokeBlock[A](request: Request[A], block: AuthorisationRequest[A] => Future[Result]): Future[Result] = {
    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    authService.authorised().retrieve(affinityGroup) {
      case Some(AffinityGroup.Agent) => agentAuthentication(block)(request, headerCarrier)
      case Some(affinityGroup) => individualAuthentication(block, affinityGroup)(request, headerCarrier)
      case _ => Future.successful(redirectToUnauthorisedUserErrorPage())
    } recover {
      case _: NoActiveSession => redirectToSignInPage()
      case _: AuthorisationException => redirectToUnauthorisedUserErrorPage()
      case e =>
        logger.error(s"[AuthorisedAction][invokeBlock] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
        InternalServerError
    }
  }

  private def sessionIdBlock(errorLogString: String, errorAction: Future[Result])
                            (block: String => Future[Result])
                            (implicit request: Request[_], hc: HeaderCarrier): Future[Result] =
    hc.sessionId match {
      case Some(sessionId) => block(sessionId.value)
      case _ => request.headers.get(SessionKeys.sessionId) match {
        case Some(sessionId) => block(sessionId)
        case _ =>
          logger.info(errorLogString)
          errorAction
      }
    }

  private[actions] def individualAuthentication[A](block: AuthorisationRequest[A] => Future[Result], affinityGroup: AffinityGroup)
                                                  (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authService.authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        val optionalMtdItId: Option[String] = enrolmentGetIdentifierValue(Individual.key, Individual.value, enrolments)
        val optionalNino: Option[String] = enrolmentGetIdentifierValue(Nino.key, Nino.value, enrolments)

        (optionalMtdItId, optionalNino) match {
          case (Some(mtdItId), Some(nino)) =>
            sessionIdBlock(
              errorLogString = "[AuthorisedAction][individualAuthentication] - No session id in request",
              errorAction = signInRedirectFutureResult
            )(sessionId => block(AuthorisationRequest(
              models.User(mtdItId, None, nino, sessionId, affinityGroup.toString),
              request
            )))
          case (_, None) =>
            val logMessage = s"[AuthorisedAction][individualAuthentication] - No active session. Redirecting to ${appConfig.signInUrl}"
            logger.info(logMessage)
            Future.successful(Redirect(appConfig.signInUrl))
          case (None, _) =>
            val logMessage = s"[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment. Redirecting user to sign up for MTD."
            logger.info(logMessage)
            Future.successful(Redirect(IndividualAuthErrorController.show))
        }
      case _ =>
        val logMessage = "[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift."
        logger.info(logMessage)
        Future(Redirect(appConfig.incomeTaxSubmissionIvRedirect))
    }
  }

  private def agentAuthPredicate(mtdId: String): Predicate =
    Enrolment(Individual.key)
      .withIdentifier(Individual.value, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.agentDelegatedAuthRule)

  private def secondaryAgentPredicate(mtdId: String): Predicate =
    Enrolment(SupportingAgent.key)
      .withIdentifier(SupportingAgent.value, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.supportingAgentDelegatedAuthRule)

  private val agentAuthLogString: String = "[AuthorisedAction][agentAuthentication]"

  private def agentRecovery[A](block: AuthorisationRequest[A] => Future[Result], mtdItId: String, nino: String)
                              (implicit request: Request[A], hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      logger.info(s"$agentAuthLogString - No active session. Redirecting to ${appConfig.signInUrl}")
      signInRedirectFutureResult
    case _: AuthorisationException =>
      authService.authorised(secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments)(
          enrolments => handleForValidAgent(block, mtdItId, nino, enrolments, isSupportingAgent = true)
        )
        .recover {
          case _: AuthorisationException =>
            logger.warn(s"$agentAuthLogString - Agent does not have delegated authority for Client.")
            agentErrorRedirectResult
          case e =>
            logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
            errorHandler.internalServerError()
        }
    case e =>
      logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      Future.successful(errorHandler.internalServerError())
  }

  private def handleForValidAgent[A](block: AuthorisationRequest[A] => Future[Result],
                                     mtdItId: String,
                                     nino: String,
                                     enrolments: Enrolments,
                                     isSupportingAgent: Boolean)
                                    (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    if (isSupportingAgent) {
      logger.warn(s"$agentAuthLogString - Secondary agent unauthorised")
      Future.successful(Redirect(controllers.errors.routes.SupportingAgentAuthErrorController.show))
    } else {
      enrolmentGetIdentifierValue(Agent.key, Agent.value, enrolments) match {
        case Some(arn) => sessionIdBlock(
          errorLogString = s"$agentAuthLogString - No session id in request",
          errorAction = signInRedirectFutureResult
        )(sessionId =>
          block(AuthorisationRequest(
            user = models.User(mtdItId, Some(arn), nino, sessionId, AffinityGroup.Agent.toString, isSupportingAgent),
            request = request
          ))
        )
        case None =>
          logger.info(s"$agentAuthLogString - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
          Future.successful(Redirect(controllers.errors.routes.YouNeedAgentServicesController.show))
      }
    }
  }

  private[actions] def agentAuthentication[A](block: AuthorisationRequest[A] => Future[Result])
                                             (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    val optionalNino = request.session.get(SessionValues.CLIENT_NINO)
    val optionalMtdItId = request.session.get(SessionValues.CLIENT_MTDITID)

    (optionalMtdItId, optionalNino) match {
      case (Some(mtdItId), Some(nino)) =>
        authService
          .authorised(agentAuthPredicate(mtdItId))
          .retrieve(allEnrolments)(
            enrollments => handleForValidAgent(block, mtdItId, nino, enrollments, isSupportingAgent = false)
          )
          .recoverWith(agentRecovery(block, mtdItId, nino))
      case (mtdItId, nino) =>
        logger.info(s"$agentAuthLogString - Agent does not have session key values. " +
          s"Redirecting to view & change. MTDITID missing:${mtdItId.isEmpty}, NINO missing:${nino.isEmpty}")
        Future.successful(Redirect(appConfig.viewAndChangeEnterUtrUrl))
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
    val logMessage = s"[AuthorisedAction][invokeBlock] - User failed to authenticate"
    logger.info(logMessage)
    Redirect(UnauthorisedUserErrorController.show)
  }

  private def redirectToSignInPage(): Result = {
    val logMessage = s"[AuthorisedAction][invokeBlock] - No active session. Redirecting to ${appConfig.signInUrl}"
    logger.info(logMessage)
    Redirect(appConfig.signInUrl)
  }
}
