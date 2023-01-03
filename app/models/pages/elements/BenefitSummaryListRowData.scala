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

import models.{CustomerAddedStateBenefit, StateBenefit}

import java.time.LocalDate
import java.util.UUID

case class BenefitSummaryListRowData(benefitId: UUID,
                                     amount: Option[BigDecimal],
                                     startDate: LocalDate,
                                     endDate: LocalDate,
                                     isIgnored: Boolean)

object BenefitSummaryListRowData {

  def mapFrom(taxYear: Int, stateBenefit: StateBenefit): BenefitSummaryListRowData = BenefitSummaryListRowData(
    benefitId = stateBenefit.benefitId,
    amount = stateBenefit.amount,
    startDate = stateBenefit.startDate,
    endDate = stateBenefit.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05")),
    isIgnored = stateBenefit.dateIgnored.nonEmpty
  )

  def mapFrom(taxYear: Int, customerAddedStateBenefit: CustomerAddedStateBenefit): BenefitSummaryListRowData = BenefitSummaryListRowData(
    benefitId = customerAddedStateBenefit.benefitId,
    amount = customerAddedStateBenefit.amount,
    startDate = customerAddedStateBenefit.startDate,
    endDate = customerAddedStateBenefit.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05")),
    isIgnored = false
  )
}
