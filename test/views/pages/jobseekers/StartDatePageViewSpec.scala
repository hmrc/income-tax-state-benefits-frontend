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

import controllers.routes.StartDateController
import forms.{DateForm, DateFormData, FormsProvider}
import models.BenefitType.JobSeekersAllowance
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

  private val underTest: StartDatePageView = inject[StartDatePageView]

  object Selectors {
    val formSelector: String = "#main-content > div > div > form"
    val hintSelector: String = "#value-for-hint"
    val inputDayField: String = s"#${DateForm.day}"
    val inputMonthField: String = s"#${DateForm.month}"
    val inputYearField: String = s"#${DateForm.year}"
    val buttonSelector: String = "#continue"
    val invalidDateErrorHref: String = "#value-for-day"
    val mustBeSameAsOrBeforeErrorHref: String = "#value-for-day"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedErrorTitle: String

    val expectedInvalidDateErrorText: String
    val expectedMustBeSameAsOrBeforeErrorText: Int => String
    val expectedMustBeBeforeErrorText: String => String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHintText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, 23 3 2007"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, 23 3 2007"
    override val expectedButtonText: String = "Continue"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedHeading: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedInvalidDateErrorText: String = "The date you started getting Jobseeker’s Allowance must be a real date"
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String = (taxYear: Int) => s"The date you started getting Jobseeker’s Allowance must be the same as or before 5 April $taxYear"
    override val expectedMustBeBeforeErrorText: String => String = (date: String) => s"The date you started getting Jobseeker’s Allowance must be before $date"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedHeading: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedInvalidDateErrorText: String = "The date you started getting Jobseeker’s Allowance must be a real date"
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String = (taxYear: Int) => s"The date you started getting Jobseeker’s Allowance must be the same as or before 5 April $taxYear"
    override val expectedMustBeBeforeErrorText: String => String = (date: String) => s"The date you started getting Jobseeker’s Allowance must be before $date"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedHeading: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedInvalidDateErrorText: String = "The date your client started getting Jobseeker’s Allowance must be a real date"
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String = (taxYear: Int) => s"The date your client started getting Jobseeker’s Allowance must be the same as or before 5 April $taxYear"
    override val expectedMustBeBeforeErrorText: String => String = (date: String) => s"The date your client started getting Jobseeker’s Allowance must be before $date"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedHeading: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedInvalidDateErrorText: String = "The date your client started getting Jobseeker’s Allowance must be a real date"
    override val expectedMustBeSameAsOrBeforeErrorText: Int => String = (taxYear: Int) => s"The date your client started getting Jobseeker’s Allowance must be the same as or before 5 April $taxYear"
    override val expectedMustBeBeforeErrorText: String => String = (date: String) => s"The date your client started getting Jobseeker’s Allowance must be before $date"
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

        implicit val document: Document = Jsoup.parse(underTest(aStartDatePage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading, isFieldSetH1 = true)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHintText, Selectors.hintSelector)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = "")
        formPostLinkCheck(StartDateController.submit(taxYearEOY, JobSeekersAllowance, aStartDatePage.sessionDataId).url, Selectors.formSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with pre-filled form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.startDateForm(taxYear, JobSeekersAllowance, userScenario.isAgent).fill(DateFormData(LocalDate.of(taxYearEOY, 2, 1)))
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "2")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYearEOY.toString)
      }

      "render page with invalid date error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.startDateForm(taxYear, JobSeekersAllowance, userScenario.isAgent).bind(Map(DateForm.day -> "dd", DateForm.month -> "mm", DateForm.year -> "yyyy"))
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedInvalidDateErrorText, Selectors.invalidDateErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "dd")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "mm")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = "yyyy")
      }

      "render page with mustBeSameAsOrBefore error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.startDateForm(taxYear, JobSeekersAllowance, userScenario.isAgent).bind(Map(DateForm.day -> "6", DateForm.month -> "4", DateForm.year -> taxYear.toString))
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedMustBeSameAsOrBeforeErrorText(taxYear), Selectors.mustBeSameAsOrBeforeErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "6")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "4")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYear.toString)
      }

      "render page with mustBeBefore error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val endDate = LocalDate.of(taxYear, 1, 10)
        val form = formsProvider.startDateForm(taxYear, JobSeekersAllowance, userScenario.isAgent, Some(endDate))
          .bind(Map(DateForm.day -> "11", DateForm.month -> "1", DateForm.year -> taxYear.toString))
        val pageModel = aStartDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedMustBeBeforeErrorText(translatedDateFormatter(endDate)), Selectors.mustBeSameAsOrBeforeErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "11")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "1")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYear.toString)
      }
    }
  }
}
