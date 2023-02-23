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
import models.BenefitDataType.{CustomerAdded, CustomerOverride, HmrcData}
import models.BenefitType.JobSeekersAllowance
import play.api.mvc.Results.Redirect
import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.requests.UserSessionDataRequestBuilder.aUserSessionDataRequest
import support.providers.TaxYearProvider

import scala.concurrent.ExecutionContext

class ReviewClaimFilterActionSpec extends UnitTest
  with TaxYearProvider {

  private val executionContext = ExecutionContext.global
  private val benefitType = JobSeekersAllowance

  private val underTest = ReviewClaimFilterAction(taxYearEOY, benefitType)(executionContext)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".filter" should {
    "return None" when {
      "when HMRC data" in {
        val sessionData = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(benefitDataType = HmrcData.name))

        await(underTest.filter(sessionData)) shouldBe None
      }

      "when Customer override data" in {
        val sessionData = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(benefitDataType = CustomerOverride.name))

        await(underTest.filter(sessionData)) shouldBe None
      }

      "when Customer added data and is finished" in {
        val sessionData = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(benefitDataType = CustomerAdded.name))

        await(underTest.filter(sessionData)) shouldBe None
      }
    }

    "return a Redirect to ClaimsController" when {
      "when customer added data and not finished" in {
        val sessionData = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(benefitDataType = CustomerAdded.name, claim = Some(aClaimCYAModel.copy(taxPaid = None))))

        await(underTest.filter(sessionData)) shouldBe Some(Redirect(ClaimsController.show(taxYearEOY, JobSeekersAllowance)))
      }
    }
  }
}
