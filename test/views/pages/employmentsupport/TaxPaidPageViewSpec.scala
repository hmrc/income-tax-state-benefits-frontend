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

import controllers.routes.TaxPaidController
import forms.{AmountForm, FormsProvider}
import models.BenefitType.EmploymentSupportAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.TaxPaidPageBuilder.aTaxPaidPage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.TaxPaidPageView

import java.time.LocalDate

class TaxPaidPageViewSpec extends ViewUnitTest {

  private val underTest: TaxPaidPageView = inject[TaxPaidPageView]

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
    val expectedZeroOrLessErrorText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of tax taken off"
    override val expectedButtonText: String = "Continue"
    override val expectedZeroOrLessErrorText: String = "The amount of tax paid must be more than £0"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedHintText: String = "Er enghraifft, £123.56"
    override val expectedLabelText: String = "Swm y dreth a ddidynnwyd"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedZeroOrLessErrorText: String = "Mae’n rhaid i swm y dreth a dalwyd fod yn fwy na £0"
  }

  trait SpecificExpectedResults {
    val expectedTitle: (LocalDate, LocalDate) => String
    val expectedErrorTitle: (LocalDate, LocalDate) => String
    val expectedP1P45Text: String
    val expectedP1P60Text: String
    val expectedErrorText: String

    def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String

    def expectedTaxExceedsAmountErrorText(amount: BigDecimal): String
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave your client."
    override val expectedP1P60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave your client."
    override val expectedErrorText: String = "Enter the amount of tax taken off your client’s Employment and Support Allowance"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = s"How much tax was taken off your client’s Employment and Support Allowance between " +
      s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
        .replace("\u00A0", " ")

    override def expectedTaxExceedsAmountErrorText(amount: BigDecimal): String =
      s"The amount of tax taken off must be less than the amount of Employment and Support Allowance your client got, £$amount"
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Gwall: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Defnyddiwch y ffurflen P45(IB) neu’r ffurflen P45(U) a roddwyd i’ch cleient gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedP1P60Text: String = "Defnyddiwch y ffurflen P60(IB) neu’r ffurflen P60(U) a roddwyd i’ch cleient gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedErrorText: String = "Nodwch swm y dreth a ddidynnwyd o Lwfans Cyflogaeth a Chymorth eich cleient"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = s"Faint o dreth a gafodd ei didynnu o Lwfans Cyflogaeth a Chymorth eich cleient rhwng " +
      s"${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")

    override def expectedTaxExceedsAmountErrorText(amount: BigDecimal): String =
      s"Mae’n rhaid i swm y dreth a ddidynnwyd fod yn llai na swm y Lwfans Cyflogaeth a Chymorth a gafodd eich cleient, sef £$amount"
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave you."
    override val expectedP1P60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave you."
    override val expectedErrorText: String = "Enter the amount of tax taken off your Employment and Support Allowance"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String =
      s"How much tax was taken off your Employment and Support Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
        .replace("\u00A0", " ")

    override def expectedTaxExceedsAmountErrorText(amount: BigDecimal): String = s"The amount of tax taken off must be less than the amount of Employment and Support Allowance you got, £$amount"
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Gwall: " + expectedHeading(startDate, endDate)
    override val expectedP1P45Text: String = "Defnyddiwch y ffurflen P45(IB) neu’r ffurflen P45(U) a roddwyd i chi gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedP1P60Text: String = "Defnyddiwch y ffurflen P60(IB) neu’r ffurflen P60(U) a roddwyd i chi gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedErrorText: String = "Nodwch swm y dreth a ddidynnwyd o’ch Lwfans Cyflogaeth a Chymorth"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String =
      s"Faint o dreth a gafodd ei didynnu o’ch Lwfans Cyflogaeth a Chymorth rhwng ${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")

    override def expectedTaxExceedsAmountErrorText(amount: BigDecimal): String = s"Mae’n rhaid i swm y dreth a ddidynnwyd fod yn llai na swm y Lwfans Cyflogaeth a Chymorth a gawsoch, sef £$amount"
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
      val pageForm = new FormsProvider().taxPaidAmountForm(EmploymentSupportAllowance, isAgent = userScenario.isAgent, maxAmount = aClaimCYAModel.amount.get - 1)
      val pageModel = aTaxPaidPage.copy(benefitType = EmploymentSupportAllowance, form = pageForm)
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        h1Check(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate))
        textOnPageCheck(get.expectedP1P45Text, paragraphTextSelector)
        amountBoxLabelCheck(expectedLabelText)
        amountBoxHintCheck(expectedHintText)
        formPostLinkCheck(TaxPaidController.submit(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with empty form and the claim not ended in the tax year" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(pageModel.copy(hasEndDate = false)).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        h1Check(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate))
        textOnPageCheck(get.expectedP1P60Text, paragraphTextSelector)
        amountBoxLabelCheck(expectedLabelText)
        amountBoxHintCheck(expectedHintText)
        formPostLinkCheck(TaxPaidController.submit(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = pageModel.copy(form = pageModel.form.bind(Map(AmountForm.amount -> "")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(get.expectedErrorText, errorHref)
        errorAboveElementCheck(get.expectedErrorText, userScenario.isWelsh)
      }

      "render page with tax must be less than amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = new FormsProvider().taxPaidAmountForm(EmploymentSupportAllowance, userScenario.isAgent, maxAmount = 10)
        val page = pageModel.copy(form = form.bind(Map(AmountForm.amount -> "10")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(get.expectedTaxExceedsAmountErrorText(amount = 10), errorHref)
        errorAboveElementCheck(get.expectedTaxExceedsAmountErrorText(amount = 10), userScenario.isWelsh)
      }

      "render page with must be more than zero amount error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = pageModel.copy(form = pageModel.form.bind(Map(AmountForm.amount -> "0")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(expectedZeroOrLessErrorText, errorHref)
        errorAboveElementCheck(expectedZeroOrLessErrorText, userScenario.isWelsh)
      }
    }
  }
}
