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

import controllers.jobseekers.routes._
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.jobseekers.ReviewClaimPageBuilder.aReviewClaimPage
import utils.ViewUtils.{bigDecimalCurrency, translatedDateFormatter}
import views.html.pages.jobseekers.ReviewClaimPageView

class ReviewClaimPageViewSpec extends ViewUnitTest {

  private val page: ReviewClaimPageView = inject[ReviewClaimPageView]

  object Selectors {
    val bannerParagraphSelector: String = ".govuk-notification-banner__heading"
    val bannerLinkSelector: String = ".govuk-notification-banner__link"
    val p1 = "#main-content > div > div > p.govuk-body"
    val saveButtonSelector = "#save-and-continue-button"
    val removeLinkSelector = "#remove-link"
    val removeLinkHiddenSelector = "#remove-link > span.govuk-visually-hidden"

    def summaryListRowFieldNameSelector(i: Int): String = s"#main-content > div > div > dl > div:nth-child($i) > dt"

    def summaryListRowFieldValueSelector(i: Int): String = s"#main-content > div > div > dl > div:nth-child($i) > dd.govuk-summary-list__value"

    def changeLink(i: Int): String = s"#main-content > div > div > dl > div:nth-child($i) > dd.govuk-summary-list__actions > a"

    def hiddenChangeLink(i: Int): String = s"#main-content > div > div > dl > div:nth-child($i) > dd.govuk-summary-list__actions > a > span.govuk-visually-hidden"
  }

  trait SpecificExpectedResults {
    val expectedStartDateText: String
    val expectedStartDateHiddenText: String
    val expectedEndDateQuestionHiddenText: Int => String
    val expectedEndDateHiddenText: String
    val expectedAmountHiddenText: String
    val expectedTaxPaidQuestionHiddenText: String
    val expectedTaxPaidHiddenText: String

    def expectedAmountText(firstDate: String, secondDate: String): String

    def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String

    def expectedTaxPaidText(firstDate: String, secondDate: String): String
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedCaption: Int => String
    val expectedExternalDataText: String
    val expectedEndDateQuestionText: Int => String
    val expectedEndDateText: String
    val expectedChangeLinkText: String
    val expectedSaveButtonText: String
    val expectedRemoveLinkText: String
    val expectedRemoveLinkHiddenText: String
    val expectedYesText: String
    val expectedNoText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Review Jobseeker’s allowance claim"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedExternalDataText: String = "This data is from the Department of Work and Pensions (DWP)"
    override val expectedEndDateQuestionText: Int => String = (taxYear: Int) => s"Did this claim end in the tax year ending 5 April $taxYear?"
    override val expectedEndDateText = "When did this claim end?"
    override val expectedChangeLinkText: String = "Change"
    override val expectedSaveButtonText: String = "Save and continue"
    override val expectedRemoveLinkText: String = "Remove claim"
    override val expectedRemoveLinkHiddenText: String = "Remove this Jobseeker’s allowance claim"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Review Jobseeker’s allowance claim"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedExternalDataText: String = "This data is from the Department of Work and Pensions (DWP)"
    override val expectedEndDateQuestionText: Int => String = (taxYear: Int) => s"Did this claim end in the tax year ending 5 April $taxYear?"
    override val expectedEndDateText = "When did this claim end?"
    override val expectedChangeLinkText: String = "Change"
    override val expectedSaveButtonText: String = "Cadw ac yn eich blaen"
    override val expectedRemoveLinkText: String = "Remove claim"
    override val expectedRemoveLinkHiddenText: String = "Remove this Jobseeker’s allowance claim"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date you started getting Jobseeker’s Allowance"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your Jobseeker’s Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedEndDateHiddenText: String = "Change the date your Jobseeker’s Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Jobseeker’s Allowance you got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether you had any tax taken off your Jobseeker’s Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your Jobseeker’s Allowance claim"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Jobseeker’s Allowance did you get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String = s"Did you have any tax taken off your Jobseeker’s Allowance between $firstDate and $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your Jobseeker’s Allowance between $firstDate and $secondDate?"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date you started getting Jobseeker’s Allowance"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your Jobseeker’s Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedEndDateHiddenText: String = "Change the date your Jobseeker’s Allowance claim ended"


