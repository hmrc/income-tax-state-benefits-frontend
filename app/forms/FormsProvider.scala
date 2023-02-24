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

package forms

import forms.DateForm.{validateEndDate, validateStartDate}
import models.{BenefitType, ClaimCYAModel}
import play.api.data.Form
import play.api.i18n.Messages
import utils.InYearUtil.toDateWithinTaxYear
import utils.ViewUtils.{translatedDateFormatter, translatedTaxYearEndDateFormatter}

import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class FormsProvider() {

  def validatedStartDateForm(dateForm: Form[DateFormData],
                             taxYear: Int,
                             benefitType: BenefitType,
                             isAgent: Boolean,
                             endDate: Option[LocalDate])
                            (implicit messages: Messages): Form[DateFormData] = {
    dateForm.copy(errors = validateStartDate(dateForm.get, taxYear, benefitType, isAgent, endDate))
  }

  def endDateYesNoForm(taxYear: Int, startDate: LocalDate)(implicit messages: Messages): Form[Boolean] = YesNoForm.yesNoForm(
    missingInputError = "common.endDateQuestionPage.error",
    Seq(translatedDateFormatter(toDateWithinTaxYear(taxYear, startDate)), translatedTaxYearEndDateFormatter(taxYear))
  )

  def validatedEndDateForm(dateForm: Form[DateFormData],
                           taxYear: Int,
                           benefitType: BenefitType,
                           isAgent: Boolean,
                           startDate: LocalDate)
                          (implicit messages: Messages): Form[DateFormData] = {
    dateForm.copy(errors = validateEndDate(dateForm.get, taxYear, benefitType, isAgent, startDate))
  }

  def amountForm(benefitType: BenefitType, isAgent: Boolean, minAmount: Option[BigDecimal] = None): Form[BigDecimal] = AmountForm.amountForm(
    emptyFieldKey = s"${benefitType.typeName}.amountPage.empty.amount.error",
    minOrLessKey = s"${benefitType.typeName}.amountPage.mustBeMoreThanTax.amount.error.${userType(isAgent)}",
    minOrLessValue = minAmount.getOrElse(0),
    maxAmountKey = s"${benefitType.typeName}.amountPage.exceedsMax.amount.error",
    wrongFormatKey = s"${benefitType.typeName}.amountPage.wrongFormat.amount.error"
  )

  // TODO: Test in template test
  def taxTakenOffYesNoForm(taxYear: Int, benefitType: BenefitType, isAgent: Boolean, claimCYAModel: ClaimCYAModel)
                          (implicit messages: Messages): Form[Boolean] = {
    val titleFirstDate = translatedDateFormatter(toDateWithinTaxYear(taxYear, claimCYAModel.startDate))
    val titleSecondDate = translatedDateFormatter(claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05")))

    YesNoForm.yesNoForm(s"${benefitType.typeName}.taxPaidQuestionPage.error.${userType(isAgent)}", Seq(titleFirstDate, titleSecondDate))
  }

  def taxPaidAmountForm(benefitType: BenefitType, isAgent: Boolean, maxAmount: BigDecimal): Form[BigDecimal] = AmountForm.amountForm(
    emptyFieldKey = s"${benefitType.typeName}.taxPaidPage.empty.amount.error.${userType(isAgent)}",
    minOrLessKey = "common.taxPaidPage.zeroOrLess.amount.error",
    minOrLessValue = 0,
    maxAmountKey = s"${benefitType.typeName}.taxPaidPage.exceedsAmount.amount.error.${userType(isAgent)}",
    maxAmountValue = maxAmount,
    wrongFormatKey = "common.taxPaidPage.wrongFormat.amount.error"
  )

  private def userType(isAgent: Boolean): String = if (isAgent) "agent" else "individual"
}
