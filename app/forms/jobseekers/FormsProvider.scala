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

package forms.jobseekers

import forms.validation.mappings.MappingUtil.dateMapping
import forms.{AmountForm, DateFormData, YesNoForm}
import models.ClaimCYAModel
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

  def startDateForm(taxYear: Int, isAgent: Boolean)
                   (implicit messages: Messages): Form[DateFormData] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    val emptyDayKey = s"jobseekers.startDatePage.error.empty.day.$isAgentSuffix"
    val emptyMonthKey = s"jobseekers.startDatePage.error.empty.month.$isAgentSuffix"
    val emptyYearKey = s"jobseekers.startDatePage.error.empty.year.$isAgentSuffix"
    val invalidDateKey = s"jobseekers.startDatePage.error.invalid.date.$isAgentSuffix"
    val tooLongAgoKey = Some(s"jobseekers.startDatePage.error.tooLongAgo.$isAgentSuffix")

    Form(
      dateMapping(emptyDayKey, emptyMonthKey, emptyYearKey, invalidDateKey, tooLongAgoKey)
        .verifying(
          messages(s"jobseekers.startDatePage.error.mustBeSameAsOrBefore.date.$isAgentSuffix", taxYear.toString),
          dateFormData => dateFormData.toLocalDate.forall(_.isBefore(LocalDate.of(taxYear, APRIL, SIX)))
        )
    )
  }

  def endDateYesNoForm(taxYear: Int): Form[Boolean] = YesNoForm.yesNoForm(
    missingInputError = "jobseekers.didClaimEndInTaxYear.error", Seq(taxYear.toString)
  )

  def endDateForm(taxYear: Int,
                  isAgent: Boolean,
                  claimStartDate: LocalDate)
                 (implicit messages: Messages): Form[DateFormData] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    val emptyDayKey = s"jobseekers.endDatePage.error.empty.day.$isAgentSuffix"
    val emptyMonthKey = s"jobseekers.endDatePage.error.empty.month.$isAgentSuffix"
    val emptyYearKey = s"jobseekers.endDatePage.error.empty.year.$isAgentSuffix"
    val invalidDateKey = "jobseekers.endDatePage.error.invalid.date"

    Form(
      dateMapping(emptyDayKey, emptyMonthKey, emptyYearKey, invalidDateKey, None)
        .verifying(
          messages(s"jobseekers.endDatePage.error.mustBeEndOfYear.$isAgentSuffix", (taxYear - 1).toString, taxYear.toString),
          dateFormData => dateFormData.toLocalDate
            .forall(date => date.isAfter(LocalDate.of(taxYear - 1, APRIL, SIX - 1)) && date.isBefore(LocalDate.of(taxYear, APRIL, SIX)))
        ).verifying(
        messages(s"jobseekers.endDatePage.error.mustBeAfterStartDate.$isAgentSuffix", translatedDateFormatter(claimStartDate)),
        dateFormData => dateFormData.toLocalDate.forall(date => date.isAfter(claimStartDate))
      )
    )
  }

  def jsaAmountForm(): Form[BigDecimal] = AmountForm.amountForm(
    emptyFieldKey = "jobseekers.amountPage.empty.amount.error",
    exceedsMaxAmountKey = "jobseekers.amountPage.exceedsMax.amount.error",
    wrongFormatKey = "jobseekers.amountPage.wrongFormat.amount.error",
    underMinAmountKey = Some("jobseekers.amountPage.lessThanZero.amount.error")
  )

  def taxTakenOffYesNoForm(isAgent: Boolean, taxYear: Int, claimCYAModel: ClaimCYAModel)
                          (implicit messages: Messages): Form[Boolean] = {
    val titleFirstDate = translatedDateFormatter(toDateWithinTaxYear(taxYear, claimCYAModel.startDate))
    val titleSecondDate = translatedDateFormatter(claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05")))

    YesNoForm.yesNoForm(s"jobseekers.taxTakenOff.error.${if (isAgent) "agent" else "individual"}", Seq(titleFirstDate, titleSecondDate))
  }

  def taxPaidAmountForm(): Form[BigDecimal] = AmountForm.amountForm(
    // TODO: This is wrong and will be implemented in another story. Test should be added when properly implemented
    emptyFieldKey = "empty error"
  )
}