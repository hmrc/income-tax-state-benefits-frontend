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
import support.builders.pages.elements.BenefitDataRowBuilder.aBenefitDataRow
import views.html.pages.ClaimsPageView

import java.time.LocalDate

class ClaimsPageViewSpec extends ViewUnitTest {

  private val page: ClaimsPageView = inject[ClaimsPageView]

  object Selectors {
    val addMissingClaimButtonSelector = "#add-missing-claim-button-id"
    val removedClaimH2Selector = "h2.govuk-heading-m"
    val removedClaimP1Selector = "p.govuk-body"
    val addMissingClaimFormSelector = "#main-content > div > div > form"
    val buttonSelector = "#continue"
    val secondaryButton = ".govuk-button.govuk-button--secondary"

    def summaryListRowSelector(row: Int, isIgnoredList: Boolean): String = s"${if (isIgnoredList) "#ignored-" else "#"}benefits-summary-list-id > div:nth-child($row)"

    def summaryListRowValueSelector(row: Int, column: Int, isIgnoredList: Boolean = false): String = s"${summaryListRowSelector(row, isIgnoredList)} > :nth-child($column)"

    def summaryListRowViewLinkSelector(row: Int, isIgnoredList: Boolean = false): String = s"${summaryListRowSelector(row, isIgnoredList)} > dd > a"

    def summaryListRowViewLinkHiddenTextSelector(row: Int, isIgnoredList: Boolean = false): String = s"${summaryListRowSelector(row, isIgnoredList)} > dd > a > span:nth-child(2)"
  }

  trait SpecificExpectedResults {
    def expectedRemovedClaimsParagraphText(isPlural: Boolean): String
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedViewLinkText: String
    val expectedViewLinkHiddenText: String
    val expectedRow1Value2Text: Int => String
    val expectedRow2Value2Text: Int => String
    val expectedRemovedClaimsText: String
    val expectedSummaryListRow1TextInYear: String
    val expectedAddMissingClaimButtonText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Jobseeker’s Allowance"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedViewLinkText: String = "View"
    override val expectedViewLinkHiddenText: String = "View Jobseeker’s Allowance claim details"
    override val expectedRow1Value2Text: Int => String = (taxYear: Int) => s"1 January $taxYear to 31 January $taxYear"
    override val expectedRow2Value2Text: Int => String = (taxYear: Int) => s"1 February $taxYear to 5 April $taxYear"
    override val expectedRemovedClaimsText: String = "Removed claims"
    override val expectedAddMissingClaimButtonText: String = "Add missing claim"
    override val expectedButtonText: String = "Continue"
    override val expectedSummaryListRow1TextInYear: String = s"1 January $taxYearEOY"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Lwfans Ceisio Gwaith"
    override val expectedHeading: String = "Lwfans Ceisio Gwaith"
    override val expectedViewLinkText: String = "Bwrw golwg"
    override val expectedViewLinkHiddenText: String = "Bwrw golwg dros fanylion yr hawliad Lwfans Ceisio Gwaith"
    override val expectedRow1Value2Text: Int => String = (taxYear: Int) => s"1 Ionawr $taxYear i 31 Ionawr $taxYear"
    override val expectedRow2Value2Text: Int => String = (taxYear: Int) => s"1 Chwefror $taxYear i 5 Ebrill $taxYear"
    override val expectedRemovedClaimsText: String = "Hawliadau sydd wedi cael eu tynnu"
    override val expectedAddMissingClaimButtonText: String = "Ychwanegu hawliad sydd ar goll"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedSummaryListRow1TextInYear: String = s"1 Ionawr $taxYearEOY"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    def expectedRemovedClaimsParagraphText(isPlural: Boolean): String = ""
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    def expectedRemovedClaimsParagraphText(isPlural: Boolean): String = ""
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    def expectedRemovedClaimsParagraphText(isPlural: Boolean): String = ""
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    def expectedRemovedClaimsParagraphText(isPlural: Boolean): String = ""

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
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render the page for an inYear tax claim" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val pageModel = aClaimsPage.copy(benefitType = JobSeekersAllowance, taxYear = taxYear, isInYear = true, ignoredBenefitDataRows = Seq.empty)

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        textOnPageCheck(expectedSummaryListRow1TextInYear, summaryListRowValueSelector(1, 1))
        linkCheck(expectedViewLinkText, summaryListRowViewLinkSelector(1), UserSessionDataController.loadToSession(taxYear,
          JobSeekersAllowance, aBenefitDataRow.benefitId).url, Some(expectedViewLinkHiddenText), Some(summaryListRowViewLinkHiddenTextSelector(1)))
        elementNotOnPageCheck(addMissingClaimFormSelector)
        elementNotOnPageCheck(removedClaimH2Selector)
        buttonCheck(expectedButtonText, buttonSelector, Some(SummaryController.show(taxYear).url))
      }

