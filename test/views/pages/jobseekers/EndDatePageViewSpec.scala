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

package views.pages.jobseekers

import controllers.routes.EndDateController
import forms.{DateForm, DateFormData, FormsProvider}
import models.BenefitType.JobSeekersAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.EndDatePageBuilder.anEndDatePage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.EndDatePageView

import java.time.LocalDate

class EndDatePageViewSpec extends ViewUnitTest {

  private val formsProvider = new FormsProvider()
  private val dateForm = DateForm.dateForm()

  private val day: String = "day"
  private val month: String = "month"
  private val year: String = "year"

  private val underTest = inject[EndDatePageView]

  object Selectors {
    val formSelector: String = "#main-content > div > div > form"
    val hintSelector: String = "#value-for-hint"
    val inputDayField: String = s"#${DateForm.day}"
    val inputMonthField: String = s"#${DateForm.month}"
    val inputYearField: String = s"#${DateForm.year}"
    val buttonSelector: String = "#continue"
    val dateFormLevelErrorHref: String = "#value-for-day"

    def invalidErrorHref(dayMonthOrYear: String): String = s"#value-for-$dayMonthOrYear"
  }

  trait SpecificExpectedResults {
    val expectedEmptyDayErrorText: String
    val expectedEmptyDayMonthErrorText: String
    val expectedEmptyDayYearErrorText: String
    val expectedEmptyMonthErrorText: String
    val expectedEmptyMonthYearErrorText: String
    val expectedEmptyYearErrorText: String
    val expectedAllFieldsEmptyErrorText: String
    val expectedInvalidDateErrorText: String

    def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String

    def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedErrorTitle: String
    val expectedHintText: String
    val expectedInvalidDateErrorText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "When did this claim end?"
    override val expectedHeading: String = expectedTitle
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedHintText: String = s"For example, 23 1 $taxYearEOY"
    override val expectedInvalidDateErrorText: String = "Enter the date the Jobseeker’s Allowance ended must be a real date"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "When did this claim end?"
    override val expectedHeading: String = expectedTitle
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedHintText: String = s"For example, 23 1 $taxYearEOY"
    override val expectedInvalidDateErrorText: String = "Enter the date the Jobseeker’s Allowance ended must be a real date"
    override val expectedButtonText: String = "Continue"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedEmptyDayErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date your Jobseeker’s Allowance claim ended"
    override val expectedInvalidDateErrorText: String = "The date your Jobseeker’s Allowance claim ended must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String =
      s"The date your Jobseeker’s Allowance claim ended must be the same as or before 5 April $taxYear"

    override def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String =
      s"The date your Jobseeker’s Allowance claim ended must be after ${translatedDateFormatter(startDate)}"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedEmptyDayErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date your Jobseeker’s Allowance claim ended must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date your Jobseeker’s Allowance claim ended"
    override val expectedInvalidDateErrorText: String = "The date your Jobseeker’s Allowance claim ended must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String =
      s"The date your Jobseeker’s Allowance claim ended must be the same as or before 5 April $taxYear"

    override def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String =
      s"The date your Jobseeker’s Allowance claim ended must be after ${translatedDateFormatter(startDate)}"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedEmptyDayErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date your client’s Jobseeker’s Allowance claim ended"
    override val expectedInvalidDateErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String =
      s"The date your client’s Jobseeker’s Allowance claim ended must be the same as or before 5 April $taxYear"

    override def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String =
      s"The date your client’s Jobseeker’s Allowance claim ended must be after ${translatedDateFormatter(startDate)}"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedEmptyDayErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a day"
    override val expectedEmptyDayMonthErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a day and month"
    override val expectedEmptyDayYearErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a day and year"
    override val expectedEmptyMonthErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a month"
    override val expectedEmptyMonthYearErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a month and year"
    override val expectedEmptyYearErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must include a year"
    override val expectedAllFieldsEmptyErrorText: String = "Enter the date your client’s Jobseeker’s Allowance claim ended"
    override val expectedInvalidDateErrorText: String = "The date your client’s Jobseeker’s Allowance claim ended must be a real date"

