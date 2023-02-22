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

package views.pages.employmentsupport

import controllers.routes._
import models.BenefitType.EmploymentSupportAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.ReviewClaimPageBuilder.aReviewClaimPage
import utils.ViewUtils.{bigDecimalCurrency, translatedDateFormatter}
import views.html.pages.ReviewClaimPageView

import java.time.LocalDate

class ReviewClaimPageViewSpec extends ViewUnitTest {

  private val underTest: ReviewClaimPageView = inject[ReviewClaimPageView]

  object Selectors {
    val bannerParagraphSelector: String = ".govuk-notification-banner__heading"
    val bannerLinkSelector: String = ".govuk-notification-banner__link"
    val p1 = "#main-content > div > div > p.govuk-body"
    val saveAndContinueButtonSelector = "#save-and-continue-button-id"
    val continueButtonSelector = "#continue"
    val pageFormSelector = "#main-content > div > div > form"
    val removeLinkSelector = "#remove-link-id"
    val removeLinkHiddenSelector = "#remove-link-id > span.govuk-visually-hidden"
    val rowsSelector = "#main-content > div > div > dl > div"
    val backLinkSelector = "#back-link-id"
    val restoreClaimButtonSelector = "#restore-claim-button-id"

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
    val expectedEndDateText: String
    val expectedChangeLinkText: String
    val expectedSaveButtonText: String
    val expectedContinueButtonText: String
    val expectedRemoveLinkText: String
    val expectedRemoveLinkHiddenText: String
    val expectedRestoreClaimButtonText: String
    val expectedBackText: String
    val expectedYesText: String
    val expectedNoText: String

    def expectedEndDateQuestionText(taxYear: Int, startDate: LocalDate): String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Review Employment and Support Allowance claim"
    override val expectedHeading: String = "Employment and Support Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedExternalDataText: String = "This data is from the Department of Work and Pensions (DWP)"
    override val expectedEndDateText = "When did this claim end?"
    override val expectedChangeLinkText: String = "Change"
    override val expectedSaveButtonText: String = "Save and continue"
    override val expectedContinueButtonText: String = "Continue"
    override val expectedRemoveLinkText: String = "Remove claim"
    override val expectedRemoveLinkHiddenText: String = "Remove this Employment and Support Allowance claim"
    override val expectedRestoreClaimButtonText: String = "Restore claim"
    override val expectedBackText: String = "Back"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"

    override def expectedEndDateQuestionText(taxYear: Int, startDate: LocalDate): String = s"Did this claim end between ${translatedDateFormatter(startDate)(defaultMessages)} and 5 April $taxYear?"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Review Employment and Support Allowance claim"
    override val expectedHeading: String = "Employment and Support Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedExternalDataText: String = "This data is from the Department of Work and Pensions (DWP)"
    override val expectedEndDateText = "When did this claim end?"
    override val expectedChangeLinkText: String = "Change"
    override val expectedSaveButtonText: String = "Cadw ac yn eich blaen"
    override val expectedContinueButtonText: String = "Continue"
    override val expectedRemoveLinkText: String = "Remove claim"
    override val expectedRemoveLinkHiddenText: String = "Remove this Employment and Support Allowance claim"
    override val expectedRestoreClaimButtonText: String = "Restore claim"
    override val expectedBackText: String = "Back"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"

