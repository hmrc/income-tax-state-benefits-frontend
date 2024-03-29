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

import controllers.routes.TaxPaidQuestionController
import forms.{FormsProvider, YesNoForm}
import models.BenefitType.EmploymentSupportAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.TaxPaidQuestionPageBuilder.aTaxPaidQuestionPage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.TaxPaidQuestionPageView

import java.time.LocalDate

class TaxPaidQuestionPageViewSpec extends ViewUnitTest {

  private val underTest: TaxPaidQuestionPageView = inject[TaxPaidQuestionPageView]

  object Selectors {
    val continueButtonFormSelector = "#main-content > div > div > form"
    val errorHref = "#value"
    val buttonSelector: String = "#continue"
  }

  trait SpecificExpectedResults {
    val expectedTitle: (LocalDate, LocalDate) => String
    val expectedErrorTitle: (LocalDate, LocalDate) => String
    val expectedHeading: (LocalDate, LocalDate) => String
    val expectedHintP45Text: String
    val expectedHintP60Text: String
    val expectedValueErrorText: (LocalDate, LocalDate) => String
    val expectedErrorText: (LocalDate, LocalDate) => String
  }

  trait CommonExpectedResults {
    val expectedYesText: String
    val expectedNoText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedButtonText: String = "Yn eich blaen"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did you have any tax taken off your Employment and Support Allowance between " +
        s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
          .replace("\u00A0", " ")
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Error: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintP45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave you."
    override val expectedHintP60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave you."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Select yes if you had any tax taken off your Employment and Support Allowance between " +
        s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
          .replace("\u00A0", " ")
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"A ddidynnwyd unrhyw dreth o’ch Lwfans Cyflogaeth a Chymorth rhwng ${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Gwall: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintP45Text: String = "Defnyddiwch y ffurflen P45(IB) neu’r ffurflen P45(U) a roddwyd i chi gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedHintP60Text: String = "Defnyddiwch y ffurflen P60(IB) neu’r ffurflen P60(U) a roddwyd i chi gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Dewiswch ‘Iawn’ os didynnwyd unrhyw dreth o’ch Lwfans Cyflogaeth a Chymorth rhwng " +
        s"${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}"
          .replace("\u00A0", " ")
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did your client have any tax taken off their Employment and Support Allowance between " +
        s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
          .replace("\u00A0", " ")
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Error: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintP45Text: String = "Use the P45(IB) or P45(U) that the Department for Work and Pensions (DWP) gave your client."
    override val expectedHintP60Text: String = "Use the P60(IB) or P60(U) that the Department for Work and Pensions (DWP) gave your client."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Select yes if your client had any tax taken off their Employment and Support Allowance " +
      s"between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
        .replace("\u00A0", " ")
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"A ddidynnwyd unrhyw dreth o Lwfans Cyflogaeth a Chymorth eich cleient rhwng " +
        s"${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
          .replace("\u00A0", " ")
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Gwall: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintP45Text: String = "Defnyddiwch y ffurflen P45(IB) neu’r ffurflen P45(U) a roddwyd i’ch cleient gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedHintP60Text: String = "Defnyddiwch y ffurflen P60(IB) neu’r ffurflen P60(U) a roddwyd i’ch cleient gan yr Adran Gwaith a Phensiynau (DWP)."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Dewiswch ‘Iawn’ os didynnwyd unrhyw dreth o Lwfans Cyflogaeth a Chymorth eich cleient rhwng " +
      s"${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}"
        .replace("\u00A0", " ")
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
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
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${userScenario.isAgent}" should {
      val pageModel = aTaxPaidQuestionPage.copy(benefitType = EmploymentSupportAllowance)
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        h1Check(get.expectedHeading(pageModel.titleFirstDate, pageModel.titleSecondDate), isFieldSetH1 = true)
        hintTextCheck(get.expectedHintP45Text)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(expectedNoText, radioNumber = 2, checked = false)
        formPostLinkCheck(TaxPaidQuestionController.submit(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with empty form and the claim not ended in the tax year" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(underTest(pageModel.copy(hasEndDate = false)).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        h1Check(get.expectedHeading(pageModel.titleFirstDate, pageModel.titleSecondDate), isFieldSetH1 = true)
        hintTextCheck(get.expectedHintP60Text)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(expectedNoText, radioNumber = 2, checked = false)
        formPostLinkCheck(TaxPaidQuestionController.submit(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with filled in form using selected 'Yes' value" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val page = pageModel.copy(form = pageModel.form.fill(value = true))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = true)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val claimCYAModel = aClaimCYAModel.copy(startDate = pageModel.titleFirstDate, endDate = Some(pageModel.titleSecondDate))
        val pageForm = new FormsProvider().taxTakenOffYesNoForm(pageModel.taxYear, EmploymentSupportAllowance, userScenario.isAgent, claimCYAModel)
        val page = pageModel.copy(form = pageForm.bind(Map(YesNoForm.yesNo -> "")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(expectedNoText, radioNumber = 2, checked = false)
        errorSummaryCheck(get.expectedValueErrorText(page.titleFirstDate, page.titleSecondDate), errorHref)
        errorAboveElementCheck(get.expectedErrorText(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)
      }
    }
  }
}
