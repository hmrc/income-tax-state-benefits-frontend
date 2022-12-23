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

import models.BenefitType.JobSeekersAllowance
import models.pages.elements.BenefitSummaryListRowData
import support.UnitTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.CustomerAddedStateBenefitsDataBuilder.aCustomerAddedStateBenefitsData
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsDataBuilder.aStateBenefitsData
import support.providers.TaxYearProvider

import java.time.LocalDate

class ClaimsPageSpec extends UnitTest
  with TaxYearProvider {

  ".apply" should {
    "create correct ClaimsPage object" in {
      val now = LocalDate.now()
      val stateBenefit_1 = aStateBenefit.copy(startDate = now.minusDays(0))
      val stateBenefit_2 = aStateBenefit.copy(startDate = now.minusDays(2))
      val stateBenefit_3 = aCustomerAddedStateBenefit.copy(startDate = now.minusDays(1))
      val stateBenefit_4 = aCustomerAddedStateBenefit.copy(startDate = now.minusDays(3))

      val stateBenefitsData = aStateBenefitsData.copy(jobSeekersAllowances = Some(Set(stateBenefit_1, stateBenefit_2)))
      val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(jobSeekersAllowances = Some(Set(stateBenefit_3, stateBenefit_4)))
      val incomeTaxUserData = anIncomeTaxUserData.copy(Some(anAllStateBenefitsData.copy(Some(stateBenefitsData), Some(customerAddedStateBenefitsData))))

      val summaryListDataRows = Seq(
        BenefitSummaryListRowData.mapFrom(taxYear, stateBenefit_4),
        BenefitSummaryListRowData.mapFrom(taxYear, stateBenefit_2),
        BenefitSummaryListRowData.mapFrom(taxYear, stateBenefit_3),
        BenefitSummaryListRowData.mapFrom(taxYear, stateBenefit_1),
      )

      ClaimsPage.apply(taxYear = taxYear, JobSeekersAllowance, isInYear = false, incomeTaxUserData = incomeTaxUserData) shouldBe ClaimsPage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        isInYear = false,
        summaryListDataRows = summaryListDataRows
      )
    }
  }
}
