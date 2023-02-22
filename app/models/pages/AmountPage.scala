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

import models.{BenefitType, ClaimCYAModel, StateBenefitsUserData}
import play.api.data.Form
import utils.InYearUtil.toDateWithinTaxYear

import java.time.LocalDate
import java.util.UUID

case class AmountPage(taxYear: Int,
                      benefitType: BenefitType,
                      titleFirstDate: LocalDate,
                      titleSecondDate: LocalDate,
                      sessionDataId: UUID,
                      hasEndDate: Boolean,
                      hasPaidTax: Boolean,
                      form: Form[BigDecimal])

object AmountPage {

  def apply(taxYear: Int,
            benefitType: BenefitType,
            stateBenefitsUserData: StateBenefitsUserData,
            form: Form[BigDecimal]): AmountPage = {
    val optAmount: Option[BigDecimal] = stateBenefitsUserData.claim.flatMap(_.amount)
    val claimCYAModel: ClaimCYAModel = stateBenefitsUserData.claim.get
    val titleFirstDate = toDateWithinTaxYear(taxYear, claimCYAModel.startDate)
    val titleSecondDate = claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05"))

    AmountPage(
      taxYear = taxYear,
      benefitType = benefitType,
      titleFirstDate = titleFirstDate,
      titleSecondDate = titleSecondDate,
      sessionDataId = stateBenefitsUserData.sessionDataId.get,
      hasEndDate = stateBenefitsUserData.claim.get.endDateQuestion.getOrElse(false),
      hasPaidTax = stateBenefitsUserData.claim.get.taxPaidQuestion.getOrElse(false),
      form = optAmount.fold(form)(amount => if (form.hasErrors) form else form.fill(amount))
    )
  }
}
