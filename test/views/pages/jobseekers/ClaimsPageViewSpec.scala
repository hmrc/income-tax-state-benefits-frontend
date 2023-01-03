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

import controllers.routes.SummaryController
import controllers.session.routes.UserSessionDataController
import models.BenefitType.JobSeekersAllowance
import models.requests.UserPriorDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.ClaimsPageBuilder.aClaimsPage
import support.builders.pages.elements.BenefitSummaryListRowDataBuilder.aBenefitSummaryListRowData
import views.html.pages.ClaimsPageView

import java.time.LocalDate

class ClaimsPageViewSpec extends ViewUnitTest {

  private val page: ClaimsPageView = inject[ClaimsPageView]

  object Selectors {
    val summaryListRowRemovedSelector: Int => String = (row: Int) => s"div.govuk-summary-list__row:nth-child($row) > div:nth-child(2) > p"
    val addMissingClaimButtonSelector = "#add-missing-claim-button-id"
    val addMissingClaimFormSelector = "#main-content > div > div > form"
    val buttonSelector = "#continue"

    def summaryListRowSelector(row: Int, isInYear: Boolean = false): String = s"div.govuk-summary-list__row:nth-child($row) ${if (!isInYear) "> div:nth-child(1)" else ""}"

    def summaryListRowValueSelector(row: Int, column: Int, isInYear: Boolean = false): String = s"${summaryListRowSelector(row, isInYear)} > dt:nth-child($column)"

    def summaryListRowViewLinkSelector(row: Int, isInYear: Boolean = false): String = s"${summaryListRowSelector(row, isInYear)} > dd > a"

    def summaryListRowViewLinkHiddenTextSelector(row: Int, isInYear: Boolean = false): String = s"${summaryListRowViewLinkSelector(row, isInYear)} > span.govuk-visually-hidden"
  }

  trait SpecificExpectedResults {
    val expectedSummaryListRowRemovedText: String
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedCaption: Int => String
    val expectedViewLinkText: String
    val expectedViewLinkHiddenText: String
    val expectedSummaryListRow1Value1Text: String
    val expectedSummaryListRow1Value2Text: Int => String
    val expectedSummaryListRow2Value1Text: String
    val expectedSummaryListRow2Value2Text: Int => String
    val expectedSummaryListRow1TextInYear: String
    val expectedAddMissingClaimButtonText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Jobseeker’s Allowance"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedViewLinkText: String = "View"
    override val expectedViewLinkHiddenText: String = "View Jobseeker’s Allowance claim details"
    override val expectedSummaryListRow1Value1Text: String = "£100"
    override val expectedSummaryListRow1Value2Text: Int => String = (taxYear: Int) => s"1 January $taxYear to 31 January $taxYear"
    override val expectedSummaryListRow2Value1Text: String = "£200.20"
    override val expectedSummaryListRow2Value2Text: Int => String = (taxYear: Int) => s"1 February $taxYear to 5 April $taxYear"
    override val expectedAddMissingClaimButtonText: String = "Add missing claim"
    override val expectedButtonText: String = "Continue"
    override val expectedSummaryListRow1TextInYear: String = "1 January 2022"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Jobseeker’s Allowance"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedViewLinkText: String = "View"
    override val expectedViewLinkHiddenText: String = "View Jobseeker’s Allowance claim details"
    override val expectedSummaryListRow1Value1Text: String = "£100"
    override val expectedSummaryListRow1Value2Text: Int => String = (taxYear: Int) => s"1 Ionawr $taxYear to 31 Ionawr $taxYear"
    override val expectedSummaryListRow2Value1Text: String = "£200.20"
    override val expectedSummaryListRow2Value2Text: Int => String = (taxYear: Int) => s"1 Chwefror $taxYear to 5 Ebrill $taxYear"
    override val expectedAddMissingClaimButtonText: String = "Add missing claim"
    override val expectedButtonText: String = "Continue"
    override val expectedSummaryListRow1TextInYear: String = "1 Ionawr 2022"
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

        val pageModel = aClaimsPage.copy(summaryListDataRows = Seq.empty)

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(expectedHeading)
        elementNotOnPageCheck(Selectors.summaryListRowSelector(1))
        formPostLinkCheck(UserSessionDataController.create(taxYearEOY, JobSeekersAllowance).url, Selectors.addMissingClaimFormSelector)
        buttonCheck(expectedButtonText, Selectors.buttonSelector, Some(SummaryController.show(taxYearEOY).url))
      }

      "render Job Seeker's Allowance with job seeker's allowance items" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aClaimsPage.copy(summaryListDataRows = Seq(
          aBenefitSummaryListRowData.copy(amount = Some(100.00), startDate = LocalDate.parse(s"$taxYearEOY-01-01"), endDate = LocalDate.parse(s"$taxYearEOY-01-31"), isIgnored = false),
          aBenefitSummaryListRowData.copy(amount = Some(200.20), startDate = LocalDate.parse(s"$taxYearEOY-02-01"), endDate = LocalDate.parse(s"$taxYearEOY-04-05"), isIgnored = true)
        ))

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(expectedHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListRow1Value1Text, Selectors.summaryListRowValueSelector(1, 1), "row-1-1")
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListRow1Value2Text(taxYearEOY), Selectors.summaryListRowValueSelector(1, 2), "row-1-2")
        linkCheck(userScenario.commonExpectedResults.expectedViewLinkText, Selectors.summaryListRowViewLinkSelector(1), UserSessionDataController.loadToSession(taxYearEOY,
          JobSeekersAllowance, aBenefitSummaryListRowData.benefitId).url, Some(expectedViewLinkHiddenText), Some(Selectors.summaryListRowViewLinkHiddenTextSelector(1)))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListRow2Value1Text, Selectors.summaryListRowValueSelector(2, 1), "row-2-1")
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListRow2Value2Text(taxYearEOY), Selectors.summaryListRowValueSelector(2, 2), "row-2-2")
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedSummaryListRowRemovedText, Selectors.summaryListRowRemovedSelector(2))
        formPostLinkCheck(UserSessionDataController.create(taxYearEOY, JobSeekersAllowance).url, Selectors.addMissingClaimFormSelector)
        buttonCheck(expectedButtonText, Selectors.buttonSelector, Some(SummaryController.show(taxYearEOY).url))
      }

      "render the page for an inYear tax claim" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val pageModel = aClaimsPage.copy(taxYear = taxYear, isInYear = true)

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYear))
        h1Check(expectedHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListRow1TextInYear, Selectors.summaryListRowValueSelector(1, 1, isInYear = true), "row-1")
        linkCheck(userScenario.commonExpectedResults.expectedViewLinkText,
          Selectors.summaryListRowViewLinkSelector(1, isInYear = true), UserSessionDataController.loadToSession(taxYear, JobSeekersAllowance, aBenefitSummaryListRowData.benefitId).url,
          Some(expectedViewLinkHiddenText), Some(Selectors.summaryListRowViewLinkHiddenTextSelector(1, isInYear = true)))
        elementNotOnPageCheck(Selectors.addMissingClaimFormSelector)
        buttonCheck(expectedButtonText, Selectors.buttonSelector, Some(SummaryController.show(taxYear).url))
      }
    }
  }
}
