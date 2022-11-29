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
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.providers.TaxYearProvider

import java.time.LocalDate

class RemoveClaimPageSpec extends UnitTest
  with TaxYearProvider {

  "RemoveClaimPage.apply(...)" should {
    "return prefilled data when end date is present and start date is within tax year" in {
      val claim = aStateBenefitsUserData.claim.get

      RemoveClaimPage.apply(taxYearEOY, aStateBenefitsUserData) shouldBe RemoveClaimPage(
        taxYear = taxYearEOY,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        itemsFirstDate = claim.startDate,
        itemsSecondDate = claim.endDate.get,
        startDate = claim.startDate,
        endDateQuestion = claim.endDateQuestion,
        endDate = claim.endDate,
        amount = claim.amount,
        taxPaidQuestion = claim.taxPaidQuestion,
        taxPaid = claim.taxPaid)
    }

    "return prefilled data when end date is not present" in {
      val claim = aStateBenefitsUserData.claim.get.copy(endDate = None)
      val stateBenefitsData = aStateBenefitsUserData.copy(claim = Some(claim))

      RemoveClaimPage.apply(taxYearEOY, stateBenefitsData) shouldBe RemoveClaimPage(
        taxYear = taxYearEOY,
        sessionDataId = stateBenefitsData.sessionDataId.get,
        itemsFirstDate = claim.startDate,
        itemsSecondDate = LocalDate.parse(s"$taxYearEOY-04-05"),
        startDate = claim.startDate,
        endDateQuestion = claim.endDateQuestion,
        endDate = None,
        amount = claim.amount,
        taxPaidQuestion = claim.taxPaidQuestion,
        taxPaid = claim.taxPaid)
    }

    "return prefilled data when start date is not within tax year" in {
      val claim = aStateBenefitsUserData.claim.get.copy(startDate = LocalDate.of(taxYearEOY - 1, 4, 5))
      val stateBenefitsData = aStateBenefitsUserData.copy(claim = Some(claim))
      RemoveClaimPage.apply(taxYearEOY, stateBenefitsData) shouldBe RemoveClaimPage(
        taxYear = taxYearEOY,
        sessionDataId = stateBenefitsData.sessionDataId.get,
        itemsFirstDate = LocalDate.of(taxYearEOY - 1, 4, 6),
        itemsSecondDate = claim.endDate.get,
        startDate = claim.startDate,
        endDateQuestion = claim.endDateQuestion,
        endDate = claim.endDate,
        amount = claim.amount,
        taxPaidQuestion = claim.taxPaidQuestion,
        taxPaid = claim.taxPaid)
    }
  }
}
