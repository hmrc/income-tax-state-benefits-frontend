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

import models.{BenefitType, ClaimCYAModel, StateBenefit, StateBenefitsUserData}
import utils.InYearUtil.toDateWithinTaxYear

import java.time.LocalDate
import java.util.UUID

case class ReviewClaimPage(taxYear: Int,
                           benefitType: BenefitType,
                           sessionDataId: UUID,
                           isInYear: Boolean,
                           isHmrcData: Boolean,
                           isIgnored: Boolean,
                           itemsFirstDate: LocalDate,
                           itemsSecondDate: LocalDate,
                           startDate: LocalDate,
                           endDateQuestion: Option[Boolean],
                           endDate: Option[LocalDate],
                           amount: Option[BigDecimal],
                           taxPaidQuestion: Option[Boolean],
                           taxPaid: Option[BigDecimal],
                           priorStartDate: Option[LocalDate],
                           priorEndDate: Option[LocalDate],
                           priorAmount: Option[BigDecimal],
                           priorTaxPaid: Option[BigDecimal])

object ReviewClaimPage {

  def apply(taxYear: Int,
            benefitType: BenefitType,
            isInYear: Boolean,
            stateBenefitsUserData: StateBenefitsUserData,
            priorData: Option[StateBenefit] = None): ReviewClaimPage = {
    val claimCYAModel: ClaimCYAModel = stateBenefitsUserData.claim.get

    ReviewClaimPage(
      taxYear = taxYear,
      benefitType = benefitType,
      sessionDataId = stateBenefitsUserData.sessionDataId.get,
      isInYear = isInYear,
      isHmrcData = stateBenefitsUserData.isHmrcData,
      isIgnored = claimCYAModel.dateIgnored.isDefined,
      itemsFirstDate = toDateWithinTaxYear(taxYear, claimCYAModel.startDate),
      itemsSecondDate = claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05")),
      startDate = claimCYAModel.startDate,
      endDateQuestion = claimCYAModel.endDateQuestion,
      endDate = claimCYAModel.endDate,
      amount = claimCYAModel.amount,
      taxPaidQuestion = claimCYAModel.taxPaidQuestion,
      taxPaid = claimCYAModel.taxPaid,
      priorStartDate = priorData.map(_.startDate),
      priorEndDate = priorData.flatMap(_.endDate),
      priorAmount = priorData.flatMap(_.amount),
      priorTaxPaid = priorData.flatMap(_.taxPaid)
    )
  }
}
