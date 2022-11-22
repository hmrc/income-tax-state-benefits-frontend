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

import models.{ClaimCYAModel, StateBenefitsUserData}
import utils.InYearUtil.toDateWithinTaxYear

import java.time.LocalDate
import java.util.UUID

case class RemoveClaimPage(taxYear: Int,
                      titleFirstDate: LocalDate,
                      endDate: Boolean,
                      titleSecondDate: LocalDate,
                      sessionDataId: UUID,
                      amount: BigDecimal,
                      taxPaidQuestion: Boolean,
                      taxPaid: Option[BigDecimal])

object RemoveClaimPage {

  def apply(taxYear: Int,
            stateBenefitsUserData: StateBenefitsUserData): RemoveClaimPage = {

    val claimCYAModel: ClaimCYAModel = stateBenefitsUserData.claim.get
    val titleFirstDate = toDateWithinTaxYear(taxYear, claimCYAModel.startDate)
    val titleSecondDate = claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05"))

    RemoveClaimPage(
      taxYear = taxYear,
      titleFirstDate = titleFirstDate,
      endDate = claimCYAModel.endDate.isDefined,
      titleSecondDate = titleSecondDate,
      sessionDataId = stateBenefitsUserData.sessionDataId.get,
      amount = claimCYAModel.amount.get,
      taxPaidQuestion = claimCYAModel.taxPaidQuestion.fold(false)(identity),
      taxPaid = claimCYAModel.taxPaid
    )
  }
}