    override def expectedEndDateQuestionText(taxYear: Int, startDate: LocalDate): String = s"Did this claim end between ${translatedDateFormatter(startDate)(welshMessages)} and 5 April $taxYear?"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did you start getting Employment and Support Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date you started getting Employment and Support Allowance"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your Employment and Support Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedEndDateHiddenText: String = "Change the date your Employment and Support Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Employment and Support Allowance you got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether you had any tax taken off your Employment and Support Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your Employment and Support Allowance claim"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Employment and Support Allowance did you get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String =
      s"Did you have any tax taken off your Employment and Support Allowance between $firstDate and $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your Employment and Support Allowance between $firstDate and $secondDate?"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did you start getting Employment and Support Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date you started getting Employment and Support Allowance"
    override val expectedEndDateHiddenText: String = "Change the date your Employment and Support Allowance claim ended"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your Employment and Support Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedAmountHiddenText: String = "Change the amount of Employment and Support Allowance you got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether you had any tax taken off your Employment and Support Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your Employment and Support Allowance claim"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Employment and Support Allowance did you get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String =
      s"Did you have any tax taken off your Employment and Support Allowance between $firstDate and $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your Employment and Support Allowance between $firstDate and $secondDate?"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did your client start getting Employment and Support Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date your client started getting Employment and Support Allowance"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your client’s Employment and Support Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedEndDateHiddenText: String = "Change the date your client’s Employment and Support Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Employment and Support Allowance your client got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether your client had any tax taken off your Employment and Support Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your client’s Employment and Support Allowance claim"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your client’s Employment and Support Allowance between $firstDate and $secondDate?"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Employment and Support Allowance did your client get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String =
      s"Did your client have any tax taken off their Employment and Support Allowance between $firstDate and $secondDate?"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did your client start getting Employment and Support Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date your client started getting Employment and Support Allowance"
    override val expectedEndDateQuestionHiddenText: Int => String = (taxYear: Int) => s"Change whether your client’s Employment and Support Allowance claim ended in the tax year ending 5 April $taxYear"
    override val expectedEndDateHiddenText: String = "Change the date your client’s Employment and Support Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Employment and Support Allowance your client got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether your client had any tax taken off your Employment and Support Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your client’s Employment and Support Allowance claim"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Employment and Support Allowance did your client get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String =
      s"Did your client have any tax taken off their Employment and Support Allowance between $firstDate and $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your client’s Employment and Support Allowance between $firstDate and $secondDate?"
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
      val pageModel = aReviewClaimPage.copy(taxYear = taxYearEOY, benefitType = EmploymentSupportAllowance)
      "render end of year version of ReviewEmploymentAndSupportAllowanceClaim page" when {
        "customer added data" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val translatedStartDate = translatedDateFormatter(pageModel.startDate)
          val translatedEndDate = translatedDateFormatter(pageModel.endDate.get)

          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          welshToggleCheck(userScenario.isWelsh)
          titleCheck(expectedTitle, userScenario.isWelsh)
          captionCheck(expectedCaption(taxYearEOY))
          elementNotOnPageCheck(p1)

          textOnPageCheck(get.expectedStartDateText, summaryListRowFieldNameSelector(1))
          textOnPageCheck(translatedStartDate, summaryListRowFieldValueSelector(1))
          linkCheck(s"$expectedChangeLinkText ${get.expectedStartDateHiddenText}", changeLink(1),
            StartDateController.show(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(1)))
          textOnPageCheck(expectedEndDateQuestionText(taxYearEOY, pageModel.startDate), summaryListRowFieldNameSelector(2))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(2), "for the end date question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateQuestionHiddenText(taxYearEOY)}", changeLink(2),
            EndDateQuestionController.show(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(2)))
          textOnPageCheck(expectedEndDateText, summaryListRowFieldNameSelector(3))
          textOnPageCheck(translatedEndDate, summaryListRowFieldValueSelector(3))
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateHiddenText}", changeLink(3),
            EndDateController.show(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(3)))
          textOnPageCheck(get.expectedAmountText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(4))
          textOnPageCheck(bigDecimalCurrency(pageModel.amount.get.toString()), summaryListRowFieldValueSelector(4))
          linkCheck(s"$expectedChangeLinkText ${get.expectedAmountHiddenText}", changeLink(4),
            AmountController.show(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(4)))
          textOnPageCheck(get.expectedTaxPaidQuestionText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(5))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(5), "for the tax paid question")
          checkElementsCount(6, rowsSelector)
          buttonCheck(expectedSaveButtonText, saveAndContinueButtonSelector)
          formPostLinkCheck(ReviewClaimController.saveAndContinue(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, pageFormSelector)
          linkCheck(expectedRemoveLinkText, removeLinkSelector, RemoveClaimController.show(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url,
            Some(expectedRemoveLinkHiddenText), Some(removeLinkHiddenSelector))
          elementNotOnPageCheck(restoreClaimButtonSelector)
          elementNotOnPageCheck(backLinkSelector)
        }

        "HMRC added data" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val page = pageModel.copy(isCustomerAdded = false)

          implicit val document: Document = Jsoup.parse(underTest(page).body)
          textOnPageCheck(expectedExternalDataText, p1)
          formPostLinkCheck(ReviewClaimController.saveAndContinue(taxYearEOY, EmploymentSupportAllowance, page.sessionDataId).url, pageFormSelector)
        }

        "the claim is ignored" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val page = pageModel.copy(isIgnored = true)

          implicit val document: Document = Jsoup.parse(underTest(page).body)
          buttonCheck(expectedRestoreClaimButtonText, restoreClaimButtonSelector)
          linkCheck(expectedBackText, backLinkSelector, ClaimsController.show(taxYearEOY, EmploymentSupportAllowance).url)
          elementNotOnPageCheck(changeLink(1))
          elementNotOnPageCheck(changeLink(2))
          elementNotOnPageCheck(changeLink(3))
          elementNotOnPageCheck(changeLink(4))
          elementNotOnPageCheck(changeLink(5))
          formPostLinkCheck(ReviewClaimController.restoreClaim(taxYearEOY, EmploymentSupportAllowance, page.sessionDataId).url, pageFormSelector)
        }
      }

      "render in year version of ReviewEmploymentAndSupportAllowanceClaim page" when {
        "HMRC added data" which {
          implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val page = pageModel.copy(taxYear = taxYear, isInYear = true)
          val translatedStartDate = translatedDateFormatter(page.startDate)

          implicit val document: Document = Jsoup.parse(underTest(page).body)

          welshToggleCheck(userScenario.isWelsh)
          titleCheck(expectedTitle, userScenario.isWelsh)
          captionCheck(expectedCaption(taxYear))
          elementNotOnPageCheck(p1)

          textOnPageCheck(get.expectedStartDateText, summaryListRowFieldNameSelector(1))
          textOnPageCheck(translatedStartDate, summaryListRowFieldValueSelector(1))
          elementNotOnPageCheck(changeLink(1))
          checkElementsCount(count = 1, rowsSelector)
          linkCheck(expectedContinueButtonText, continueButtonSelector, ClaimsController.show(taxYear, EmploymentSupportAllowance).url)
          elementNotOnPageCheck(restoreClaimButtonSelector)
          elementNotOnPageCheck(backLinkSelector)
        }
      }
    }
  }
}
