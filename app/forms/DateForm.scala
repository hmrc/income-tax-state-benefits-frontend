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

import filters.InputFilters
import forms.validation.mappings.MappingUtil.trimmedText
import models.BenefitType
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import utils.ViewUtils.translatedDateFormatter

import java.time.LocalDate
import java.time.Month.APRIL
import scala.util.Try

object DateForm extends InputFilters {

  private val SIX = 6

  val formValuesPrefix = "value-for"

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

  def validateStartDate(formData: DateFormData,
                        taxYear: Int,
                        benefitType: BenefitType,
                        isAgent: Boolean,
                        endDate: Option[LocalDate])
                       (implicit messages: Messages): Seq[FormError] = {
    lazy val emptyDateFieldsValidationValue = emptyDateFieldsValidation(formData, benefitType, datePageName = "startDatePage", isAgent)
    lazy val invalidDateFormatValidationValue = invalidDateFormatValidation(formData, benefitType, datePageName = "startDatePage", isAgent)
    lazy val startDateSpecificValidationValue = startDateSpecificValidation(formData.toLocalDate.get, taxYear, benefitType, isAgent, endDate)

    emptyDateFieldsValidationValue match {
      case _ :: _ => emptyDateFieldsValidationValue
      case _ => if (invalidDateFormatValidationValue.nonEmpty) invalidDateFormatValidationValue else startDateSpecificValidationValue
    }
  }

  private def startDateSpecificValidation(startDate: LocalDate,
                                          taxYear: Int,
                                          benefitType: BenefitType,
                                          isAgent: Boolean,
                                          endDate: Option[LocalDate])
                                         (implicit messages: Messages): Seq[FormError] = {
    val isAfterMinDate = startDate.isAfter(LocalDate.parse("1900-01-01"))
    val isBeforeEOY = startDate.isBefore(LocalDate.of(taxYear, APRIL, SIX))
    val isBeforeDate = startDate.isBefore(endDate.getOrElse(startDate.plusDays(1)))

    lazy val tooLongAgoErrorMessage = s"${benefitType.typeName}.startDatePage.error.tooLongAgo.${userType(isAgent)}"
    lazy val mustBeSameAsOrBeforeErrorMessage = s"${benefitType.typeName}.startDatePage.error.mustBeSameAsOrBefore.date.${userType(isAgent)}"
    lazy val mustBeBeforeErrorMessage = s"${benefitType.typeName}.startDatePage.error.mustBeBefore.date.${userType(isAgent)}"

    (isAfterMinDate, isBeforeEOY, isBeforeDate) match {
      case (false, _, _) => Seq(FormError("invalidOrNotAllowed", tooLongAgoErrorMessage))
      case (true, false, _) => Seq(FormError("invalidOrNotAllowed", mustBeSameAsOrBeforeErrorMessage, Seq(taxYear.toString)))
      case (true, _, false) => Seq(FormError("invalidOrNotAllowed", mustBeBeforeErrorMessage, Seq(translatedDateFormatter(endDate.get))))
      case _ => Seq.empty
    }
  }

  private def emptyDateFieldsValidation(formData: DateFormData,
                                        benefitType: BenefitType,
                                        datePageName: String,
                                        isAgent: Boolean): Seq[FormError] = {
    lazy val userTypeValue = userType(isAgent)

    (formData.day.isEmpty, formData.month.isEmpty, formData.year.isEmpty) match {
      case (true, true, true) => Seq(FormError(s"emptyAll", s"${benefitType.typeName}.$datePageName.error.empty.all.$userTypeValue"))
      case (true, true, false) => Seq(FormError("emptyDayMonth", s"${benefitType.typeName}.$datePageName.error.empty.dayMonth.$userTypeValue"))
      case (true, false, true) => Seq(FormError("emptyDayYear", s"${benefitType.typeName}.$datePageName.error.empty.dayYear.$userTypeValue"))
      case (false, true, true) => Seq(FormError("emptyMonthYear", s"${benefitType.typeName}.$datePageName.error.empty.monthYear.$userTypeValue"))
      case (false, false, true) => Seq(FormError("emptyYear", s"${benefitType.typeName}.$datePageName.error.empty.year.$userTypeValue"))
      case (false, true, false) => Seq(FormError("emptyMonth", s"${benefitType.typeName}.$datePageName.error.empty.month.$userTypeValue"))
      case (true, false, false) => Seq(FormError("emptyDay", s"${benefitType.typeName}.$datePageName.error.empty.day.$userTypeValue"))
      case (false, false, false) => Seq()
    }
  }

  private def invalidDateFormatValidation(formData: DateFormData,
                                          benefitType: BenefitType,
                                          datePageName: String,
                                          isAgent: Boolean): Seq[FormError] = {
    Try(LocalDate.of(formData.year.toInt, formData.month.toInt, formData.day.toInt)).toOption match {
      case None => Seq(FormError("invalidOrNotAllowed", s"${benefitType.typeName}.$datePageName.error.invalid.date.${userType(isAgent)}"))
      case _ => Seq.empty
    }
  }

  private def userType(isAgent: Boolean): String = if (isAgent) "agent" else "individual"
}
