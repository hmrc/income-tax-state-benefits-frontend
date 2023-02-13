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

package models.pages.elements

import support.UnitTest
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.StateBenefitBuilder.aStateBenefit
import support.providers.TaxYearProvider

import java.time.LocalDate

class BenefitDataRowSpec extends UnitTest
  with TaxYearProvider {

  "mapFrom stateBenefit" should {
    "return correct BenefitSummaryListRowData" in {
      BenefitDataRow.mapFrom(taxYear, aStateBenefit) shouldBe BenefitDataRow(
        benefitId = aStateBenefit.benefitId,
        amount = aStateBenefit.amount,
        startDate = aStateBenefit.startDate,
        endDate = aStateBenefit.endDate.get,
        isIgnored = aStateBenefit.dateIgnored.nonEmpty
      )
    }

    "return correct BenefitSummaryListRowData when state benefit has not endDate" in {
      BenefitDataRow.mapFrom(taxYear, aStateBenefit.copy(endDate = None)) shouldBe BenefitDataRow(
        benefitId = aStateBenefit.benefitId,
        amount = aStateBenefit.amount,
        startDate = aStateBenefit.startDate,
        endDate = LocalDate.parse(s"$taxYear-04-05"),
        isIgnored = aStateBenefit.dateIgnored.nonEmpty
      )
    }
  }

  "mapFrom customerAddedStateBenefit" should {
    "return correct BenefitSummaryListRowData" in {
      BenefitDataRow.mapFrom(taxYear, aCustomerAddedStateBenefit) shouldBe BenefitDataRow(
        benefitId = aCustomerAddedStateBenefit.benefitId,
        amount = aCustomerAddedStateBenefit.amount,
        startDate = aCustomerAddedStateBenefit.startDate,
        endDate = aCustomerAddedStateBenefit.endDate.get,
        isIgnored = false
      )
    }

    "return correct BenefitSummaryListRowData when customer added state benefit has not endDate" in {
      BenefitDataRow.mapFrom(taxYear, aCustomerAddedStateBenefit.copy(endDate = None)) shouldBe BenefitDataRow(
        benefitId = aCustomerAddedStateBenefit.benefitId,
        amount = aCustomerAddedStateBenefit.amount,
        startDate = aCustomerAddedStateBenefit.startDate,
        endDate = LocalDate.parse(s"$taxYear-04-05"),
        isIgnored = false
      )
    }
  }
}
