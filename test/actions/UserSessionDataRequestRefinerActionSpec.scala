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

import controllers.routes.ClaimsController
import models.BenefitType
import models.BenefitType.{EmploymentSupportAllowance, JobSeekersAllowance}
import models.errors.HttpParserError
import models.requests.UserSessionDataRequest
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.mvc.Results.{InternalServerError, Redirect}
import support.UnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.builders.requests.AuthorisationRequestBuilder.anAuthorisationRequest
import support.mocks.{MockErrorHandler, MockStateBenefitsService}
import support.providers.TaxYearProvider

import java.util.UUID
import scala.concurrent.ExecutionContext

class UserSessionDataRequestRefinerActionSpec extends UnitTest
  with MockStateBenefitsService
  with MockErrorHandler
  with TaxYearProvider {

  private val executionContext = ExecutionContext.global
  private val sessionDataId: UUID = UUID.randomUUID()

  private def createAction(benefitType: BenefitType) = UserSessionDataRequestRefinerAction(
    taxYear,
    benefitType,
    sessionDataId,
    mockStateBenefitsService,
    mockErrorHandler
  )(executionContext)

  private val underTest = createAction(JobSeekersAllowance)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".refine" should {
    "redirect to ClaimsController when getting session data result in NOT_FOUND" in {
      mockGetUserSessionData(aUser, sessionDataId, Left(HttpParserError(NOT_FOUND)))

      await(underTest.refine(anAuthorisationRequest)) shouldBe Left(Redirect(ClaimsController.show(taxYear, JobSeekersAllowance)))
    }

    "handle InternalServerError when when getting session data result in an error" in {
      mockGetUserSessionData(aUser, sessionDataId, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      await(underTest.refine(anAuthorisationRequest)) shouldBe Left(InternalServerError)
    }

    "redirect to ClaimsController when benefitType from response differs from the URL one" in {
      val underTest = createAction(EmploymentSupportAllowance)

      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData.copy(benefitType = JobSeekersAllowance.typeName)))

      await(underTest.refine(anAuthorisationRequest)) shouldBe Left(Redirect(ClaimsController.show(taxYear, EmploymentSupportAllowance)))
    }

    "return StateBenefitsUserData when the service returns data" in {
      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData))

      await(underTest.refine(anAuthorisationRequest)) shouldBe
        Right(UserSessionDataRequest(aStateBenefitsUserData, anAuthorisationRequest.user, anAuthorisationRequest.request))
    }
  }
}
