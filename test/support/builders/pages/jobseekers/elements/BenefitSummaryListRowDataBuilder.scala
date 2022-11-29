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

package support.builders.pages.jobseekers.elements

import models.pages.elements.BenefitSummaryListRowData
import support.utils.TaxYearUtils.taxYearEOY

import java.time.LocalDate
import java.util.UUID

object BenefitSummaryListRowDataBuilder {

  val aBenefitSummaryListRowData: BenefitSummaryListRowData = BenefitSummaryListRowData(
    benefitId = UUID.randomUUID(),
    amount = Some(100.00),
    startDate = LocalDate.parse(s"$taxYearEOY-01-01"),
    endDate = LocalDate.parse(s"$taxYearEOY-04-05"),
    isIgnored = false
  )
}
