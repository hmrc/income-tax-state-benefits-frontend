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

package views.pages.employmentsupport

import controllers.routes.StartDateController
import forms.{DateForm, DateFormData, FormsProvider}
import models.BenefitType.EmploymentSupportAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.StartDatePageBuilder.aStartDatePage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.StartDatePageView

import java.time.LocalDate

class StartDatePageViewSpec extends ViewUnitTest {

  private val formsProvider = new FormsProvider()
  private val dateForm = DateForm.dateForm()

  private val day: String = "day"
  private val month: String = "month"
  private val year: String = "year"

  private val underTest: StartDatePageView = inject[StartDatePageView]

  object Selectors {
    val formSelector: String = "#main-content > div > div > form"
    val hintSelector: String = "#value-for-hint"
    val inputDayField: String = s"#${DateForm.day}"
    val inputMonthField: String = s"#${DateForm.month}"
    val inputYearField: String = s"#${DateForm.year}"
    val buttonSelector: String = "#continue"
    val mustBeSameAsOrBeforeErrorHref: String = "#value-for-day"

    def invalidErrorHref(dayMonthOrYear: String): String = s"#value-for-$dayMonthOrYear"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedErrorTitle: String
    val expectedEmptyDayErrorText: String
    val expectedEmptyDayMonthErrorText: String
    val expectedEmptyDayYearErrorText: String
    val expectedEmptyMonthErrorText: String
    val expectedEmptyMonthYearErrorText: String
    val expectedEmptyYearErrorText: String
    val expectedAllFieldsEmptyErrorText: String
    val expectedInvalidDateErrorText: String

    def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String

    def expectedMustBeBeforeErrorText(date: String): String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHintText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Employment and Support Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = s"For example, 23 1 $taxYearEOY"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Employment and Support Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = s"For example, 23 1 $taxYearEOY"
    override val expectedButtonText: String = "Continue"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "When did you start getting Employment and Support Allowance?"
    override val expectedHeading: String = "When did you start getting Employment and Support Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedEmptyDayErrorText: String = "The date you started getting Employment and Support Allowance must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date you started getting Employment and Support Allowance must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date you started getting Employment and Support Allowance must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date you started getting Employment and Support Allowance must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date you started getting Employment and Support Allowance must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date you started getting Employment and Support Allowance must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date you started getting Employment and Support Allowance"
    override val expectedInvalidDateErrorText: String = "The date you started getting Employment and Support Allowance must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String = s"The date you started getting Employment and Support Allowance must be the same as or before 5 April $taxYear"

    override def expectedMustBeBeforeErrorText(date: String): String = s"The date you started getting Employment and Support Allowance must be before $date"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "When did you start getting Employment and Support Allowance?"
    override val expectedHeading: String = "When did you start getting Employment and Support Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedEmptyDayErrorText: String = "The date you started getting Employment and Support Allowance must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date you started getting Employment and Support Allowance must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date you started getting Employment and Support Allowance must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date you started getting Employment and Support Allowance must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date you started getting Employment and Support Allowance must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date you started getting Employment and Support Allowance must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date you started getting Employment and Support Allowance"
    override val expectedInvalidDateErrorText: String = "The date you started getting Employment and Support Allowance must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String = s"The date you started getting Employment and Support Allowance must be the same as or before 5 April $taxYear"

    override def expectedMustBeBeforeErrorText(date: String): String = s"The date you started getting Employment and Support Allowance must be before $date"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "When did your client start getting Employment and Support Allowance?"
    override val expectedHeading: String = "When did your client start getting Employment and Support Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedEmptyDayErrorText: String = "The date your client started getting Employment and Support Allowance must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date your client started getting Employment and Support Allowance must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date your client started getting Employment and Support Allowance must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date your client started getting Employment and Support Allowance must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date your client started getting Employment and Support Allowance must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date your client started getting Employment and Support Allowance must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date your client started getting Employment and Support Allowance"
    override val expectedInvalidDateErrorText: String = "The date your client started getting Employment and Support Allowance must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String = s"The date your client started getting Employment and Support Allowance must be the same as or before 5 April $taxYear"