      "render the page for an inYear tax claim when there are no benefits" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val pageModel = aClaimsPage.copy(benefitType = JobSeekersAllowance, taxYear = taxYear, isInYear = true, ignoredBenefitDataRows = Seq.empty)

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        elementNotOnPageCheck(addMissingClaimFormSelector)
        elementNotOnPageCheck(removedClaimH2Selector)
        buttonCheck(expectedButtonText, buttonSelector, Some(SummaryController.show(taxYear).url))
        elementNotOnPageCheck(secondaryButton)
      }

      "render page without any claims" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aClaimsPage.copy(benefitType = JobSeekersAllowance, benefitDataRows = Seq.empty, ignoredBenefitDataRows = Seq.empty)

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        elementNotOnPageCheck(summaryListRowSelector(1, isIgnoredList = false))
        formPostLinkCheck(UserSessionDataController.create(taxYearEOY, JobSeekersAllowance).url, addMissingClaimFormSelector)
        buttonCheck(expectedAddMissingClaimButtonText, addMissingClaimButtonSelector, None)
        buttonCheck(expectedButtonText, buttonSelector, Some(SummaryController.show(taxYearEOY).url))
        textOnPageCheck(expectedButtonText, secondaryButton)
      }

      "render page with one normal claim only" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aClaimsPage.copy(
          benefitType = JobSeekersAllowance,
          benefitDataRows = Seq(aBenefitDataRow.copy(amount = Some(100.00), startDate = LocalDate.parse(s"$taxYearEOY-01-01"), endDate = LocalDate.parse(s"$taxYearEOY-01-31"))),
          ignoredBenefitDataRows = Seq.empty
        )

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        textOnPageCheck("£100", summaryListRowValueSelector(1, 1))
        textOnPageCheck(expectedRow1Value2Text(taxYearEOY), summaryListRowValueSelector(1, 2))
        linkCheck(s"$expectedViewLinkText $expectedViewLinkHiddenText", summaryListRowViewLinkSelector(1),
          UserSessionDataController.loadToSession(taxYearEOY, JobSeekersAllowance, aBenefitDataRow.benefitId).url, Some(summaryListRowViewLinkHiddenTextSelector(1)))
        formPostLinkCheck(UserSessionDataController.create(taxYearEOY, JobSeekersAllowance).url, addMissingClaimFormSelector)
        elementNotOnPageCheck(removedClaimH2Selector)
        buttonCheck(expectedAddMissingClaimButtonText, addMissingClaimButtonSelector, None)
        buttonCheck(expectedButtonText, buttonSelector, Some(SummaryController.show(taxYearEOY).url))
        textOnPageCheck(expectedAddMissingClaimButtonText, secondaryButton)
      }

      "render page with one ignored claim only" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aClaimsPage.copy(
          benefitType = JobSeekersAllowance,
          benefitDataRows = Seq.empty,
          ignoredBenefitDataRows =
            Seq(aBenefitDataRow.copy(amount = Some(200.20), startDate = LocalDate.parse(s"$taxYearEOY-02-01"), endDate = LocalDate.parse(s"$taxYearEOY-04-05"), isIgnored = true))
        )

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        elementNotOnPageCheck(summaryListRowSelector(1, isIgnoredList = false))
        formPostLinkCheck(UserSessionDataController.create(taxYearEOY, JobSeekersAllowance).url, addMissingClaimFormSelector)
        buttonCheck(expectedAddMissingClaimButtonText, addMissingClaimButtonSelector, None)
        textOnPageCheck(expectedRemovedClaimsText, removedClaimH2Selector)
        textOnPageCheck(get.expectedRemovedClaimsParagraphText(isPlural = false), removedClaimP1Selector)
        textOnPageCheck("£200.20", summaryListRowValueSelector(1, 1, isIgnoredList = true))
        textOnPageCheck(expectedRow2Value2Text(taxYearEOY), summaryListRowValueSelector(1, 2, isIgnoredList = true))
        linkCheck(s"$expectedViewLinkText $expectedViewLinkHiddenText", summaryListRowViewLinkSelector(1, isIgnoredList = true), UserSessionDataController.loadToSession(taxYearEOY,
          JobSeekersAllowance, aBenefitDataRow.benefitId).url, Some(summaryListRowViewLinkHiddenTextSelector(1)), additionalTestText = "ignore")
        buttonCheck(expectedButtonText, buttonSelector, Some(SummaryController.show(taxYearEOY).url))
        textOnPageCheck(expectedAddMissingClaimButtonText, secondaryButton)
      }

      "render page with multiple ignored claims only" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aClaimsPage.copy(
          benefitType = JobSeekersAllowance,
          benefitDataRows = Seq.empty,
          ignoredBenefitDataRows = Seq(
            aBenefitDataRow.copy(amount = Some(100.20), startDate = LocalDate.parse(s"$taxYearEOY-01-01"), endDate = LocalDate.parse(s"$taxYearEOY-01-31"), isIgnored = true),
            aBenefitDataRow.copy(amount = Some(200.20), startDate = LocalDate.parse(s"$taxYearEOY-02-01"), endDate = LocalDate.parse(s"$taxYearEOY-04-05"), isIgnored = true)
          )
        )

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        elementNotOnPageCheck(summaryListRowSelector(1, isIgnoredList = false))
        formPostLinkCheck(UserSessionDataController.create(taxYearEOY, JobSeekersAllowance).url, addMissingClaimFormSelector)
        buttonCheck(expectedAddMissingClaimButtonText, addMissingClaimButtonSelector, None)
        textOnPageCheck(expectedRemovedClaimsText, removedClaimH2Selector)
        textOnPageCheck(get.expectedRemovedClaimsParagraphText(isPlural = true), removedClaimP1Selector)
        textOnPageCheck("£100.20", summaryListRowValueSelector(1, 1, isIgnoredList = true))
        textOnPageCheck(expectedRow1Value2Text(taxYearEOY), summaryListRowValueSelector(1, 2, isIgnoredList = true))
        linkCheck(s"$expectedViewLinkText $expectedViewLinkHiddenText", summaryListRowViewLinkSelector(1, isIgnoredList = true), UserSessionDataController.loadToSession(taxYearEOY,
          JobSeekersAllowance, aBenefitDataRow.benefitId).url, Some(summaryListRowViewLinkHiddenTextSelector(1)), additionalTestText = "ignore row 1")
        textOnPageCheck("£200.20", summaryListRowValueSelector(2, 1, isIgnoredList = true))
        textOnPageCheck(expectedRow2Value2Text(taxYearEOY), summaryListRowValueSelector(2, 2, isIgnoredList = true))
        linkCheck(s"$expectedViewLinkText $expectedViewLinkHiddenText", summaryListRowViewLinkSelector(2, isIgnoredList = true), UserSessionDataController.loadToSession(taxYearEOY,
          JobSeekersAllowance, aBenefitDataRow.benefitId).url, Some(summaryListRowViewLinkHiddenTextSelector(2)), additionalTestText = "ignore row 2")
        buttonCheck(expectedButtonText, buttonSelector, Some(SummaryController.show(taxYearEOY).url))
        textOnPageCheck(expectedAddMissingClaimButtonText, secondaryButton)
      }

      "render page with one normal claim and one ignored claim" which {
        implicit val userPriorDataRequest: UserPriorDataRequest[AnyContent] = getUserPriorDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aClaimsPage.copy(
          benefitType = JobSeekersAllowance,
          benefitDataRows = Seq(aBenefitDataRow.copy(amount = Some(100.00), startDate = LocalDate.parse(s"$taxYearEOY-01-01"), endDate = LocalDate.parse(s"$taxYearEOY-01-31"))),
          ignoredBenefitDataRows =
            Seq(aBenefitDataRow.copy(amount = Some(200.20), startDate = LocalDate.parse(s"$taxYearEOY-02-01"), endDate = LocalDate.parse(s"$taxYearEOY-04-05"), isIgnored = true))
        )

        implicit val document: Document = Jsoup.parse(page(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        textOnPageCheck("£100", summaryListRowValueSelector(1, 1))
        textOnPageCheck(expectedRow1Value2Text(taxYearEOY), summaryListRowValueSelector(1, 2))
        linkCheck(s"$expectedViewLinkText $expectedViewLinkHiddenText", summaryListRowViewLinkSelector(1),
          UserSessionDataController.loadToSession(taxYearEOY, JobSeekersAllowance, aBenefitDataRow.benefitId).url, Some(summaryListRowViewLinkHiddenTextSelector(1)))
        formPostLinkCheck(UserSessionDataController.create(taxYearEOY, JobSeekersAllowance).url, addMissingClaimFormSelector)
        buttonCheck(expectedAddMissingClaimButtonText, addMissingClaimButtonSelector, None)
        textOnPageCheck(expectedRemovedClaimsText, removedClaimH2Selector)
        textOnPageCheck(get.expectedRemovedClaimsParagraphText(false), removedClaimP1Selector)
        textOnPageCheck("£200.20", summaryListRowValueSelector(1, 1, isIgnoredList = true))
        textOnPageCheck(expectedRow2Value2Text(taxYearEOY), summaryListRowValueSelector(1, 2, isIgnoredList = true))
        linkCheck(s"$expectedViewLinkText $expectedViewLinkHiddenText", summaryListRowViewLinkSelector(1, isIgnoredList = true), UserSessionDataController.loadToSession(taxYearEOY,
          JobSeekersAllowance, aBenefitDataRow.benefitId).url, Some(summaryListRowViewLinkHiddenTextSelector(1)), additionalTestText = "ignore")
        buttonCheck(expectedButtonText, buttonSelector, Some(SummaryController.show(taxYearEOY).url))
        textOnPageCheck(expectedAddMissingClaimButtonText, secondaryButton)
      }
    }
  }
}
