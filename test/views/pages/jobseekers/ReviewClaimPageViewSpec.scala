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

import controllers.routes._
import models.BenefitType.JobSeekersAllowance
import models.requests.UserPriorAndSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.pages.ReviewClaimPageBuilder.aReviewClaimPage
import utils.ViewUtils.{bigDecimalCurrency, translatedDateFormatter, translatedTaxYearEndDateFormatter}
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
    val expectedEndDateHiddenText: String
    val expectedAmountHiddenText: String
    val expectedTaxPaidQuestionHiddenText: String
    val expectedTaxPaidHiddenText: String

    def expectedAmountText(firstDate: String, secondDate: String): String

    def expectedEndDateQuestionHiddenText(taxYear: Int): String

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
    val expectedWasText: String

    def expectedEndDateQuestionText(taxYear: Int, startDate: LocalDate): String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Review Jobseeker’s Allowance claim"
    override val expectedHeading: String = "Jobseeker’s Allowance"
    override val expectedCaption: Int => String = (taxYear: Int) => s"6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedExternalDataText: String = "This data is from the Department of Work and Pensions (DWP)"
    override val expectedEndDateText = "When did this claim end?"
    override val expectedChangeLinkText: String = "Change"
    override val expectedSaveButtonText: String = "Save and continue"
    override val expectedContinueButtonText: String = "Continue"
    override val expectedRemoveLinkText: String = "Remove claim"
    override val expectedRemoveLinkHiddenText: String = "Remove this Jobseeker’s allowance claim"
    override val expectedRestoreClaimButtonText: String = "Restore claim"
    override val expectedBackText: String = "Back"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedWasText: String = "was:"

    override def expectedEndDateQuestionText(taxYear: Int, startDate: LocalDate): String =
      s"Did this claim end between ${translatedDateFormatter(startDate)(defaultMessages)} and ${translatedTaxYearEndDateFormatter(taxYear)(defaultMessages)}?"
        .replace("\u00A0", " ")
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Adolygu’r hawliad Lwfans Ceisio Gwaith"
    override val expectedHeading: String = "Lwfans Ceisio Gwaith"
    override val expectedCaption: Int => String = (taxYear: Int) => s"6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedExternalDataText: String = "Mae’r data hyn yn dod o’r Adran Gwaith a Phensiynau (DWP)"
    override val expectedEndDateText = "Pryd y daeth yr hawliad hwn i ben?"
    override val expectedChangeLinkText: String = "Newid"
    override val expectedSaveButtonText: String = "Cadw ac yn eich blaen"
    override val expectedContinueButtonText: String = "Yn eich blaen"
    override val expectedRemoveLinkText: String = "Tynnu’r hawliad"
    override val expectedRemoveLinkHiddenText: String = "Tynnu’r hawliad Lwfans Ceisio Gwaith hwn"
    override val expectedRestoreClaimButtonText: String = "Hawliad i’w adfer"
    override val expectedBackText: String = "Yn ôl"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedWasText: String = "was:"

    override def expectedEndDateQuestionText(taxYear: Int, startDate: LocalDate): String =
      s"A wnaeth yr hawliad hwn ddod i ben rhwng ${translatedDateFormatter(startDate)(welshMessages)} a ${translatedTaxYearEndDateFormatter(taxYear)(welshMessages)}?"
        .replace("\u00A0", " ")
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date you started getting Jobseeker’s Allowance"
    override val expectedEndDateHiddenText: String = "Change the date your Jobseeker’s Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Jobseeker’s Allowance you got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether you had any tax taken off your Jobseeker’s Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your Jobseeker’s Allowance claim"

    override def expectedEndDateQuestionHiddenText(taxYear: Int): String = s"Change whether your Jobseeker’s Allowance claim ended in the tax year ending 5 April $taxYear"

    override def expectedAmountText(firstDate: String, secondDate: String): String =
      s"How much Jobseeker’s Allowance did you get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String = s"Did you have any tax taken off your Jobseeker’s Allowance between $firstDate and $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your Jobseeker’s Allowance between $firstDate and $secondDate?"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedStartDateText: String = "Pryd y gwnaethoch ddechrau cael Lwfans Ceisio Gwaith?"
    override val expectedStartDateHiddenText: String = "Newid y dyddiad y gwnaethoch ddechrau cael Lwfans Ceisio Gwaith"
    override val expectedEndDateHiddenText: String = "Newid y dyddiad y daeth eich hawliad Lwfans Ceisio Gwaith i ben"
    override val expectedAmountHiddenText: String = "Newid swm y Lwfans Ceisio Gwaith a gawsoch"
    override val expectedTaxPaidQuestionHiddenText: String = "Newid p’un a ddidynnwyd unrhyw dreth o’ch hawliad Lwfans Ceisio Gwaith"
    override val expectedTaxPaidHiddenText: String = "Newid swm y dreth a ddidynnwyd o’ch hawliad Lwfans Ceisio Gwaith"

    override def expectedEndDateQuestionHiddenText(taxYear: Int): String =
      s"Newid p’un a wnaeth eich hawliad Lwfans Ceisio Gwaith dod i ben yn ystod y flwyddyn dreth a ddaeth i ben ar 5 Ebrill $taxYear"

    override def expectedAmountText(firstDate: String, secondDate: String): String =
      s"Faint o Lwfans Ceisio Gwaith a gawsoch rhwng $firstDate a $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String =
      s"A ddidynnwyd unrhyw dreth o’ch Lwfans Ceisio Gwaith rhwng $firstDate a $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"Faint o dreth a gafodd ei didynnu o’ch Lwfans Ceisio Gwaith rhwng $firstDate a $secondDate?"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedStartDateText: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedStartDateHiddenText: String = "Change the date your client started getting Jobseeker’s Allowance"
    override val expectedEndDateHiddenText: String = "Change the date your client’s Jobseeker’s Allowance claim ended"
    override val expectedAmountHiddenText: String = "Change the amount of Jobseeker’s Allowance your client got"
    override val expectedTaxPaidQuestionHiddenText: String = "Change whether your client had any tax taken off your Jobseeker’s Allowance claim"
    override val expectedTaxPaidHiddenText: String = "Change the amount of tax taken off your client’s Jobseeker’s Allowance claim"

    override def expectedEndDateQuestionHiddenText(taxYear: Int): String = s"Change whether your client’s Jobseeker’s Allowance claim ended in the tax year ending 5 April $taxYear"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"How much tax was taken off your client’s Jobseeker’s Allowance between $firstDate and $secondDate?"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"How much Jobseeker’s Allowance did your client get between $firstDate and $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String =
      s"Did your client have any tax taken off their Jobseeker’s Allowance between $firstDate and $secondDate?"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedStartDateText: String = "Pryd y gwnaeth eich cleient ddechrau cael Lwfans Ceisio Gwaith?"
    override val expectedStartDateHiddenText: String = "Newid y dyddiad y gwnaeth eich cleient ddechrau cael Lwfans Ceisio Gwaith"
    override val expectedEndDateHiddenText: String = "Newid y dyddiad y daeth hawliad Lwfans Ceisio Gwaith eich cleient i ben"
    override val expectedAmountHiddenText: String = "Newid swm y Lwfans Ceisio Gwaith a gafodd eich cleient"
    override val expectedTaxPaidQuestionHiddenText: String = "Newid p’un a ddidynnwyd unrhyw dreth o hawliad Lwfans Ceisio Gwaith eich cleient"
    override val expectedTaxPaidHiddenText: String = "Newid swm y dreth a ddidynnwyd o hawliad Lwfans Ceisio Gwaith eich cleient"

    override def expectedEndDateQuestionHiddenText(taxYear: Int): String =
      s"Newid p’un a wnaeth hawliad Lwfans Ceisio Gwaith eich cleient ddod i ben yn ystod y flwyddyn dreth a ddaeth i ben ar 5 Ebrill $taxYear"

    override def expectedAmountText(firstDate: String, secondDate: String): String = s"Faint o Lwfans Ceisio Gwaith a gafodd eich cleient rhwng $firstDate a $secondDate?"

    override def expectedTaxPaidQuestionText(firstDate: String, secondDate: String): String =
      s"A ddidynnwyd unrhyw dreth o Lwfans Ceisio Gwaith eich cleient rhwng $firstDate a $secondDate?"

    override def expectedTaxPaidText(firstDate: String, secondDate: String): String = s"Faint o dreth a gafodd ei didynnu o Lwfans Ceisio Gwaith eich cleient rhwng $firstDate a $secondDate?"
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
      val pageModel = aReviewClaimPage.copy(isHmrcData = false, taxYear = taxYearEOY)
      "render end of year version of ReviewJobSeekersAllowanceClaim page" when {
        "customer added data" which {
          implicit val UserPriorAndSessionDataRequest: UserPriorAndSessionDataRequest[AnyContent] = getUserPriorAndSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val translatedStartDate = translatedDateFormatter(pageModel.startDate).replace("\u00A0", " ")
          val translatedEndDate = translatedDateFormatter(pageModel.endDate.get).replace("\u00A0", " ")

          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          welshToggleCheck(userScenario.isWelsh)
          titleCheck(expectedTitle, userScenario.isWelsh)
          captionCheck(expectedCaption(taxYearEOY))
          elementNotOnPageCheck(p1)

          textOnPageCheck(get.expectedStartDateText, summaryListRowFieldNameSelector(1))
          textOnPageCheck(translatedStartDate, summaryListRowFieldValueSelector(1))
          linkCheck(s"$expectedChangeLinkText ${get.expectedStartDateHiddenText}", changeLink(1),
            StartDateController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(1)))
          textOnPageCheck(expectedEndDateQuestionText(taxYearEOY, pageModel.startDate), summaryListRowFieldNameSelector(2))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(2), "for the end date question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateQuestionHiddenText(taxYearEOY)}", changeLink(2),
            EndDateQuestionController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(2)))
          textOnPageCheck(expectedEndDateText, summaryListRowFieldNameSelector(3))
          textOnPageCheck(translatedEndDate, summaryListRowFieldValueSelector(3))
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateHiddenText}", changeLink(3),
            EndDateController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(3)))
          textOnPageCheck(get.expectedTaxPaidQuestionText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(4))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(4), "for the tax paid question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedTaxPaidQuestionHiddenText}", changeLink(4),
            TaxPaidQuestionController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(4)))
          textOnPageCheck(get.expectedAmountText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(5))
          textOnPageCheck(bigDecimalCurrency(pageModel.amount.get.toString()), summaryListRowFieldValueSelector(5))
          linkCheck(s"$expectedChangeLinkText ${get.expectedAmountHiddenText}", changeLink(5),
            AmountController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(5)))
          textOnPageCheck(get.expectedTaxPaidText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(6))
          textOnPageCheck(bigDecimalCurrency(pageModel.taxPaid.get.toString()), summaryListRowFieldValueSelector(6))
          linkCheck(s"$expectedChangeLinkText ${get.expectedTaxPaidHiddenText}", changeLink(6),
            TaxPaidController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(6)))
          checkElementsCount(6, rowsSelector)
          buttonCheck(expectedSaveButtonText, saveAndContinueButtonSelector)
          formPostLinkCheck(ReviewClaimController.saveAndContinue(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, pageFormSelector)
          linkCheck(expectedRemoveLinkText, removeLinkSelector, RemoveClaimController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url,
            Some(expectedRemoveLinkHiddenText), Some(removeLinkHiddenSelector))
          elementNotOnPageCheck(restoreClaimButtonSelector)
          elementNotOnPageCheck(backLinkSelector)
        }

        "HMRC added data" which {
          implicit val UserPriorAndSessionDataRequest: UserPriorAndSessionDataRequest[AnyContent] = getUserPriorAndSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val page = aReviewClaimPage.copy(isHmrcData = true)

          implicit val document: Document = Jsoup.parse(underTest(page).body)
          textOnPageCheck(expectedExternalDataText, p1)
          formPostLinkCheck(ReviewClaimController.saveAndContinue(taxYearEOY, JobSeekersAllowance, page.sessionDataId).url, pageFormSelector)
        }

        "customer override data" which {
          val priorData = aStateBenefit.copy(
            startDate = aStateBenefit.startDate.plusDays(1),
            endDate = aStateBenefit.endDate.map(_.plusDays(1)),
            amount = aStateBenefit.amount.map(_ + 1),
            taxPaid = aStateBenefit.taxPaid.map(_ + 1))
          val pageModel = aReviewClaimPage.copy(isHmrcData = false, taxYear = taxYearEOY,
            priorStartDate = Some(priorData.startDate), priorEndDate = priorData.endDate, priorAmount = priorData.amount, priorTaxPaid = priorData.taxPaid)
          implicit val UserPriorAndSessionDataRequest: UserPriorAndSessionDataRequest[AnyContent] = getUserPriorAndSessionDataRequest(userScenario.isAgent).copy(priorData = Some(priorData))
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val translatedStartDate = translatedDateFormatter(pageModel.startDate).replace("\u00A0", " ")
          val translatedEndDate = translatedDateFormatter(pageModel.endDate.get).replace("\u00A0", " ")
          val translatedPriorStartDate = translatedDateFormatter(priorData.startDate).replace("\u00A0", " ")
          val translatedPriorEndDate = translatedDateFormatter(priorData.endDate.get).replace("\u00A0", " ")

          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          welshToggleCheck(userScenario.isWelsh)
          titleCheck(expectedTitle, userScenario.isWelsh)
          captionCheck(expectedCaption(taxYearEOY))
          elementNotOnPageCheck(p1)

          textOnPageCheck(get.expectedStartDateText, summaryListRowFieldNameSelector(1))
          textOnPageCheck(s"$translatedStartDate $expectedWasText $translatedPriorStartDate", summaryListRowFieldValueSelector(1))
          linkCheck(s"$expectedChangeLinkText ${get.expectedStartDateHiddenText}", changeLink(1),
            StartDateController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(1)))
          textOnPageCheck(expectedEndDateQuestionText(taxYearEOY, pageModel.startDate), summaryListRowFieldNameSelector(2))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(2), "for the end date question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateQuestionHiddenText(taxYearEOY)}", changeLink(2),
            EndDateQuestionController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(2)))
          textOnPageCheck(expectedEndDateText, summaryListRowFieldNameSelector(3))
          textOnPageCheck(s"$translatedEndDate $expectedWasText $translatedPriorEndDate" , summaryListRowFieldValueSelector(3))
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateHiddenText}", changeLink(3),
            EndDateController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(3)))
          textOnPageCheck(get.expectedTaxPaidQuestionText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(4))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(4), "for the tax paid question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedTaxPaidQuestionHiddenText}", changeLink(4),
            TaxPaidQuestionController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(4)))
          textOnPageCheck(get.expectedAmountText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(5))
          textOnPageCheck(s"${bigDecimalCurrency(pageModel.amount.get.toString())} $expectedWasText ${bigDecimalCurrency(priorData.amount.get.toString())}", summaryListRowFieldValueSelector(5))
          linkCheck(s"$expectedChangeLinkText ${get.expectedAmountHiddenText}", changeLink(5),
            AmountController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(5)))
          textOnPageCheck(get.expectedTaxPaidText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(6))
          textOnPageCheck(s"${bigDecimalCurrency(pageModel.taxPaid.get.toString())} $expectedWasText ${bigDecimalCurrency(priorData.taxPaid.get.toString())}", summaryListRowFieldValueSelector(6))
          linkCheck(s"$expectedChangeLinkText ${get.expectedTaxPaidHiddenText}", changeLink(6),
            TaxPaidController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(6)))
          checkElementsCount(6, rowsSelector)
        }

        "customer added data with the same values" which {
          implicit val UserPriorAndSessionDataRequest: UserPriorAndSessionDataRequest[AnyContent] = getUserPriorAndSessionDataRequest(userScenario.isAgent).copy(priorData = Some(aStateBenefit))
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val translatedStartDate = translatedDateFormatter(pageModel.startDate).replace("\u00A0", " ")
          val translatedEndDate = translatedDateFormatter(pageModel.endDate.get).replace("\u00A0", " ")

          implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

          welshToggleCheck(userScenario.isWelsh)
          titleCheck(expectedTitle, userScenario.isWelsh)
          captionCheck(expectedCaption(taxYearEOY))
          elementNotOnPageCheck(p1)

          textOnPageCheck(get.expectedStartDateText, summaryListRowFieldNameSelector(1))
          textOnPageCheck(translatedStartDate, summaryListRowFieldValueSelector(1))
          linkCheck(s"$expectedChangeLinkText ${get.expectedStartDateHiddenText}", changeLink(1),
            StartDateController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(1)))
          textOnPageCheck(expectedEndDateQuestionText(taxYearEOY, pageModel.startDate), summaryListRowFieldNameSelector(2))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(2), "for the end date question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateQuestionHiddenText(taxYearEOY)}", changeLink(2),
            EndDateQuestionController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(2)))
          textOnPageCheck(expectedEndDateText, summaryListRowFieldNameSelector(3))
          textOnPageCheck(translatedEndDate, summaryListRowFieldValueSelector(3))
          linkCheck(s"$expectedChangeLinkText ${get.expectedEndDateHiddenText}", changeLink(3),
            EndDateController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(3)))
          textOnPageCheck(get.expectedTaxPaidQuestionText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(4))
          textOnPageCheck(expectedYesText, summaryListRowFieldValueSelector(4), "for the tax paid question")
          linkCheck(s"$expectedChangeLinkText ${get.expectedTaxPaidQuestionHiddenText}", changeLink(4),
            TaxPaidQuestionController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(4)))
          textOnPageCheck(get.expectedAmountText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(5))
          textOnPageCheck(bigDecimalCurrency(pageModel.amount.get.toString()), summaryListRowFieldValueSelector(5))
          linkCheck(s"$expectedChangeLinkText ${get.expectedAmountHiddenText}", changeLink(5),
            AmountController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(5)))
          textOnPageCheck(get.expectedTaxPaidText(translatedStartDate, translatedEndDate), summaryListRowFieldNameSelector(6))
          textOnPageCheck(bigDecimalCurrency(pageModel.taxPaid.get.toString()), summaryListRowFieldValueSelector(6))
          linkCheck(s"$expectedChangeLinkText ${get.expectedTaxPaidHiddenText}", changeLink(6),
            TaxPaidController.show(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, Some(hiddenChangeLink(6)))
          checkElementsCount(6, rowsSelector)
        }

        "the claim is ignored" which {
          implicit val UserPriorAndSessionDataRequest: UserPriorAndSessionDataRequest[AnyContent] = getUserPriorAndSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val page = pageModel.copy(isIgnored = true)

          implicit val document: Document = Jsoup.parse(underTest(page).body)
          buttonCheck(expectedRestoreClaimButtonText, restoreClaimButtonSelector)
          linkCheck(expectedBackText, backLinkSelector, ClaimsController.show(taxYearEOY, JobSeekersAllowance).url)
          elementNotOnPageCheck(changeLink(1))
          elementNotOnPageCheck(changeLink(2))
          elementNotOnPageCheck(changeLink(3))
          elementNotOnPageCheck(changeLink(4))
          elementNotOnPageCheck(changeLink(5))
          formPostLinkCheck(ReviewClaimController.restoreClaim(taxYearEOY, JobSeekersAllowance, page.sessionDataId).url, pageFormSelector)
        }
      }

      "render in year version of ReviewJobSeekersAllowanceClaim page" when {
        "HMRC added data" which {
          implicit val UserPriorAndSessionDataRequest: UserPriorAndSessionDataRequest[AnyContent] = getUserPriorAndSessionDataRequest(userScenario.isAgent)
          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val page = pageModel.copy(taxYear = taxYear, isInYear = true)
          val translatedStartDate = translatedDateFormatter(page.startDate).replace("\u00A0", " ")

          implicit val document: Document = Jsoup.parse(underTest(page).body)

          welshToggleCheck(userScenario.isWelsh)
          titleCheck(expectedTitle, userScenario.isWelsh)
          captionCheck(expectedCaption(taxYear))
          elementNotOnPageCheck(p1)

          textOnPageCheck(get.expectedStartDateText, summaryListRowFieldNameSelector(1))
          textOnPageCheck(translatedStartDate, summaryListRowFieldValueSelector(1))
          elementNotOnPageCheck(changeLink(1))
          checkElementsCount(count = 1, rowsSelector)
          linkCheck(expectedContinueButtonText, continueButtonSelector, ClaimsController.show(taxYear, JobSeekersAllowance).url)
          elementNotOnPageCheck(restoreClaimButtonSelector)
          elementNotOnPageCheck(backLinkSelector)
        }
      }
    }
  }
}