    override val expectedAmountHiddenText: String = "Change the amount of Jobseeker’s Allowance you got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether you had any tax taken off your Jobseeker’s Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your Jobseeker’s Allowance claim"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Jobseeker’s Allowance did you get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String = s"Did you have any tax taken off your Jobseeker’s Allowance between $firstDate and $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your Jobseeker’s Allowance between $firstDate and $secondDate?"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date your client started getting Jobseeker’s Allowance"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your client’s Jobseeker’s Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedEndDateHiddenText: String = "Change the date your client’s Jobseeker’s Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Jobseeker’s Allowance your client got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether your client had any tax taken off your Jobseeker’s Allowance claim"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your client’s Jobseeker’s Allowance between $firstDate and $secondDate?"

    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your client’s Jobseeker’s Allowance claim"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Jobseeker’s Allowance did your client get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String = s"Did your client have any tax taken off their Jobseeker’s Allowance between $firstDate and $secondDate?"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date your client started getting Jobseeker’s Allowance"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your client’s Jobseeker’s Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedEndDateHiddenText: String = "Change the date your client’s Jobseeker’s Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Jobseeker’s Allowance your client got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether your client had any tax taken off your Jobseeker’s Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your client’s Jobseeker’s Allowance claim"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Jobseeker’s Allowance did your client get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String = s"Did your client have any tax taken off their Jobseeker’s Allowance between $firstDate and $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your client’s Jobseeker’s Allowance between $firstDate and $secondDate?"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      import Selectors._
      import userScenario.commonExpectedResults._
      import userScenario.specificExpectedResults._

      "render end of year version of ReviewJobSeekersAllowanceClaim page" when {
        "there is user provided data" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val pageModel = aReviewClaimPage.copy(taxYear = taxYearEOY)
          val translatedStartDate = translatedDateFormatter(pageModel.startDate)
          val translatedEndDate = translatedDateFormatter(pageModel.endDate.get)

          implicit val document: Document = Jsoup.parse(page(pageModel).body)

          welshToggleCheck(userScenario.isWelsh)
          titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
          captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYearEOY))
          elementNotOnPageCheck(p1)

          textOnPageCheck(get.expectedStartDateText, summaryListRowFieldNameSelector(1))
          textOnPageCheck(translatedStartDate, summaryListRowFieldValueSelector(1))
          linkCheck(s"$expectedChangeLinkText ${get.expectedStartDateHiddenText}", changeLink(1), StartDateController.show(taxYearEOY, pageModel.sessionDataId).url, Some(hiddenChangeLink(1)))
          textOnPageCheck(expectedEndDateQuestionText(taxYearEOY), summaryListRowFieldNameSelector(2))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(2), "for the end date question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateQuestionHiddenText(taxYearEOY)}", changeLink(2),
            DidClaimEndInTaxYearController.show(taxYearEOY, pageModel.sessionDataId).url, Some(hiddenChangeLink(2)))
          textOnPageCheck(expectedEndDateText, summaryListRowFieldNameSelector(3))
          textOnPageCheck(translatedEndDate, summaryListRowFieldValueSelector(3))
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateHiddenText}", changeLink(3), EndDateController.show(taxYearEOY, pageModel.sessionDataId).url, Some(hiddenChangeLink(3)))
          textOnPageCheck(get.expectedAmountText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(4))
          textOnPageCheck(bigDecimalCurrency(pageModel.amount.get.toString()), summaryListRowFieldValueSelector(4))
          linkCheck(s"$expectedChangeLinkText ${get.expectedAmountHiddenText}", changeLink(4), AmountController.show(taxYearEOY, pageModel.sessionDataId).url, Some(hiddenChangeLink(4)))
          textOnPageCheck(get.expectedTaxPaidQuestionText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(5))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(5), "for the tax paid question")
          buttonCheck(userScenario.commonExpectedResults.expectedSaveButtonText, saveButtonSelector)
          linkCheck(expectedRemoveLinkText, removeLinkSelector, RemoveClaimController.show(taxYearEOY, pageModel.sessionDataId).url,
            Some(expectedRemoveLinkHiddenText), Some(removeLinkHiddenSelector))
        }

        "there is external data provided" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val pageModel = aReviewClaimPage.copy(isCustomerAdded = false)

          implicit val document: Document = Jsoup.parse(page(pageModel).body)
          textOnPageCheck(userScenario.commonExpectedResults.expectedExternalDataText, Selectors.p1)
        }
      }
    }
  }
}