    override def expectedMustBeSameAsOrBeforeErrorText(taxYear: Int): String =
      s"The date your client’s Jobseeker’s Allowance claim ended must be the same as or before 5 April $taxYear"

    override def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String =
      s"The date your client’s Jobseeker’s Allowance claim ended must be after ${translatedDateFormatter(startDate)}"
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

        implicit val document: Document = Jsoup.parse(underTest(anEndDatePage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading, isFieldSetH1 = true)
        textOnPageCheck(expectedHintText, hintSelector)
        inputFieldValueCheck(DateForm.day, inputDayField, value = "")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
        inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        formPostLinkCheck(EndDateController.submit(taxYearEOY, JobSeekersAllowance, anEndDatePage.sessionDataId).url, formSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with pre-filled form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = dateForm.fill(DateFormData(LocalDate.of(taxYearEOY, 2, 1)))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = form)
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
          val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
          val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, JobSeekersAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedAllFieldsEmptyErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        }

        "date with missing day" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "", month = "2", year = taxYearEOY.toString)
          val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
          val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, JobSeekersAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyDayErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
          inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
        }

        "date with missing day and month" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "", month = "", year = taxYearEOY.toString)
          val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
          val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, JobSeekersAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyDayMonthErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
        }

        "date with missing day and year" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "", month = "2", year = "")
          val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
          val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, JobSeekersAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyDayYearErrorText, invalidErrorHref(day))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
          inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        }

        "date with missing month" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "1", month = "", year = taxYearEOY.toString)
          val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
          val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, JobSeekersAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyMonthErrorText, invalidErrorHref(month))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
        }

        "date with missing month and year" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "1", month = "", year = "")
          val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
          val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, JobSeekersAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(expectedErrorTitle, userScenario.isWelsh)
          errorSummaryCheck(get.expectedEmptyMonthYearErrorText, invalidErrorHref(month))
          inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
          inputFieldValueCheck(DateForm.month, inputMonthField, value = "")
          inputFieldValueCheck(DateForm.year, inputYearField, value = "")
        }

        "date with missing year" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val formData = DateFormData(day = "1", month = "2", year = "")
          val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
          val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, JobSeekersAllowance, form = pageForm)
          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          titleCheck(expectedErrorTitle, userScenario.isWelsh)
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
        val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYearEOY, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, benefitType = JobSeekersAllowance, form = pageForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(get.expectedInvalidDateErrorText, invalidErrorHref(day))
        inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "2")
        inputFieldValueCheck(DateForm.year, inputYearField, value = "yyyy")
      }

      "render page with mustBeEndOfYear error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)


        val formData = DateFormData(day = "6", month = "4", year = taxYear.toString)
        val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYear, JobSeekersAllowance, userScenario.isAgent, aClaimCYAModel.startDate)
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, benefitType = JobSeekersAllowance, form = pageForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(get.expectedMustBeSameAsOrBeforeErrorText(taxYear), dateFormLevelErrorHref)
        inputFieldValueCheck(DateForm.day, inputDayField, value = "6")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "4")
        inputFieldValueCheck(DateForm.year, inputYearField, value = taxYear.toString)
      }

      "render page with mustBeAfterStartDate error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val startDate = LocalDate.of(taxYearEOY, 1, 1)

        val formData = DateFormData(day = "1", month = "1", year = taxYearEOY.toString)
        val pageForm = formsProvider.validatedEndDateForm(dateForm.fill(formData), taxYear, JobSeekersAllowance, userScenario.isAgent, startDate)
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, benefitType = JobSeekersAllowance, form = pageForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(get.expectedMustBeAfterStartDateErrorText(startDate), dateFormLevelErrorHref)
        inputFieldValueCheck(DateForm.day, inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, inputMonthField, value = "1")
        inputFieldValueCheck(DateForm.year, inputYearField, value = taxYearEOY.toString)
      }
    }
  }
}
