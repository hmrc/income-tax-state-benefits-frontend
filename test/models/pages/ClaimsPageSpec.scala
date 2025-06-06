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

package models.pages

import forms.YesNoForm
import models.BenefitType.JobSeekersAllowance
import models.pages.elements.BenefitDataRow
import play.api.data.Form
import support.UnitTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.CustomerAddedStateBenefitsDataBuilder.aCustomerAddedStateBenefitsData
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsDataBuilder.aStateBenefitsData
import support.providers.TaxYearProvider

import java.time.LocalDate
import java.util.UUID

class ClaimsPageSpec extends UnitTest
  with TaxYearProvider {

  val form: Form[Boolean] = YesNoForm.yesNoForm("some.error.message.key")

  ".apply" should {
    "create correct ClaimsPage object" in {
      val now = LocalDate.now()
      val stateBenefit_1 = aStateBenefit.copy(startDate = now.minusDays(0))
      val stateBenefit_2 = aStateBenefit.copy(startDate = now.minusDays(2), dateIgnored = None)
      val stateBenefit_3 = aCustomerAddedStateBenefit.copy(startDate = now.minusDays(1))
      val stateBenefit_4 = aCustomerAddedStateBenefit.copy(startDate = now.minusDays(3))

      val stateBenefitsData = aStateBenefitsData.copy(jobSeekersAllowances = Some(Set(stateBenefit_1, stateBenefit_2)))
      val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(jobSeekersAllowances = Some(Set(stateBenefit_3, stateBenefit_4)))
      val incomeTaxUserData = anIncomeTaxUserData.copy(Some(anAllStateBenefitsData.copy(Some(stateBenefitsData), Some(customerAddedStateBenefitsData))))

      val benefitDataRows = Seq(
        BenefitDataRow.mapFrom(taxYear, stateBenefit_4),
        BenefitDataRow.mapFrom(taxYear, stateBenefit_2),
        BenefitDataRow.mapFrom(taxYear, stateBenefit_3)
      )


      ClaimsPage.apply(taxYear = taxYear, JobSeekersAllowance, isInYear = false, incomeTaxUserData = incomeTaxUserData, form = form) shouldBe ClaimsPage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        isInYear = false,
        benefitDataRows = benefitDataRows,
        ignoredBenefitDataRows = Seq(BenefitDataRow.mapFrom(taxYear, stateBenefit_1)),
        form = form
      )
    }

    "create correct ClaimsPage object filtering overridden benefit ids" in {
      val now = LocalDate.now()
      val stateBenefit_1 = aStateBenefit.copy(benefitId = UUID.randomUUID(), startDate = now.minusDays(2), dateIgnored = None)
      val stateBenefit_2 = aStateBenefit.copy(startDate = now.minusDays(0), dateIgnored = None)
      val stateBenefit_3 = aCustomerAddedStateBenefit.copy(benefitId = stateBenefit_2.benefitId, startDate = now.minusDays(1))
      val stateBenefit_4 = aCustomerAddedStateBenefit.copy(startDate = now.minusDays(3))

      val stateBenefitsData = aStateBenefitsData.copy(jobSeekersAllowances = Some(Set(stateBenefit_1, stateBenefit_2)))
      val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(jobSeekersAllowances = Some(Set(stateBenefit_3, stateBenefit_4)))
      val incomeTaxUserData = anIncomeTaxUserData.copy(Some(anAllStateBenefitsData.copy(Some(stateBenefitsData), Some(customerAddedStateBenefitsData))))

      val benefitDataRows = Seq(
        BenefitDataRow.mapFrom(taxYear, stateBenefit_4),
        BenefitDataRow.mapFrom(taxYear, stateBenefit_1),
        BenefitDataRow.mapFrom(taxYear, stateBenefit_3)
      )

      ClaimsPage.apply(taxYear = taxYear, JobSeekersAllowance, isInYear = false, incomeTaxUserData = incomeTaxUserData, form) shouldBe ClaimsPage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        isInYear = false,
        benefitDataRows = benefitDataRows,
        ignoredBenefitDataRows = Seq(),
        form
      )
    }
  }
}
