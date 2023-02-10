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

import controllers.routes.AmountController
import forms.{AmountForm, FormsProvider}
import models.BenefitType.EmploymentSupportAllowance
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
    val expectedCaption: Int => String
    val expectedHintText: String
    val expectedLabelText: String
    val expectedButtonText: String
    val expectedEmptyAmountErrorText: String
    val expectedMaxAmountErrorText: String
    val expectedMoreThanZeroAmountErrorText: String
    val expectedIncorrectFormatAmountErrorText: String

    def expectedAmountMustBeMoreErrorText(amount: BigDecimal): String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Employment and Support Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of Employment and Support Allowance"
    override val expectedButtonText: String = "Continue"
    override val expectedEmptyAmountErrorText: String = "Enter the amount of Employment and Support Allowance"
    override val expectedMaxAmountErrorText: String = "The amount of Employment and Support Allowance must be less than £100,000,000,000"
    override val expectedMoreThanZeroAmountErrorText: String = "The amount of Employment and Support Allowance must be more than £0"
    override val expectedIncorrectFormatAmountErrorText: String = "The amount of Employment and Support Allowance must be a number"

    override def expectedAmountMustBeMoreErrorText(amount: BigDecimal): String = s"The amount of Employment and Support Allowance must be more than £$amount"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Employment and Support Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of Employment and Support Allowance"
    override val expectedButtonText: String = "Continue"
    override val expectedEmptyAmountErrorText: String = "Enter the amount of Employment and Support Allowance"
    override val expectedMaxAmountErrorText: String = "The amount of Employment and Support Allowance must be less than £100,000,000,000"
    override val expectedMoreThanZeroAmountErrorText: String = "The amount of Employment and Support Allowance must be more than £0"
    override val expectedIncorrectFormatAmountErrorText: String = "The amount of Employment and Support Allowance must be a number"

    override def expectedAmountMustBeMoreErrorText(amount: BigDecimal): String = s"The amount of Employment and Support Allowance must be more than £$amount"
  }

  trait SpecificExpectedResults {
    val expectedHeading: (LocalDate, LocalDate) => String
    val expectedTitle: (LocalDate, LocalDate) => String
    val expectedErrorTitle: (LocalDate, LocalDate) => String
    val expectedP1Text: String
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Employment and Support Allowance did your client get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 your client got after their claim ended. If they had tax taken off, enter the amount before tax."
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Employment and Support Allowance did your client get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 your client got after their claim ended. If they had tax taken off, enter the amount before tax."
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Employment and Support Allowance did you get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 you got after your claim ended. If you had tax taken off, enter the amount before tax."
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Employment and Support Allowance did you get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 you got after your claim ended. If you had tax taken off, enter the amount before tax."
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(IndividualSpecificExpectedEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(AgentSpecificExpectedEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(IndividualSpecificExpectedCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(AgentSpecificExpectedCY))
  )

  userScenarios.foreach { userScenario =>
    import userScenario.commonExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${userScenario.isAgent}" should {
      val pageModel = anAmountPage.copy(benefitType = EmploymentSupportAllowance, form = new FormsProvider().amountForm(EmploymentSupportAllowance))
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate))
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedP1Text, Selectors.paragraphTextSelector)
        amountBoxLabelCheck(userScenario.commonExpectedResults.expectedLabelText)
        amountBoxHintCheck(userScenario.commonExpectedResults.expectedHintText)
        formPostLinkCheck(AmountController.submit(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, Selectors.continueButtonFormSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = pageModel.copy(form = pageModel.form.bind(Map(AmountForm.amount -> "")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedEmptyAmountErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedEmptyAmountErrorText)
      }

      "render page with max amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = pageModel.copy(form = pageModel.form.bind(Map(AmountForm.amount -> "100,000,000,000")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedMaxAmountErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedMaxAmountErrorText)
      }

      "render page with must be more than zero amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = pageModel.copy(form = pageModel.form.bind(Map(AmountForm.amount -> "0")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedMoreThanZeroAmountErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedMoreThanZeroAmountErrorText)
      }

      "render page with incorrect format amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = pageModel.copy(form = pageModel.form.bind(Map(AmountForm.amount -> "abc")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedIncorrectFormatAmountErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedIncorrectFormatAmountErrorText)
      }

      "render page with amount must be more than ... error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = new FormsProvider().amountForm(EmploymentSupportAllowance, Some(10))
        val page = pageModel.copy(form = form.bind(Map(AmountForm.amount -> "10")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedAmountMustBeMoreErrorText(amount = 10), Selectors.errorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedAmountMustBeMoreErrorText(amount = 10))
      }
    }
  }
}
