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

import controllers.routes.TaxPaidController
import forms.AmountForm
import models.BenefitType.JobSeekersAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
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
    val expectedCaption: Int => String
    val expectedHintText: String
    val expectedLabelText: String
    val expectedButtonText: String
    val expectedErrorText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of tax taken off"
    override val expectedButtonText: String = "Continue"
    override val expectedErrorText: String = "empty error"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of tax taken off"
    override val expectedButtonText: String = "Continue"
    override val expectedErrorText: String = "empty error"
  }

  trait SpecificExpectedResults {
    val expectedHeading: (LocalDate, LocalDate) => String
    val expectedTitle: (LocalDate, LocalDate) => String
    val expectedErrorTitle: (LocalDate, LocalDate) => String
    val expectedP1Text: String
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much tax was taken off your client’s Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 your client got after their claim ended."
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much tax was taken off your client’s Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 your client got after their claim ended."
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much tax was taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 you got after your claim ended."
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much tax was taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 you got after your claim ended."
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
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(aTaxPaidPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle(aTaxPaidPage.titleFirstDate, aTaxPaidPage.titleSecondDate), userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle(aTaxPaidPage.titleFirstDate, aTaxPaidPage.titleSecondDate))
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedP1Text, Selectors.paragraphTextSelector)
        amountBoxLabelCheck(userScenario.commonExpectedResults.expectedLabelText)
        amountBoxHintCheck(userScenario.commonExpectedResults.expectedHintText)
        formPostLinkCheck(TaxPaidController.submit(taxYearEOY, JobSeekersAllowance, aTaxPaidPage.sessionDataId).url, Selectors.continueButtonFormSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val pageModel = aTaxPaidPage.copy(form = aTaxPaidPage.form.bind(Map(AmountForm.amount -> "")))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle(aTaxPaidPage.titleFirstDate, aTaxPaidPage.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText)
      }
    }
  }
}
