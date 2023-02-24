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

import controllers.routes.AmountController
import forms.{AmountForm, FormsProvider}
import models.BenefitType.JobSeekersAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.AmountPageBuilder.anAmountPage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.AmountPageView

import java.time.LocalDate

class AmountPageViewSpec extends ViewUnitTest {

  private val underTest: AmountPageView = inject[AmountPageView]

  object Selectors {
    val paragraphTextSelector = "#main-content > div > div > p"
    val continueButtonFormSelector = "#main-content > div > div > form"
    val errorHref = "#amount"
    val buttonSelector: String = "#continue"
  }

  trait CommonExpectedResults {
    val expectedHintText: String
    val expectedLabelText: String
    val expectedButtonText: String
    val expectedEmptyAmountErrorText: String
    val expectedMaxAmountErrorText: String
    val expectedIncorrectFormatAmountErrorText: String
    val expectedEnterTaxText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of Jobseeker’s Allowance"
    override val expectedButtonText: String = "Continue"
    override val expectedEmptyAmountErrorText: String = "Enter the amount of Jobseeker’s Allowance"
    override val expectedMaxAmountErrorText: String = "The amount of Jobseeker’s Allowance must be less than £100,000,000,000"
    override val expectedIncorrectFormatAmountErrorText: String = "The amount of Jobseeker’s Allowance must be a number"
    override val expectedEnterTaxText: String = "Enter the amount before tax."
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of Jobseeker’s Allowance"
    override val expectedButtonText: String = "Continue"
    override val expectedEmptyAmountErrorText: String = "Enter the amount of Jobseeker’s Allowance"
    override val expectedMaxAmountErrorText: String = "The amount of Jobseeker’s Allowance must be less than £100,000,000,000"
    override val expectedIncorrectFormatAmountErrorText: String = "The amount of Jobseeker’s Allowance must be a number"
    override val expectedEnterTaxText: String = "Enter the amount before tax."
  }

  trait SpecificExpectedResults {
    val expectedHeading: (LocalDate, LocalDate) => String
    val expectedTitle: (LocalDate, LocalDate) => String
    val expectedErrorTitle: (LocalDate, LocalDate) => String
    val expectedP1P45Text: String
    val expectedP1P60Text: String

    def expectedMoreThanTaxErrorText(amount: BigDecimal): String
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did your client get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave your client."
    override val expectedP1P60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave your client."

    override def expectedMoreThanTaxErrorText(amount: BigDecimal): String = s"The amount of Jobseeker’s Allowance your client got must be more than the amount of tax taken off it, £$amount"
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did your client get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave your client."
    override val expectedP1P60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave your client."

    override def expectedMoreThanTaxErrorText(amount: BigDecimal): String = s"The amount of Jobseeker’s Allowance your client got must be more than the amount of tax taken off it, £$amount"
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did you get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave you."
    override val expectedP1P60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave you."

    override def expectedMoreThanTaxErrorText(amount: BigDecimal): String = s"The amount of Jobseeker’s Allowance you got must be more than the amount of tax taken off it, £$amount"
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did you get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave you."
    override val expectedP1P60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave you."

    override def expectedMoreThanTaxErrorText(amount: BigDecimal): String = s"The amount of Jobseeker’s Allowance you got must be more than the amount of tax taken off it, £$amount"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(IndividualSpecificExpectedEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(AgentSpecificExpectedEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(IndividualSpecificExpectedCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(AgentSpecificExpectedCY))
  )

  userScenarios.foreach { userScenario =>
    import Selectors._
    import userScenario.commonExpectedResults._
    import userScenario.specificExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${userScenario.isAgent}" should {
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(anAmountPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(anAmountPage.titleFirstDate, anAmountPage.titleSecondDate), userScenario.isWelsh)
        h1Check(get.expectedTitle(anAmountPage.titleFirstDate, anAmountPage.titleSecondDate))
        textOnPageCheck(s"${get.expectedP1P45Text} $expectedEnterTaxText", paragraphTextSelector)
        amountBoxLabelCheck(expectedLabelText)
        amountBoxHintCheck(expectedHintText)
        formPostLinkCheck(AmountController.submit(taxYearEOY, JobSeekersAllowance, anAmountPage.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with empty form and no tax paid" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(anAmountPage.copy(hasPaidTax = false)).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(anAmountPage.titleFirstDate, anAmountPage.titleSecondDate), userScenario.isWelsh)
        h1Check(get.expectedTitle(anAmountPage.titleFirstDate, anAmountPage.titleSecondDate))
        textOnPageCheck(s"${get.expectedP1P45Text}", paragraphTextSelector)
        amountBoxLabelCheck(expectedLabelText)
        amountBoxHintCheck(expectedHintText)
        formPostLinkCheck(AmountController.submit(taxYearEOY, JobSeekersAllowance, anAmountPage.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with empty form and the claim not ended in the tax year" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(anAmountPage.copy(hasEndDate = false)).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(anAmountPage.titleFirstDate, anAmountPage.titleSecondDate), userScenario.isWelsh)
        h1Check(get.expectedTitle(anAmountPage.titleFirstDate, anAmountPage.titleSecondDate))
        textOnPageCheck(s"${get.expectedP1P60Text} $expectedEnterTaxText", paragraphTextSelector)
        amountBoxLabelCheck(expectedLabelText)
        amountBoxHintCheck(expectedHintText)
        formPostLinkCheck(AmountController.submit(taxYearEOY, JobSeekersAllowance, anAmountPage.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = anAmountPage.copy(form = anAmountPage.form.bind(Map(AmountForm.amount -> "")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(anAmountPage.titleFirstDate, anAmountPage.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(expectedEmptyAmountErrorText, errorHref)
        errorAboveElementCheck(expectedEmptyAmountErrorText)
      }

      "render page with max amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = anAmountPage.copy(form = anAmountPage.form.bind(Map(AmountForm.amount -> "100,000,000,000")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(expectedMaxAmountErrorText, errorHref)
        errorAboveElementCheck(expectedMaxAmountErrorText)
      }

      "render page with incorrect format amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = anAmountPage.copy(form = anAmountPage.form.bind(Map(AmountForm.amount -> "abc")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(expectedIncorrectFormatAmountErrorText, errorHref)
        errorAboveElementCheck(expectedIncorrectFormatAmountErrorText)
      }

      "render page with amount must be more than tax error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val form = new FormsProvider().amountForm(JobSeekersAllowance, userScenario.isAgent, Some(10))
        val page = anAmountPage.copy(form = form.bind(Map(AmountForm.amount -> "10")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(get.expectedMoreThanTaxErrorText(amount = 10), errorHref)
        errorAboveElementCheck(get.expectedMoreThanTaxErrorText(amount = 10))
      }

      "render page with must be more than zero amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val form = new FormsProvider().amountForm(JobSeekersAllowance, userScenario.isAgent)
        val page = anAmountPage.copy(form = form.bind(Map(AmountForm.amount -> "0")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(get.expectedMoreThanTaxErrorText(amount = 0), errorHref)
        errorAboveElementCheck(get.expectedMoreThanTaxErrorText(amount = 0))
      }
    }
  }
}
