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

import models.IncomeTaxUserData
import models.pages.elements.BenefitSummaryListRowData

case class JobSeekersAllowancePage(taxYear: Int, summaryListDataRows: Seq[BenefitSummaryListRowData])

object JobSeekersAllowancePage {

  def apply(taxYear: Int, incomeTaxUserData: IncomeTaxUserData): JobSeekersAllowancePage = {
    val hmrcData = incomeTaxUserData.hmrcJobSeekersAllowances
      .map(BenefitSummaryListRowData.mapFrom(taxYear, _)).toSeq

    val customerData = incomeTaxUserData.customerJobSeekersAllowances
      .map(BenefitSummaryListRowData.mapFrom(taxYear, _)).toSeq

    val benefitSummaryListRowData = (hmrcData ++ customerData)
      .sortWith((it1, it2) => it1.startDate.isBefore(it2.startDate))

    JobSeekersAllowancePage(taxYear, benefitSummaryListRowData)
  }
}
