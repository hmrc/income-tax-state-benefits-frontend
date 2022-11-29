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
import forms.jobseekers.FormsProvider
import forms.{DateForm, DateFormData}
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.jobseekers.EndDatePageBuilder.anEndDatePage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.jobseekers.EndDatePageView

import java.time.LocalDate

class EndDatePageViewSpec extends ViewUnitTest {

  private val formsProvider = new FormsProvider()

  private val underTest = inject[EndDatePageView]

  object Selectors {
    val formSelector: String = "#main-content > div > div > form"
    val hintSelector: String = "#value-for-hint"
    val inputDayField: String = s"#${DateForm.day}"
    val inputMonthField: String = s"#${DateForm.month}"
    val inputYearField: String = s"#${DateForm.year}"
    val buttonSelector: String = "#continue"
    val dateFormLevelErrorHref: String = "#value-for-day"
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
    val expectedHintText: String
    val expectedInvalidDateErrorText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "When did this claim end?"
    override val expectedHeading: String = expectedTitle
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, 23 3 2007"
    override val expectedInvalidDateErrorText: String = "Enter the date the Jobseeker’s Allowance ended must be a real date"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "When did this claim end?"
    override val expectedHeading: String = expectedTitle
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, 23 3 2007"
    override val expectedInvalidDateErrorText: String = "Enter the date the Jobseeker’s Allowance ended must be a real date"
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
        textOnPageCheck(userScenario.commonExpectedResults.expectedHintText, Selectors.hintSelector)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = "")
        formPostLinkCheck(EndDateController.submit(taxYearEOY, anEndDatePage.sessionDataId).url, Selectors.formSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with pre-filled form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.endDateForm(taxYear, userScenario.isAgent, aClaimCYAModel.startDate).fill(DateFormData(LocalDate.of(taxYearEOY, 2, 1)))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "2")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYearEOY.toString)
      }

      "render page with invalid date error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.endDateForm(taxYear, userScenario.isAgent, aClaimCYAModel.startDate).bind(Map(DateForm.day -> "dd", DateForm.month -> "mm", DateForm.year -> "yyyy"))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.commonExpectedResults.expectedInvalidDateErrorText, Selectors.dateFormLevelErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "dd")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "mm")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = "yyyy")
      }

      "render page with mustBeEndOfYear error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.endDateForm(taxYear, userScenario.isAgent, aClaimCYAModel.startDate).bind(Map(DateForm.day -> "6", DateForm.month -> "4", DateForm.year -> taxYear.toString))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedMustBeSameAsOrBeforeErrorText(taxYear), Selectors.dateFormLevelErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "6")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "4")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYear.toString)
      }

      "render page with mustBeAfterStartDate error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val startDate = LocalDate.of(taxYearEOY, 1, 1)

        val form = formsProvider.endDateForm(taxYearEOY, userScenario.isAgent, startDate).bind(Map(DateForm.day -> "1", DateForm.month -> "1", DateForm.year -> taxYearEOY.toString))
        val pageModel = anEndDatePage.copy(taxYear = taxYearEOY, form = form)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedMustBeAfterStartDateErrorText(startDate), Selectors.dateFormLevelErrorHref)
        inputFieldValueCheck(DateForm.day, Selectors.inputDayField, value = "1")
        inputFieldValueCheck(DateForm.month, Selectors.inputMonthField, value = "1")
        inputFieldValueCheck(DateForm.year, Selectors.inputYearField, value = taxYearEOY.toString)
      }
    }
  }
}