    override def expectedMustBeBeforeErrorText(date: String): String = s"The date your client started getting Employment and Support Allowance must be before $date"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "When did your client start getting Employment and Support Allowance?"
    override val expectedHeading: String = "When did your client start getting Employment and Support Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedEmptyDayErrorText: String = "The date your client started getting Employment and Support Allowance must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date your client started getting Employment and Support Allowance must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date your client started getting Employment and Support Allowance must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date your client started getting Employment and Support Allowance must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date your client started getting Employment and Support Allowance must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date your client started getting Employment and Support Allowance must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date your client started getting Employment and Support Allowance"
    override val expectedInvalidDateErrorText: String = "The date your client started getting Employment and Support Allowance must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String = s"The date your client started getting Employment and Support Allowance must be the same as or before 5 April $taxYear"

    override def expectedMustBeBeforeErrorText(date: String): String = s"The date your client started getting Employment and Support Allowance must be before $date"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    import Selectors._
    import userScenario.commonExpectedResults._
    import userScenario.specificExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(underTest(aStartDatePage.copy(benefitType = EmploymentSupportAllowance)).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(get.expectedHeading, isFieldSetH1 = true)
        textOnPageCheck(expectedHintText, hintSelector)
        inputFieldValueCheck(DateForm.day, inputDayField, value = "")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
        inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        formPostLinkCheck(StartDateController.submit(taxYearEOY, EmploymentSupportAllowance, aStartDatePage.sessionDataId).url, formSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with pre-filled form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = dateForm.fill(DateFormData(LocalDate.of(taxYearEOY, 2, 1)))
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, benefitType = EmploymentSupportAllowance, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
        inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
      }

      "render page with empty date fields error" when {
        "no date was entered" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "", month = "", year = "")
          val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
          val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedAllFieldsEmptyErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        }

        "date with missing day" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "", month = "2", year = taxYearEOY.toString)
          val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
          val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyDayErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
          inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
        }

        "date with missing day and month" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "", month = "", year = taxYearEOY.toString)
          val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
          val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyDayMonthErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
        }

        "date with missing day and year" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "", month = "2", year = "")
          val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
          val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyDayYearErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
          inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        }

        "date with missing month" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "1", month = "", year = taxYearEOY.toString)
          val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
          val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyMonthErrorText, invalidErrorHref(month))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
        }

        "date with missing month and year" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "1", month = "", year = "")
          val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
          val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyMonthYearErrorText, invalidErrorHref(month))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        }

        "date with missing year" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "1", month = "2", year = "")
          val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
          val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyYearErrorText, invalidErrorHref(year))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
          inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        }
      }

      "render page with invalid date error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val formData = DateFormData(day = "1", month = "2", year = "yyyy")
        val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYearEOY, EmploymentSupportAllowance, userScenario.isAgent, None)
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(get.expectedInvalidDateErrorText, invalidErrorHref(day))
        inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
        inputFieldValueCheck(DateForm.year, inputYearField, value = "yyyy")
      }

      "render page with mustBeSameAsOrBefore error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val formData = DateFormData(day = "6", month = "4", year = taxYear.toString)
        val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYear, EmploymentSupportAllowance, userScenario.isAgent, None)
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(get.expectedMustBeSameAsOrBeforeErrorText(taxYear), mustBeSameAsOrBeforeErrorHref)
        inputFieldValueCheck(DateForm.day, inputDayField, value = "6")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "4")
        inputFieldValueCheck(DateForm.year, inputYearField, value = taxYear.toString)
      }

      "render page with mustBeBefore end date error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val endDate = LocalDate.of(taxYear, 1, 10)
        val formData = DateFormData(day = "11", month = "1", year = taxYear.toString)
        val pageForm = formsProvider.validatedStartDateForm(dateForm.fill(formData), taxYear, EmploymentSupportAllowance, userScenario.isAgent, Some(endDate))
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, EmploymentSupportAllowance, form = pageForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(get.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(get.expectedMustBeBeforeErrorText(translatedDateFormatter(endDate)), mustBeSameAsOrBeforeErrorHref)
        inputFieldValueCheck(DateForm.day, inputDayField, value = "11")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "1")
        inputFieldValueCheck(DateForm.year, inputYearField, value = taxYear.toString)
      }
    }
  }
}
