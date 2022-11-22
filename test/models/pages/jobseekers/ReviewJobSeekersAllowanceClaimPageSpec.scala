/*
 * Copyright 2022 HM Revenue & Customs
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

package models.pages.jobseekers

import support.UnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.providers.TaxYearProvider

import java.util.UUID

class ReviewJobSeekersAllowanceClaimPageSpec extends UnitTest
  with TaxYearProvider {

  ".apply" should {
    "create correct ReviewJobSeekersAllowanceClaimPage object" in {
      val isInYear = true
      val claim = aStateBenefitsUserData.claim.get

      ReviewJobSeekersAllowanceClaimPage.apply(taxYear, isInYear, aStateBenefitsUserData) shouldBe ReviewJobSeekersAllowanceClaimPage(
        taxYear = taxYear,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        isInYear = isInYear,
        isUsingCustomerData = !aStateBenefitsUserData.isPriorSubmission,
        startDate = Some(claim.startDate),
        endDateQuestion = claim.endDateQuestion,
        endDate = claim.endDate,
        amount = claim.amount,
        taxPaidQuestion = claim.taxPaidQuestion,
        taxPaid = claim.taxPaid
      )
    }
  }
}
