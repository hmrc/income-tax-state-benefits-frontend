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

case class ReviewClaimPage(taxYear: Int,
                           sessionDataId: UUID,
                           isInYear: Boolean,
                           isCustomerAdded: Boolean,
                           itemsFirstDate: LocalDate,
                           itemsSecondDate: LocalDate,
                           startDate: LocalDate,
                           endDateQuestion: Option[Boolean],
                           endDate: Option[LocalDate],
                           amount: Option[BigDecimal],
                           taxPaidQuestion: Option[Boolean],
                           taxPaid: Option[BigDecimal])

object ReviewClaimPage {

  def apply(taxYear: Int,
            isInYear: Boolean,
            stateBenefitsUserData: StateBenefitsUserData): ReviewClaimPage = {
    val claimCYAModel: ClaimCYAModel = stateBenefitsUserData.claim.get
    val itemsFirstDate = toDateWithinTaxYear(taxYear, claimCYAModel.startDate)
    val itemsSecondDate = claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05"))

    ReviewClaimPage(
      taxYear = taxYear,
      sessionDataId = stateBenefitsUserData.sessionDataId.get,
      isInYear = isInYear,
      isCustomerAdded = !stateBenefitsUserData.isPriorSubmission,
      itemsFirstDate = itemsFirstDate,
      itemsSecondDate = itemsSecondDate,
      startDate = claimCYAModel.startDate,
      endDateQuestion = claimCYAModel.endDateQuestion,
      endDate = claimCYAModel.endDate,
      amount = claimCYAModel.amount,
      taxPaidQuestion = claimCYAModel.taxPaidQuestion,
      taxPaid = claimCYAModel.taxPaid
    )
  }
}