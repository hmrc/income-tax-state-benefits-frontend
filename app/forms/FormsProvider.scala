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

import forms.DateForm.validateStartDate
import forms.validation.mappings.MappingUtil.dateMapping
import models.{BenefitType, ClaimCYAModel}
import play.api.data.Form
import play.api.i18n.Messages
import utils.InYearUtil.toDateWithinTaxYear
import utils.ViewUtils.translatedDateFormatter

import java.time.LocalDate
import java.time.Month.APRIL
import javax.inject.Singleton

@Singleton
class FormsProvider() {

  private val SIX = 6

  def validatedStartDateForm(dateForm: Form[DateFormData],
                             taxYear: Int,
                             benefitType: BenefitType,
                             isAgent: Boolean,
                             endDate: Option[LocalDate])
                            (implicit messages: Messages): Form[DateFormData] = {
    dateForm.copy(errors = validateStartDate(dateForm.get, taxYear, benefitType, isAgent, endDate))
  }

  // TODO: Test in template test
  def endDateYesNoForm(taxYear: Int): Form[Boolean] = YesNoForm.yesNoForm(
    missingInputError = "common.endDateQuestionPage.error", Seq(taxYear.toString)
  )

  // TODO: Test in template test
  def endDateForm(taxYear: Int,
                  benefitType: BenefitType,
                  isAgent: Boolean,
                  claimStartDate: LocalDate)
                 (implicit messages: Messages): Form[DateFormData] = {
    lazy val isAgentSuffix = userType(isAgent)
    val emptyDayKey = s"${benefitType.typeName}.endDatePage.error.empty.day.$isAgentSuffix"
    val emptyMonthKey = s"${benefitType.typeName}.endDatePage.error.empty.month.$isAgentSuffix"
    val emptyYearKey = s"${benefitType.typeName}.endDatePage.error.empty.year.$isAgentSuffix"
    val invalidDateKey = s"${benefitType.typeName}.endDatePage.error.invalid.date"

    Form(
      dateMapping(emptyDayKey, emptyMonthKey, emptyYearKey, invalidDateKey, None)
        .verifying(
          messages(s"${benefitType.typeName}.endDatePage.error.mustBeEndOfYear.$isAgentSuffix", (taxYear - 1).toString, taxYear.toString),
          dateFormData => dateFormData.toLocalDate
            .forall(date => date.isAfter(LocalDate.of(taxYear - 1, APRIL, SIX - 1)) && date.isBefore(LocalDate.of(taxYear, APRIL, SIX)))
        ).verifying(
        messages(s"${benefitType.typeName}.endDatePage.error.mustBeAfterStartDate.$isAgentSuffix", translatedDateFormatter(claimStartDate)),
        dateFormData => dateFormData.toLocalDate.forall(date => date.isAfter(claimStartDate))
      )
    )
  }

  def amountForm(benefitType: BenefitType, minAmount: Option[BigDecimal] = None): Form[BigDecimal] = AmountForm.amountForm(
    emptyFieldKey = s"${benefitType.typeName}.amountPage.empty.amount.error",
    minOrLessKey = s"${benefitType.typeName}.amountPage.minimumOrLess.amount.error",
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

  // TODO: Test in template test
  def taxPaidAmountForm(benefitType: BenefitType, isAgent: Boolean, maxAmount: BigDecimal): Form[BigDecimal] = AmountForm.amountForm(
    emptyFieldKey = s"${benefitType.typeName}.taxPaidPage.empty.amount.error.${userType(isAgent)}",
    minOrLessKey = "common.taxPaidPage.zeroOrLess.amount.error",
    minOrLessValue = 0,
    maxAmountKey = "common.taxPaidPage.exceedsMax.amount.error",
    maxAmountValue = maxAmount,
    wrongFormatKey = "common.taxPaidPage.wrongFormat.amount.error"
  )

  private def userType(isAgent: Boolean): String = if (isAgent) "agent" else "individual"
}
