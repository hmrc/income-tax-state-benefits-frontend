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

import controllers.routes.SectionCompletedQuestionController
import forms.YesNoForm
import models.BenefitType.JobSeekersAllowance
import models.requests.UserPriorDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.SectionCompletedQuestionPageBuilder.aSectionCompletedQuestionPage
import views.html.pages.SectionCompletedQuestionPageView

class SectionCompletedQuestionPageViewSpec extends ViewUnitTest {

  private val underTest: SectionCompletedQuestionPageView = inject[SectionCompletedQuestionPageView]

  object Selectors {
    val continueButtonFormSelector = "#main-content > div > div > form"
    val errorHref = "#value"
    val buttonSelector: String = "#continue"
  }

  trait CommonExpectedResults {
    val expectedHeading: String
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedCaption: Int => String
    val expectedYesText: String
    val expectedNoText: String
    val expectedButtonText: String
    val expectedErrorText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedHeading: String = "Have you completed this section?"
    override val expectedTitle: String = expectedHeading
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Yes, I’ve completed this section"
    override val expectedNoText: String = "No, I’ll come back to it later"
    override val expectedButtonText: String = "Continue"
    override val expectedErrorText: String = "Select whether you’ve completed this section"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedHeading: String = "Have you completed this section?"
    override val expectedTitle: String = expectedHeading
    override val expectedErrorTitle: String = s"Error: $expectedTitle"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Yes, I’ve completed this section"
    override val expectedNoText: String = "No, I’ll come back to it later"
    override val expectedButtonText: String = "Continue"
    override val expectedErrorText: String = "Select whether you’ve completed this section"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, _]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, None),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, None)
  )

  userScenarios.foreach { userScenario =>
    import userScenario.commonExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)}" should {
      "render page with empty form" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(underTest(aSectionCompletedQuestionPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        fieldSetH1Check(userScenario.commonExpectedResults.expectedHeading)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)
        formPostLinkCheck(SectionCompletedQuestionController.submit(taxYearEOY, JobSeekersAllowance).url, Selectors.continueButtonFormSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with filled in form using selected 'Yes' value" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aSectionCompletedQuestionPage.copy(form = aSectionCompletedQuestionPage.form.fill(value = true))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = true)
      }

      "render page with empty selection error" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aSectionCompletedQuestionPage.copy(form = aSectionCompletedQuestionPage.form.bind(Map(YesNoForm.yesNo -> "")))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText)
      }
    }
  }
}
