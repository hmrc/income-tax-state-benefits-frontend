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

package views.pages

import controllers.routes.SummaryController
import models.requests.UserPriorDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.models.pages.SummaryPageBuilder.aSummaryPage
import views.html.pages.SummaryPageView

class SummaryPageViewSpec extends ViewUnitTest {

  private val page: SummaryPageView = inject[SummaryPageView]

  object Selectors {
    val jobSeekersAllowanceSelector: String = sectionNameSelector(1)
    val jobSeekersAllowanceLinkSelector = "#jobSeekersAllowance_link"
    val jobSeekersAllowanceStatusSelector: String = statusTagSelector(1)
    val buttonSelector = "#return-to-overview-button-id"

    def sectionNameSelector(index: Int): String = s"#main-content > div > div > ol > li:nth-child($index) > span.app-task-list__task-name"

    def statusTagSelector(index: Int): String = s"#main-content > div > div > ol > li:nth-child($index) > span.hmrc-status-tag"
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedAlternativeHeading: String => String
    val expectedCaption: Int => String

    val jobSeekersAllowance: String

    val notStartedText: String

    val buttonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "State benefits"
    override val expectedHeading: String = "State benefits"
    override val expectedAlternativeHeading: String => String = (employerRef: String) => s"Contractor: $employerRef"
    override val expectedCaption: Int => String = (taxYear: Int) => s"State benefits for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val jobSeekersAllowance: String = "Jobseeker’s Allowance"
    override val notStartedText: String = "Not started"
    override val buttonText: String = "Return to overview"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "State benefits"
    override val expectedHeading: String = "State benefits"
    override val expectedAlternativeHeading: String => String = (employerRef: String) => s"Contractwr: $employerRef"
    override val expectedCaption: Int => String = (taxYear: Int) => s"State benefits for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val jobSeekersAllowance: String = "Jobseeker’s Allowance"
    override val notStartedText: String = "Not started"
    override val buttonText: String = "Return to overview"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, Unit]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY)
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render summary page with multiple task list items" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(aSummaryPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)
        textOnPageCheck(userScenario.commonExpectedResults.jobSeekersAllowance, Selectors.jobSeekersAllowanceSelector)
        linkCheck(userScenario.commonExpectedResults.jobSeekersAllowance, Selectors.jobSeekersAllowanceLinkSelector, SummaryController.show(taxYear).url)
        textOnPageCheck(userScenario.commonExpectedResults.notStartedText, Selectors.jobSeekersAllowanceStatusSelector)
        buttonCheck(userScenario.commonExpectedResults.buttonText, Selectors.buttonSelector, Some(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
      }
    }
  }
}
