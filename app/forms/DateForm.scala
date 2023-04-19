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
import utils.ViewUtils.{translatedDateFormatter, translatedTaxYearEndDateFormatter}

import java.time.LocalDate
import java.time.Month.{APRIL, JANUARY}
import scala.util.Try

object DateForm extends InputFilters {

  private val ONE = 1
  private val SIX = 6
  private val NINETEEN_HUNDRED = 1900

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
                       (implicit messages: Messages): Seq[FormError] =
    (for {
      date <- dateOrAllCommonValidation(formData, benefitType, datePageName = "startDatePage", isAgent)
      _ <- startDateSpecificValidation(date, taxYear, benefitType, isAgent, endDate).toLeft(())
    } yield None).left.toSeq

  def validateEndDate(formData: DateFormData,
                      taxYear: Int,
                      benefitType: BenefitType,
                      isAgent: Boolean,
                      startDate: LocalDate)
                     (implicit messages: Messages): Seq[FormError] =
    (for {
      date <- dateOrAllCommonValidation(formData, benefitType, datePageName = "endDatePage", isAgent)
      _ <- endDateSpecificValidation(date, taxYear, benefitType, isAgent, startDate).toLeft(())
    } yield None).left.toSeq

  private def dateOrAllCommonValidation(formData: DateFormData,
                                        benefitType: BenefitType,
                                        datePageName: String,
                                        isAgent: Boolean): Either[FormError, LocalDate] =
    for {
      _ <- emptyDateFieldsValidation(formData, benefitType, datePageName, isAgent).toLeft(())
      date <- dateOrInvalidDateFormatValidation(formData, benefitType, datePageName, isAgent)
      _ <- commonDateValidation(date, benefitType, datePageName, isAgent).toLeft(())
    } yield date

  private def commonDateValidation(date: LocalDate,
                                   benefitType: BenefitType,
                                   datePageName: String,
                                   isAgent: Boolean): Option[FormError] = {

    val year = date.getYear
    val has4DigitYear = year >= 1000 && year < 10000

    val mustHave4DigitYearErrorMessage = s"${benefitType.typeName}.$datePageName.error.mustHave4DigitYear.${userType(isAgent)}"

    Option.when(!has4DigitYear)(FormError("invalidOrNotAllowed", mustHave4DigitYearErrorMessage))
  }

  private def startDateSpecificValidation(startDate: LocalDate,
                                          taxYear: Int,
                                          benefitType: BenefitType,
                                          isAgent: Boolean,
                                          endDate: Option[LocalDate])
                                         (implicit messages: Messages): Option[FormError] = {

    val isAfter1900 = startDate.isAfter(LocalDate.of(NINETEEN_HUNDRED, JANUARY, ONE))
    val isBeforeEOY = startDate.isBefore(LocalDate.of(taxYear, APRIL, SIX))
    val isBeforeDate = startDate.isBefore(endDate.getOrElse(startDate.plusDays(1)))

    lazy val mustBeAfter1900ErrorMessage = s"${benefitType.typeName}.startDatePage.error.mustBeAfter1900.${userType(isAgent)}"
    lazy val mustBeSameAsOrBeforeErrorMessage = s"${benefitType.typeName}.startDatePage.error.mustBeSameAsOrBefore.date.${userType(isAgent)}"
    lazy val mustBeBeforeErrorMessage = s"${benefitType.typeName}.startDatePage.error.mustBeBefore.date.${userType(isAgent)}"

    (isAfter1900, isBeforeEOY, isBeforeDate) match {
      case (false, _, _) => Some(FormError("invalidOrNotAllowed", mustBeAfter1900ErrorMessage))
      case (true, false, _) => Some(FormError("invalidOrNotAllowed", mustBeSameAsOrBeforeErrorMessage, Seq(translatedTaxYearEndDateFormatter(taxYear))))
      case (true, _, false) => Some(FormError("invalidOrNotAllowed", mustBeBeforeErrorMessage, Seq(translatedDateFormatter(endDate.get))))
      case _ => None
    }
  }

  private def endDateSpecificValidation(endDate: LocalDate,
                                        taxYear: Int,
                                        benefitType: BenefitType,
                                        isAgent: Boolean,
                                        startDate: LocalDate)
                                       (implicit messages: Messages): Option[FormError] = {
    val isAfterStartDate = endDate.isAfter(startDate)
    val isBeforeEOY = endDate.isBefore(LocalDate.of(taxYear, APRIL, SIX))

    lazy val mustBeEndOfYearErrorMessage = s"${benefitType.typeName}.endDatePage.error.mustBeEndOfYear.${userType(isAgent)}"
    lazy val mustBeAfterStartDateErrorMessage = s"${benefitType.typeName}.endDatePage.error.mustBeAfterStartDate.${userType(isAgent)}"

    (isAfterStartDate, isBeforeEOY) match {
      case (false, _) => Some(FormError("invalidOrNotAllowed", mustBeAfterStartDateErrorMessage, Seq(translatedDateFormatter(startDate))))
      case (true, false) => Some(FormError("invalidOrNotAllowed", mustBeEndOfYearErrorMessage, Seq(translatedTaxYearEndDateFormatter(taxYear))))
      case _ => None
    }
  }

  private def emptyDateFieldsValidation(formData: DateFormData,
                                        benefitType: BenefitType,
                                        datePageName: String,
                                        isAgent: Boolean): Option[FormError] = {
    lazy val userTypeValue = userType(isAgent)

    (formData.day.isEmpty, formData.month.isEmpty, formData.year.isEmpty) match {
      case (true, true, true) => Some(FormError(s"emptyAll", s"${benefitType.typeName}.$datePageName.error.empty.all.$userTypeValue"))
      case (true, true, false) => Some(FormError("emptyDayMonth", s"${benefitType.typeName}.$datePageName.error.empty.dayMonth.$userTypeValue"))
      case (true, false, true) => Some(FormError("emptyDayYear", s"${benefitType.typeName}.$datePageName.error.empty.dayYear.$userTypeValue"))
      case (false, true, true) => Some(FormError("emptyMonthYear", s"${benefitType.typeName}.$datePageName.error.empty.monthYear.$userTypeValue"))
      case (false, false, true) => Some(FormError("emptyYear", s"${benefitType.typeName}.$datePageName.error.empty.year.$userTypeValue"))
      case (false, true, false) => Some(FormError("emptyMonth", s"${benefitType.typeName}.$datePageName.error.empty.month.$userTypeValue"))
      case (true, false, false) => Some(FormError("emptyDay", s"${benefitType.typeName}.$datePageName.error.empty.day.$userTypeValue"))
      case (false, false, false) => None
    }
  }

  private def dateOrInvalidDateFormatValidation(formData: DateFormData,
                                                benefitType: BenefitType,
                                                datePageName: String,
                                                isAgent: Boolean): Either[FormError, LocalDate] = {
    Try(LocalDate.of(formData.year.toInt, formData.month.toInt, formData.day.toInt)).toOption match {
      case None => Left(FormError("invalidOrNotAllowed", s"${benefitType.typeName}.$datePageName.error.invalid.date.${userType(isAgent)}"))
      case Some(date) => Right(date)
    }
  }

  private def userType(isAgent: Boolean): String = if (isAgent) "agent" else "individual"
}
