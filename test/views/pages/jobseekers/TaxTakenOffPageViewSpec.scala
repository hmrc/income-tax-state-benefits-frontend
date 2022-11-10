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

import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.jobseekers.TaxTakenOffPageBuilder.aTaxTakenOffPage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.jobseekers.TaxTakenOffPageView

import java.time.LocalDate

class TaxTakenOffPageViewSpec extends ViewUnitTest {

  private val underTest: TaxTakenOffPageView = inject[TaxTakenOffPageView]

  object Selectors {
    val continueButtonFormSelector = "#main-content > div > div > form"
    val errorHref = "#value"
    val buttonSelector: String = "#continue"
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedButtonText: String
    val expectedYesText: String
    val expectedNoText: String
  }

  trait SpecificExpectedResults {
    val expectedTitle: (LocalDate, LocalDate) => String
    val expectedHeading: (LocalDate, LocalDate) => String
    val expectedErrorTitle: String
    val expectedHintText: String
    val expectedErrorText: (LocalDate, LocalDate) => String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedButtonText: String = "Continue"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedButtonText: String = "Continue"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedHintText: String = "This amount will be on the P45 you got after your claim ended."
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Select yes if you had any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedHintText: String = "This amount will be on the P45 you got after your claim ended."
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Select yes if you had any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedHintText: String = "This amount will be on the P45 your client got after their claim ended."
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Select yes if your client had any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedHeading: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedHintText: String = "This amount will be on the P45 your client got after their claim ended."
    override val expectedErrorText: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"Select yes if your client had any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}"
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
        implicit val document: Document = Jsoup.parse(underTest(aTaxTakenOffPage).body)

        welshToggleCheck(userScenario.isWelsh)
//        titleCheck(userScenario.specificExpectedResults.get.expectedTitle(aTaxTakenOffPage.titleFirstDate, aTaxTakenOffPage.titleSecondDate), userScenario.isWelsh)
//        captionCheck(expectedCaption(taxYearEOY))
        // Todo
        //        h1Check(userScenario.specificExpectedResults.get.expectedHeading(aTaxTakenOffPage.titleFirstDate, aTaxTakenOffPage.titleSecondDate), "userScenario.isWelsh")
        //        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        //        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)
        //        formPostLinkCheck(DidClaimEndInTaxYearController.submit(taxYearEOY, aTaxTakenOffPage.sessionDataId).url, Selectors.continueButtonFormSelector)
        //        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }
    }
  }
}
