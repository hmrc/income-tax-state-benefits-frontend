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

package support.mocks

import actions.ActionsProvider
import models.requests.{AuthorisationRequest, UserPriorDataRequest, UserSessionDataRequest}
import models.{BenefitType, IncomeTaxUserData, StateBenefitsUserData}
import org.scalamock.handlers.{CallHandler1, CallHandler2, CallHandler3}
import org.scalamock.scalatest.MockFactory
import play.api.mvc._
import support.builders.UserBuilder.aUser

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait MockActionsProvider extends MockFactory
  with MockAuthorisedAction
  with MockErrorHandler {

  protected val mockActionsProvider: ActionsProvider = mock[ActionsProvider]

  def mockPriorDataFor(taxYear: Int,
                       result: IncomeTaxUserData): CallHandler1[Int, ActionBuilder[UserPriorDataRequest, AnyContent]] = {
    (mockActionsProvider.priorDataFor(_: Int))
      .expects(taxYear)
      .returns(value = userPriorDataRequestActionBuilder(result))
  }

  def mockPriorDataWithViewStateBenefitsAudit(taxYear: Int,
                                          benefitType: BenefitType,
                                          result: IncomeTaxUserData): CallHandler2[Int, BenefitType, ActionBuilder[UserPriorDataRequest, AnyContent]] = {
    (mockActionsProvider.priorDataWithViewStateBenefitsAudit(_: Int, _: BenefitType))
      .expects(taxYear, benefitType)
      .returns(value = userPriorDataRequestActionBuilder(result))
  }

  def mockEndOfYearSessionDataFor(taxYear: Int,
                                  benefitType: BenefitType,
                                  sessionDataId: UUID,
                                  result: StateBenefitsUserData): CallHandler3[Int, BenefitType, UUID, ActionBuilder[UserSessionDataRequest, AnyContent]] = {
    (mockActionsProvider.endOfYearSessionDataFor(_: Int, _: BenefitType, _: UUID))
      .expects(taxYear, benefitType, sessionDataId)
      .returns(value = userSessionDataRequestActionBuilder(result))
  }

  def mockSessionDataFor(taxYear: Int,
                         benefitType: BenefitType,
                         sessionDataId: UUID,
                         result: StateBenefitsUserData): CallHandler3[Int, BenefitType, UUID, ActionBuilder[UserSessionDataRequest, AnyContent]] = {
    (mockActionsProvider.reviewClaimSessionDataFor(_: Int, _: BenefitType, _: UUID))
      .expects(taxYear, benefitType, sessionDataId)
      .returns(value = userSessionDataRequestActionBuilder(result))
  }

  def mockEndOfYear(taxYear: Int): CallHandler1[Int, ActionBuilder[AuthorisationRequest, AnyContent]] = {
    (mockActionsProvider.endOfYear(_: Int))
      .expects(taxYear)
      .returns(value = authorisationRequestActionBuilder)
  }

  private def authorisationRequestActionBuilder: ActionBuilder[AuthorisationRequest, AnyContent] =
    new ActionBuilder[AuthorisationRequest, AnyContent] {
      override def parser: BodyParser[AnyContent] = BodyParser("anyContent")(_ => throw new NotImplementedError)

      override def invokeBlock[A](request: Request[A], block: AuthorisationRequest[A] => Future[Result]): Future[Result] =
        block(AuthorisationRequest(aUser, request))

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }


  private def userSessionDataRequestActionBuilder(result: StateBenefitsUserData): ActionBuilder[UserSessionDataRequest, AnyContent] =
    new ActionBuilder[UserSessionDataRequest, AnyContent] {
      override def parser: BodyParser[AnyContent] = BodyParser("anyContent")(_ => throw new NotImplementedError)

      override def invokeBlock[A](request: Request[A], block: UserSessionDataRequest[A] => Future[Result]): Future[Result] =
        block(UserSessionDataRequest(result, aUser, request))

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }


  private def userPriorDataRequestActionBuilder(result: IncomeTaxUserData): ActionBuilder[UserPriorDataRequest, AnyContent] =
    new ActionBuilder[UserPriorDataRequest, AnyContent] {
      override def parser: BodyParser[AnyContent] = BodyParser("anyContent")(_ => throw new NotImplementedError)

      override def invokeBlock[A](request: Request[A], block: UserPriorDataRequest[A] => Future[Result]): Future[Result] =
        block(UserPriorDataRequest(result, aUser, request))

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }
}
