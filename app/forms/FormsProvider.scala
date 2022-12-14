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

import forms.validation.mappings.MappingUtil.dateMapping
import models.{BenefitType, ClaimCYAModel}
import play.api.data.Form
import play.api.i18n.Messages
import utils.InYearUtil.toDateWithinTaxYear
import utils.ViewUtils.translatedDateFormatter

import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class FormsProvider() {

  private val APRIL = 4
  private val SIX = 6

  def startDateForm(taxYear: Int, benefitType: BenefitType, isAgent: Boolean, endDate: Option[LocalDate] = None)
                   (implicit messages: Messages): Form[DateFormData] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    val emptyDayKey = s"${benefitType.typeName}.startDatePage.error.empty.day.$isAgentSuffix"
    val emptyMonthKey = s"${benefitType.typeName}.startDatePage.error.empty.month.$isAgentSuffix"
    val emptyYearKey = s"${benefitType.typeName}.startDatePage.error.empty.year.$isAgentSuffix"
    val invalidDateKey = s"${benefitType.typeName}.startDatePage.error.invalid.date.$isAgentSuffix"
    val tooLongAgoKey = Some(s"${benefitType.typeName}.startDatePage.error.tooLongAgo.$isAgentSuffix")

    val mapping = dateMapping(emptyDayKey, emptyMonthKey, emptyYearKey, invalidDateKey, tooLongAgoKey)
      .verifying(
        messages(s"${benefitType.typeName}.startDatePage.error.mustBeSameAsOrBefore.date.$isAgentSuffix", taxYear.toString),
        dateFormData => dateFormData.toLocalDate.forall(_.isBefore(LocalDate.of(taxYear, APRIL, SIX)))
      )

    Form(
      endDate.map(date =>
        mapping.verifying(
          messages(s"${benefitType.typeName}.startDatePage.error.mustBeBefore.date.$isAgentSuffix", translatedDateFormatter(date)),
          dateFormData => dateFormData.toLocalDate.forall(_.isBefore(date))
        )
      ).getOrElse(mapping)
    )
  }

  def endDateYesNoForm(taxYear: Int): Form[Boolean] = YesNoForm.yesNoForm(
    missingInputError = "common.endDateQuestionPage.error", Seq(taxYear.toString)
  )

  def endDateForm(taxYear: Int,
                  benefitType: BenefitType,
                  isAgent: Boolean,
                  claimStartDate: LocalDate)
                 (implicit messages: Messages): Form[DateFormData] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
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

  def amountForm(benefitType: BenefitType): Form[BigDecimal] = AmountForm.amountForm(
    emptyFieldKey = s"${benefitType.typeName}.amountPage.empty.amount.error",
    exceedsMaxAmountKey = s"${benefitType.typeName}.amountPage.exceedsMax.amount.error",
    wrongFormatKey = s"${benefitType.typeName}.amountPage.wrongFormat.amount.error",
    underMinAmountKey = Some(s"${benefitType.typeName}.amountPage.lessThanZero.amount.error")
  )

  def taxTakenOffYesNoForm(taxYear: Int, benefitType: BenefitType, isAgent: Boolean, claimCYAModel: ClaimCYAModel)
                          (implicit messages: Messages): Form[Boolean] = {
    val titleFirstDate = translatedDateFormatter(toDateWithinTaxYear(taxYear, claimCYAModel.startDate))
    val titleSecondDate = translatedDateFormatter(claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05")))

    YesNoForm.yesNoForm(s"${benefitType.typeName}.taxPaidQuestionPage.error.${if (isAgent) "agent" else "individual"}", Seq(titleFirstDate, titleSecondDate))
  }

  def taxPaidAmountForm(): Form[BigDecimal] = AmountForm.amountForm(
    // TODO: This is wrong and will be implemented in another story. Test should be added when properly implemented
    emptyFieldKey = "empty error"
  )
}
