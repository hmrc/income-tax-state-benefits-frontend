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

import controllers.jobseekers.routes.JobSeekersAllowanceController
import models.requests.UserPriorDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.models.pages.jobseekers.JobSeekersAllowancePageBuilder.aJobSeekersAllowancePage
import support.builders.models.pages.jobseekers.elements.BenefitSummaryListRowDataBuilder.aBenefitSummaryListRowData
import views.html.pages.jobseekers.JobSeekersAllowancePageView

import java.time.LocalDate

class JobSeekersAllowancePageViewSpec extends ViewUnitTest {

  private val page: JobSeekersAllowancePageView = inject[JobSeekersAllowancePageView]

  object Selectors {
    val summaryListRowSelector: Int => String = (row: Int) => s".govuk-summary-list > div:nth-child($row) > div.govuk-summary-list__row"
    val summaryListRowViewLinkSelector: Int => String = (row: Int) => s".govuk-summary-list > div:nth-child($row) > div.govuk-summary-list__row > dd > a"
    val summaryListRowRemovedSelector: Int => String = (row: Int) => s".govuk-summary-list > div:nth-child($row) > div > p"
    val addMissingClaimLinkSelector = "p > a#add-missing-claim-link"
    val buttonSelector = "#continue-button-id"
  }

  trait SpecificExpectedResults {
    val expectedSummaryListRowRemovedText: String
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedCaption: Int => String
    val expectedViewLinkText: String
    val expectedSummaryListRow1Text: String
    val expectedSummaryListRow2Text: String
    val expectedAddMissingClaimLinkText: String
    val expectedButtonText: String

  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Jobseeker’s Allowance"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedViewLinkText: String = "View"
    override val expectedSummaryListRow1Text = "£100 1 January 2022 to 31 January 2022 View"
    override val expectedSummaryListRow2Text = "£200.20 1 February 2022 to 5 April 2022 View"
    override val expectedAddMissingClaimLinkText: String = "Add missing claim"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Jobseeker’s Allowance"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedViewLinkText: String = "View"
    override val expectedSummaryListRow1Text = "£100 1 Ionawr 2022 to 31 Ionawr 2022 View"
    override val expectedSummaryListRow2Text = "£200.20 1 Chwefror 2022 to 5 Ebrill 2022 View"
    override val expectedAddMissingClaimLinkText: String = "Add missing claim"
    override val expectedButtonText: String = "Continue"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedSummaryListRowRemovedText: String = "You have removed this claim and it will not be included in your return."
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedSummaryListRowRemovedText: String = "You have removed this claim and it will not be included in your return."
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedSummaryListRowRemovedText: String = "You have removed this claim and it will not be included in your client’s return."
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedSummaryListRowRemovedText: String = "You have removed this claim and it will not be included in your client’s return."
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
      "render Job Seeker's Allowance page without any job seeker's allowance items" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aJobSeekersAllowancePage.copy(summaryListDataRows = Seq.empty)

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYear))
        h1Check(expectedHeading)
        elementNotOnPageCheck(Selectors.summaryListRowSelector(1))
        linkCheck(expectedAddMissingClaimLinkText, selector = Selectors.addMissingClaimLinkSelector, href = JobSeekersAllowanceController.show(taxYear).url)
        buttonCheck(expectedButtonText, Selectors.buttonSelector, None)
      }

      "render Job Seeker's Allowance with job seeker's allowance items" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aJobSeekersAllowancePage.copy(summaryListDataRows = Seq(
          aBenefitSummaryListRowData.copy(amount = Some(100.00), startDate = LocalDate.parse(s"$taxYearEOY-01-01"), endDate = LocalDate.parse(s"$taxYearEOY-01-31"), isIgnored = false),
          aBenefitSummaryListRowData.copy(amount = Some(200.20), startDate = LocalDate.parse(s"$taxYearEOY-02-01"), endDate = LocalDate.parse(s"$taxYearEOY-04-05"), isIgnored = true)
        ))

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYear))
        h1Check(expectedHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListRow1Text, Selectors.summaryListRowSelector(1), "row-1")
        linkCheck(userScenario.commonExpectedResults.expectedViewLinkText, Selectors.summaryListRowViewLinkSelector(1), JobSeekersAllowanceController.show(taxYear).url)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListRow2Text, Selectors.summaryListRowSelector(2), "row-2")
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedSummaryListRowRemovedText, Selectors.summaryListRowRemovedSelector(2))
        linkCheck(expectedAddMissingClaimLinkText, selector = Selectors.addMissingClaimLinkSelector, href = JobSeekersAllowanceController.show(taxYear).url)
        buttonCheck(expectedButtonText, Selectors.buttonSelector, None)
      }
    }
  }
}
