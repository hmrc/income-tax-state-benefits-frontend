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
import models.requests.{AuthorisationRequest, UserPriorAndSessionDataRequest, UserPriorDataRequest, UserSessionDataRequest}
import models.{BenefitType, IncomeTaxUserData, StateBenefit, StateBenefitsUserData}
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc._
import support.builders.UserBuilder.aUser

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait MockActionsProvider extends MockitoSugar
  with MockAuthorisedAction
  with MockErrorHandler {

  protected val mockActionsProvider: ActionsProvider = mock[ActionsProvider]

  def mockPriorDataFor(taxYear: Int,
                       result: IncomeTaxUserData): Unit =
    when(mockActionsProvider.priorDataFor(eqTo(taxYear)))
      .thenReturn(userPriorDataRequestActionBuilder(result))

  def mockPriorDataWithViewStateBenefitsAudit(taxYear: Int,
                                              benefitType: BenefitType,
                                              result: IncomeTaxUserData): Unit =
    when(mockActionsProvider.priorDataWithViewStateBenefitsAudit(eqTo(taxYear), eqTo(benefitType)))
      .thenReturn(userPriorDataRequestActionBuilder(result))

  def mockEndOfYearSessionDataFor(taxYear: Int,
                                  benefitType: BenefitType,
                                  sessionDataId: UUID,
                                  result: StateBenefitsUserData): Unit =
    when(mockActionsProvider.endOfYearSessionDataFor(eqTo(taxYear), eqTo(benefitType), eqTo(sessionDataId)))
      .thenReturn(userSessionDataRequestActionBuilder(result))

  def mockReviewClaimWithAuditing(taxYear: Int,
                                  benefitType: BenefitType,
                                  sessionDataId: UUID,
                                  result: StateBenefitsUserData,
                                  priorData: Option[StateBenefit] = None): Unit =
    when(mockActionsProvider.reviewClaimWithAuditing(eqTo(taxYear), eqTo(benefitType), eqTo(sessionDataId)))
      .thenReturn(userPriorAndSessionDataRequestActionBuilder(result, priorData))

  def mockReviewClaimSaveAndContinue(taxYear: Int,
                                     benefitType: BenefitType,
                                     sessionDataId: UUID,
                                     result: StateBenefitsUserData): Unit =
    when(mockActionsProvider.reviewClaimSaveAndContinue(eqTo(taxYear), eqTo(benefitType), eqTo(sessionDataId)))
      .thenReturn(userSessionDataRequestActionBuilder(result))

  def mockEndOfYear(taxYear: Int): Unit =
    when(mockActionsProvider.endOfYear(eqTo(taxYear)))
      .thenReturn(authorisationRequestActionBuilder)

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

  private def userPriorAndSessionDataRequestActionBuilder(result: StateBenefitsUserData, priorData: Option[StateBenefit]): ActionBuilder[UserPriorAndSessionDataRequest, AnyContent] =
    new ActionBuilder[UserPriorAndSessionDataRequest, AnyContent] {
      override def parser: BodyParser[AnyContent] = BodyParser("anyContent")(_ => throw new NotImplementedError)

      override def invokeBlock[A](request: Request[A], block: UserPriorAndSessionDataRequest[A] => Future[Result]): Future[Result] =
        block(UserPriorAndSessionDataRequest(result, priorData, aUser, request))

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }
}
