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

import controllers.jobseekers.routes.TaxPaidQuestionController
import forms.YesNoForm
import forms.jobseekers.FormsProvider
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.jobseekers.TaxPaidQuestionPageBuilder.aTaxPaidQuestionPage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.jobseekers.TaxPaidQuestionPageView

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
    val expectedHintText: String
    val expectedValueErrorText: (LocalDate, LocalDate) => String
    val expectedErrorText: (LocalDate, LocalDate) => String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedYesText: String
    val expectedNoText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedButtonText: String = "Continue"
  }


  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Error: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintText: String = "This amount will be on the P45 you got after your claim ended."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Select yes if you had any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Error: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintText: String = "This amount will be on the P45 you got after your claim ended."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Select yes if you had any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}"
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Error: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintText: String = "This amount will be on the P45 your client got after their claim ended."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Select yes if your client had any tax taken off their Jobseeker’s Allowance " +
      s"between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Error: ${expectedTitle(firstDate, secondDate)}"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedTitle(firstDate, secondDate)
    override val expectedHintText: String = "This amount will be on the P45 your client got after their claim ended."
    override val expectedValueErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => s"Select yes if your client had any tax taken off their Jobseeker’s Allowance " +
      s"between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}"
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate, secondDate) => expectedValueErrorText(firstDate, secondDate)
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
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aTaxPaidQuestionPage
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        fieldSetH1Check(userScenario.specificExpectedResults.get.expectedHeading(aTaxPaidQuestionPage.titleFirstDate, aTaxPaidQuestionPage.titleSecondDate))
        hintTextCheck(userScenario.specificExpectedResults.get.expectedHintText)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)
        formPostLinkCheck(TaxPaidQuestionController.submit(taxYearEOY, pageModel.sessionDataId).url, Selectors.continueButtonFormSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with filled in form using selected 'Yes' value" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aTaxPaidQuestionPage.copy(form = aTaxPaidQuestionPage.form.fill(value = true))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = true)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val claimCYAModel = aClaimCYAModel.copy(startDate = aTaxPaidQuestionPage.titleFirstDate, endDate = Some(aTaxPaidQuestionPage.titleSecondDate))
        val pageForm = new FormsProvider().taxTakenOffYesNoForm(userScenario.isAgent, aTaxPaidQuestionPage.taxYear, claimCYAModel)
        val pageModel = aTaxPaidQuestionPage.copy(form = pageForm.bind(Map(YesNoForm.yesNo -> "")))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedValueErrorText(pageModel.titleFirstDate, pageModel.titleSecondDate), Selectors.errorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText(pageModel.titleFirstDate, pageModel.titleSecondDate))
      }
    }
  }
}
