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

import forms.AmountForm
import forms.jobseekers.FormsProvider
import models.BenefitType.JobSeekersAllowance
import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.providers.TaxYearProvider

import java.time.LocalDate

class AmountPageSpec extends UnitTest
  with TaxYearProvider {

  private val pageForm = new FormsProvider().jsaAmountForm()

  "AmountPage.apply(...)" should {
    "return page with pre-filled form when amount is preset" in {
      val claimCYAModel = aClaimCYAModel.copy(amount = Some(123.45))
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      AmountPage.apply(taxYearEOY, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe AmountPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = stateBenefitsUserData.claim.get.startDate,
        titleSecondDate = stateBenefitsUserData.claim.get.endDate.get,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm.fill(value = 123.45)
      )
    }

    "return page without pre-filled form when amount is not preset" in {
      val claimCYAModel = aClaimCYAModel.copy(amount = None)
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      AmountPage.apply(taxYearEOY, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe AmountPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = stateBenefitsUserData.claim.get.startDate,
        titleSecondDate = stateBenefitsUserData.claim.get.endDate.get,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }

    "return page with pre-filled form with errors when form has errors" in {
      val formWithErrors = pageForm.bind(Map(AmountForm.amount -> "wrong-amount"))

      AmountPage.apply(taxYearEOY, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe AmountPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = aStateBenefitsUserData.claim.get.startDate,
        titleSecondDate = aStateBenefitsUserData.claim.get.endDate.get,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = pageForm.bind(Map(AmountForm.amount -> "wrong-amount"))
      )
    }

    "return page with titleFirstDate equal to startDate when after start of financial year" in {
      val claimCYAModel = aClaimCYAModel.copy(startDate = LocalDate.of(taxYear - 1, 4, 6), amount = None)
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      AmountPage.apply(taxYear, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe AmountPage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        titleFirstDate = stateBenefitsUserData.claim.get.startDate,
        titleSecondDate = stateBenefitsUserData.claim.get.endDate.get,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }

    "return page with titleFirstDate equal to start of financial year when startDate is before that" in {
      val claimCYAModel = aClaimCYAModel.copy(startDate = LocalDate.of(taxYear - 1, 4, 5), amount = None)
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      AmountPage.apply(taxYear, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe AmountPage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        titleFirstDate = LocalDate.of(taxYear - 1, 4, 6),
        titleSecondDate = stateBenefitsUserData.claim.get.endDate.get,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }

    "return page with titleSecondDate equal to endDate when exists" in {
      val claimCYAModel = aClaimCYAModel.copy(endDate = Some(LocalDate.of(taxYear, 1, 1)), amount = None)
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      AmountPage.apply(taxYearEOY, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe AmountPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = stateBenefitsUserData.claim.get.startDate,
        titleSecondDate = stateBenefitsUserData.claim.get.endDate.get,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }

    "return page with titleSecondDate equal to end of financial year when endDate is missing" in {
      val claimCYAModel = aClaimCYAModel.copy(endDate = None, amount = None)
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      AmountPage.apply(taxYearEOY, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe AmountPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = stateBenefitsUserData.claim.get.startDate,
        titleSecondDate = LocalDate.of(taxYearEOY, 4, 5),
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }
  }
}
