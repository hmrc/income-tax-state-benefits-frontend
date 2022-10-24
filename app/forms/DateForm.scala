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

package forms

import filters.InputFilters
import forms.validation.mappings.MappingUtil.trimmedText
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import utils.ViewUtils.translatedDateFormatter

import java.time.LocalDate

// TODO: (Hristo) Test me and fix me.
object DateForm extends InputFilters {

  val formValuesPrefix = "value-for"

  private val APRIL = 4
  private val SIX = 6

  val day: String = s"$formValuesPrefix-day"
  val month: String = s"$formValuesPrefix-month"
  val year: String = s"$formValuesPrefix-year"

  def dateForm(): Form[DateFormData] = Form(
    mapping(
      day -> trimmedText.transform[String](filter, identity),
      month -> trimmedText.transform[String](filter, identity),
      year -> trimmedText.transform[String](filter, identity)
    )(DateFormData.apply)(DateFormData.unapply)
  )

  def validateStartDate(dateFormData: DateFormData, taxYear: Int, isAgent: Boolean): Seq[FormError] = {
    (emptyStartDateValidation(dateFormData, isAgent) ++
      localStartDateValidation(dateFormData, isAgent) ++
      validateStartDateIsBeforeEOY(dateFormData, taxYear, isAgent)).headOption.toSeq
  }


  def validateEndDate(dateFormData: DateFormData,
                      taxYear: Int,
                      isAgent: Boolean,
                      startDate: LocalDate)
                     (implicit messages: Messages): Seq[FormError] = {
    (emptyEndDateValidation(dateFormData) ++
      localEndDateValidation(dateFormData) ++
      validateEndDateIsEOY(dateFormData, taxYear, isAgent) ++
      validateEndDateIsAfterStartDate(dateFormData, isAgent, startDate)).headOption.toSeq
  }

  private def emptyStartDateValidation(dateFormData: DateFormData, isAgent: Boolean): Seq[FormError] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    (dateFormData.day.isEmpty, dateFormData.month.isEmpty, dateFormData.year.isEmpty) match {
      case (false, false, false) => Seq()
      case _ => Seq(FormError(s"invalidDate", s"jobseekers.startDatePage.invalid.date.error.$isAgentSuffix"))
    }
  }

  private def emptyEndDateValidation(dateFormData: DateFormData): Seq[FormError] = {
    (dateFormData.day.isEmpty, dateFormData.month.isEmpty, dateFormData.year.isEmpty) match {
      case (false, false, false) => Seq()
      case _ => Seq(FormError(s"invalidDate", "jobseekers.endDatePage.invalid.date.error"))
    }
  }

  def localStartDateValidation(dateFormData: DateFormData, isAgent: Boolean): Seq[FormError] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    dateFormData.toLocalDate.fold(Seq[FormError](FormError(s"invalidDate", s"jobseekers.startDatePage.invalid.date.error.$isAgentSuffix")))(_ => Seq())
  }

  def localEndDateValidation(dateFormData: DateFormData): Seq[FormError] = {
    dateFormData.toLocalDate.fold(Seq[FormError](FormError(s"invalidDate", s"jobseekers.endDatePage.invalid.date.error")))(_ => Seq())
  }

  private def validateStartDateIsBeforeEOY(dateFormData: DateFormData, taxYear: Int, isAgent: Boolean): Seq[FormError] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    dateFormData.toLocalDate
      .filter(_.isBefore(LocalDate.of(taxYear, APRIL, SIX)))
      .map(_ => Seq())
      .getOrElse(Seq(FormError(s"mustBeSameAsOrBefore", s"jobseekers.startDatePage.mustBeSameAsOrBefore.date.error.$isAgentSuffix", Seq(taxYear.toString))))
  }

  private def validateEndDateIsEOY(dateFormData: DateFormData, taxYear: Int, isAgent: Boolean): Seq[FormError] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    dateFormData.toLocalDate
      .filter(_.isAfter(LocalDate.of(taxYear - 1, APRIL, SIX - 1)))
      .filter(_.isBefore(LocalDate.of(taxYear, APRIL, SIX)))
      .map(_ => Seq())
      .getOrElse(Seq(FormError(s"mustBeEndOfYear", s"jobseekers.endDatePage.mustBeEndOfYear.date.error.$isAgentSuffix", Seq((taxYear - 1).toString, taxYear.toString))))
  }

  def validateEndDateIsAfterStartDate(dateFormData: DateFormData,
                                      isAgent: Boolean,
                                      startDate: LocalDate)
                                     (implicit messages: Messages): Seq[FormError] = {
    lazy val isAgentSuffix = if (isAgent) "agent" else "individual"
    dateFormData.toLocalDate
      .filter(_.isAfter(startDate))
      .map(_ => Seq.empty)
      .getOrElse(Seq(FormError(
        s"mustBeAfterStartDate", s"jobseekers.endDatePage.mustBeAfterStartDate.date.error.$isAgentSuffix",
        Seq(translatedDateFormatter(startDate))
      )))
  }
}
