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

package views.pages.jobseekers

import controllers.jobseekers.routes.EndDateController
import forms.{DateForm, DateFormData}
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.jobseekers.EndDatePageBuilder.anEndDatePage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.jobseekers.EndDatePageView

import java.time.LocalDate

class EndDatePageViewSpec extends ViewUnitTest {

  private val underTest = inject[EndDatePageView]

  object Selectors {
    val formSelector: String = "#main-content > div > div > form"
    val inputDayField: String = s"#${DateForm.day}"
    val inputMonthField: String = s"#${DateForm.month}"
    val inputYearField: String = s"#${DateForm.year}"
    val buttonSelector: String = "#continue"
    val invalidDateErrorHref: String = "#invalidDate"
    val mustBeSameAsOrBeforeErrorHref: String = "#mustBeEndOfYear"
    val mustBeAfterStartDateErrorHref: String = "#mustBeAfterStartDate"
  }

  trait SpecificExpectedResults {
    val expectedMustBeSameAsOrBeforeErrorText: Int => String

    def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedErrorTitle: String
    val expectedCaption: Int => String
    val expectedInvalidDateErrorText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "When did this claim end?"
    override val expectedHeading: String = expectedTitle
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedInvalidDateErrorText: String = "Enter the date the Jobseeker’s Allowance claim ended"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "When did this claim end?"
    override val expectedHeading: String = expectedTitle
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedInvalidDateErrorText: String = "Enter the date the Jobseeker’s Allowance claim ended"
    override val expectedButtonText: String = "Continue"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String =
      (taxYear: Int) => s"The date your Jobseeker’s Allowance claim ended must be between 6 April ${taxYear - 1} and 5 April $taxYear"

    override def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String =
      s"The date your Jobseeker’s Allowance claim ended must be after ${translatedDateFormatter(startDate)}"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String =
      (taxYear: Int) => s"The date your Jobseeker’s Allowance claim ended must be between 6 April ${taxYear - 1} and 5 April $taxYear"

    override def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String =
      s"The date your Jobseeker’s Allowance claim ended must be after ${translatedDateFormatter(startDate)}"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String =
      (taxYear: Int) => s"The date your client’s Jobseeker’s Allowance claim ended must be between 6 April ${taxYear - 1} and 5 April $taxYear"

    override def expectedMustBeAfterStartDateErrorText(startDate: LocalDate)(implicit messages: Messages): String =
      s"The date your client’s Jobseeker’s Allowance claim ended must be after ${translatedDateFormatter(startDate)}"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String =
      (taxYear: Int) => s"The date your client’s Jobseeker’s Allowance claim ended must be between 6 April ${taxYear - 1} and 5 April $taxYear"

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
    import userScenario.commonExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(underTest(anEndDatePage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.commonExpectedResults.expectedHeading)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = "")
        formPostLinkCheck(EndDateController.submit(taxYearEOY, anEndDatePage.sessionDataId).url, Selectors.formSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with pre-filled form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = DateForm.dateForm().fill(DateFormData(LocalDate.of(taxYearEOY, 2, 1)))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "2")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYearEOY.toString)
      }

      "render page with invalid date error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = DateForm.dateForm().bind(Map(DateForm.day -> "dd", DateForm.month -> "mm", DateForm.year -> "yyyy"))
        val newForm = form.copy(errors = DateForm.validateEndDate(form.get, taxYearEOY, userScenario.isAgent, LocalDate.of(taxYearEOY, 1, 1)))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = newForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.commonExpectedResults.expectedInvalidDateErrorText, Selectors.invalidDateErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "dd")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "mm")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = "yyyy")
      }

      "render page with mustBeEndOfYear error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = DateForm.dateForm().bind(Map(DateForm.day -> "6", DateForm.month -> "4", DateForm.year -> taxYearEOY.toString))
        val newForm = form.copy(errors = DateForm.validateEndDate(form.get, taxYearEOY, userScenario.isAgent, LocalDate.of(taxYearEOY, 1, 1)))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = newForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedMustBeSameAsOrBeforeErrorText(taxYearEOY), Selectors.mustBeSameAsOrBeforeErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "6")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "4")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYearEOY.toString)
      }

      "render page with mustBeAfterStartDate error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val startDate = LocalDate.of(taxYearEOY, 1, 2)
        val endDate = LocalDate.of(taxYearEOY, 1, 1)

        val form = DateForm.dateForm().bind(Map(
          DateForm.day -> endDate.getDayOfMonth.toString,
          DateForm.month -> endDate.getMonthValue.toString,
          DateForm.year -> endDate.getYear.toString
        ))
        val newForm = form.copy(errors = DateForm.validateEndDate(form.get, taxYearEOY, userScenario.isAgent, startDate))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = newForm)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedMustBeAfterStartDateErrorText(startDate), Selectors.mustBeAfterStartDateErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "1")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYearEOY.toString)
      }
    }
  }
}
