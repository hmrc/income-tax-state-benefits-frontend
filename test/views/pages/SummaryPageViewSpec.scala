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

package views.pages

import controllers.routes.ClaimsController
import models.BenefitType.JobSeekersAllowance
import models.requests.UserPriorDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.SummaryPageBuilder.aSummaryPage
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
    val jobSeekersAllowance: String
    val notStartedText: String
    val buttonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "State benefits"
    override val expectedHeading: String = "State benefits"
    override val jobSeekersAllowance: String = "Jobseeker’s Allowance"
    override val notStartedText: String = "Not started"
    override val buttonText: String = "Return to overview"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Budd-daliadau’r Wladwriaeth"
    override val expectedHeading: String = "Budd-daliadau’r Wladwriaeth"
    override val jobSeekersAllowance: String = "Lwfans Ceisio Gwaith"
    override val notStartedText: String = "Heb ddechrau"
    override val buttonText: String = "Yn ôl i’r trosolwg"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, Unit]] = Seq(
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY)
  )

  userScenarios.foreach { userScenario =>
    import Selectors._
    import userScenario.commonExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render summary page with multiple task list items" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(aSummaryPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        textOnPageCheck(jobSeekersAllowance, jobSeekersAllowanceSelector)
        linkCheck(jobSeekersAllowance, jobSeekersAllowanceLinkSelector, ClaimsController.show(taxYear, JobSeekersAllowance).url)
        textOnPageCheck(notStartedText, jobSeekersAllowanceStatusSelector)
        buttonCheck(buttonText, buttonSelector, Some(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
      }
    }
  }
}
