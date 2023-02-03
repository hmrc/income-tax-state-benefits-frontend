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
    val expectedHintText: String

    def expectedTitle(firstDate: LocalDate, secondDate: LocalDate): String

    def expectedErrorTitle(firstDate: LocalDate, secondDate: LocalDate): String

    def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String

    def expectedValueErrorText(firstDate: LocalDate, secondDate: LocalDate): String

    def expectedErrorText(firstDate: LocalDate, secondDate: LocalDate): String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedYesText: String
    val expectedNoText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Employment and Support Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Employment and Support Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedButtonText: String = "Continue"
  }


  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedHintText: String = "This amount will be on the P45 you got after your claim ended."

    override def expectedTitle(firstDate: LocalDate, secondDate: LocalDate): String = s"Did you have any tax taken off your Employment and Support Allowance between " +
      s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"

    override def expectedErrorTitle(firstDate: LocalDate, secondDate: LocalDate): String = s"Error: ${expectedTitle(firstDate, secondDate)}"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = expectedTitle(firstDate, secondDate)

    override def expectedValueErrorText(firstDate: LocalDate, secondDate: LocalDate): String = s"Select yes if you had any tax taken off your Employment and Support Allowance between " +
      s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"

    override def expectedErrorText(firstDate: LocalDate, secondDate: LocalDate): String = expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedHintText: String = "This amount will be on the P45 you got after your claim ended."

    override def expectedTitle(firstDate: LocalDate, secondDate: LocalDate): String =
      s"Did you have any tax taken off your Employment and Support Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"

    override def expectedErrorTitle(firstDate: LocalDate, secondDate: LocalDate): String = s"Error: ${expectedTitle(firstDate, secondDate)}"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = expectedTitle(firstDate, secondDate)

    override def expectedValueErrorText(firstDate: LocalDate, secondDate: LocalDate): String = s"Select yes if you had any tax taken off your Employment and Support Allowance between " +
      s"${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}"

    override def expectedErrorText(firstDate: LocalDate, secondDate: LocalDate): String = expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedHintText: String = "This amount will be on the P45 your client got after their claim ended."

    override def expectedTitle(firstDate: LocalDate, secondDate: LocalDate): String = s"Did your client have any tax taken off their Employment and Support Allowance between " +
      s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"

    override def expectedErrorTitle(firstDate: LocalDate, secondDate: LocalDate): String = s"Error: ${expectedTitle(firstDate, secondDate)}"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = expectedTitle(firstDate, secondDate)

    override def expectedValueErrorText(firstDate: LocalDate, secondDate: LocalDate): String = s"Select yes if your client had any tax taken off their Employment and Support Allowance " +
      s"between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"

    override def expectedErrorText(firstDate: LocalDate, secondDate: LocalDate): String = expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedHintText: String = "This amount will be on the P45 your client got after their claim ended."

    override def expectedTitle(firstDate: LocalDate, secondDate: LocalDate): String = s"Did your client have any tax taken off their Employment and Support Allowance between " +
      s"${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"

    override def expectedErrorTitle(firstDate: LocalDate, secondDate: LocalDate): String = s"Error: ${expectedTitle(firstDate, secondDate)}"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = expectedTitle(firstDate, secondDate)

    override def expectedValueErrorText(firstDate: LocalDate, secondDate: LocalDate): String = s"Select yes if your client had any tax taken off their Employment and Support Allowance " +
      s"between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}"

    override def expectedErrorText(firstDate: LocalDate, secondDate: LocalDate): String = expectedValueErrorText(firstDate, secondDate)
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    import userScenario.commonExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${userScenario.isAgent}" should {
      val pageModel = aTaxPaidQuestionPage.copy(benefitType = EmploymentSupportAllowance)
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading(pageModel.titleFirstDate, pageModel.titleSecondDate), isFieldSetH1 = true)
        hintTextCheck(userScenario.specificExpectedResults.get.expectedHintText)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)
        formPostLinkCheck(TaxPaidQuestionController.submit(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, Selectors.continueButtonFormSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with filled in form using selected 'Yes' value" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val page = pageModel.copy(form = pageModel.form.fill(value = true))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = true)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val claimCYAModel = aClaimCYAModel.copy(startDate = pageModel.titleFirstDate, endDate = Some(pageModel.titleSecondDate))
        val pageForm = new FormsProvider().taxTakenOffYesNoForm(pageModel.taxYear, EmploymentSupportAllowance, userScenario.isAgent, claimCYAModel)
        val page = pageModel.copy(form = pageForm.bind(Map(YesNoForm.yesNo -> "")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedValueErrorText(page.titleFirstDate, page.titleSecondDate), Selectors.errorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText(page.titleFirstDate, page.titleSecondDate))
      }
    }
  }
}
