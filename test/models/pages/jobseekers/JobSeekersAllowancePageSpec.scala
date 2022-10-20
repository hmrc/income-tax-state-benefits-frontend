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

import models.pages.elements.BenefitSummaryListRowData
import support.UnitTest
import support.builders.models.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.providers.TaxYearProvider

class JobSeekersAllowancePageSpec extends UnitTest
  with TaxYearProvider {

  ".apply" should {
    "create correct JobSeekersAllowancePage object" in {
      val hmrcData = anIncomeTaxUserData.hmrcJobSeekersAllowances
        .map(BenefitSummaryListRowData.mapFrom(taxYear, _)).toSeq

      val customerData = anIncomeTaxUserData.customerJobSeekersAllowances
        .map(BenefitSummaryListRowData.mapFrom(taxYear, _)).toSeq

      JobSeekersAllowancePage.apply(taxYear = taxYear, incomeTaxUserData = anIncomeTaxUserData) shouldBe JobSeekersAllowancePage(
        taxYear = taxYear,
        summaryListDataRows = hmrcData ++ customerData
      )
    }
  }
}
