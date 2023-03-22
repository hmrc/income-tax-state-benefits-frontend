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
import models.BenefitType.JobSeekersAllowance
import models.errors.HttpParserError
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results.{InternalServerError, Redirect}
import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.requests.AuthorisationRequestBuilder.anAuthorisationRequest
import support.builders.requests.UserSessionDataRequestBuilder.aUserSessionDataRequest
import support.mocks.{MockErrorHandler, MockStateBenefitsService}
import support.providers.TaxYearProvider

import scala.concurrent.ExecutionContext

class SaveAndContinueFilterActionSpec extends UnitTest
  with TaxYearProvider
  with MockStateBenefitsService
  with MockErrorHandler {

  private val executionContext = ExecutionContext.global
  private val benefitType = JobSeekersAllowance

  private val underTest = SaveAndContinueFilterAction(taxYearEOY, benefitType, mockStateBenefitsService, mockErrorHandler)(executionContext)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".filter" should {
    "handle InternalServerError when is priorSubmission and getting prior data result in an error" in {
      mockGetPriorData(anAuthorisationRequest.user, taxYearEOY, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      await(underTest.filter(aUserSessionDataRequest)) shouldBe Some(InternalServerError)
    }

    "return a Redirect to ClaimsController when is priorSubmission and does not have updates" in {
      mockGetPriorData(anAuthorisationRequest.user, taxYearEOY, Right(anIncomeTaxUserData))

      await(underTest.filter(aUserSessionDataRequest)) shouldBe Some(Redirect(ClaimsController.show(taxYearEOY, JobSeekersAllowance)))
    }

    "return None when is priorSubmission and has updates" in {
      val sessionDataRequest = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(amount = Some(BigDecimal(111))))))
      mockGetPriorData(anAuthorisationRequest.user, taxYearEOY, Right(anIncomeTaxUserData))

      await(underTest.filter(sessionDataRequest)) shouldBe None
    }

    "return None when user data is not a priorSubmission" in {
      val sessionDataRequest = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(claim = None))

      await(underTest.filter(sessionDataRequest)) shouldBe None
    }
  }
}
