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

import controllers.routes.ReviewClaimController
import models.BenefitType.JobSeekersAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.RemoveClaimPageBuilder.aRemoveClaimPage
import utils.ViewUtils.{bigDecimalCurrency, translatedDateFormatter}
import views.html.pages.RemoveClaimPageView

import java.time.LocalDate

class RemoveClaimPageViewSpec extends ViewUnitTest {

  private object Selectors {
    val startDateRowKeySelector = "#main-content > div > div > dl > div:nth-child(1) > dt"
    val startDateRowValueSelector = "#main-content > div > div > dl > div:nth-child(1) > dd"
    val endDateQuestionRowKeySelector = "#main-content > div > div > dl > div:nth-child(2) > dt"
    val endDateQuestionRowValueSelector = "#main-content > div > div > dl > div:nth-child(2) > dd"
    val endDateRowKeySelector = "#main-content > div > div > dl > div:nth-child(3) > dt"
    val endDateRowValueSelector = "#main-content > div > div > dl > div:nth-child(3) > dd"
    val amountRowKeySelector = "#main-content > div > div > dl > div:nth-child(4) > dt"
    val amountRowValueSelector = "#main-content > div > div > dl > div:nth-child(4) > dd"
    val taxPaidQuestionRowKeySelector = "#main-content > div > div > dl > div:nth-child(5) > dt"
    val taxPaidQuestionRowValueSelector = "#main-content > div > div > dl > div:nth-child(5) > dd"
    val taxPaidRowKeySelector = "#main-content > div > div > dl > div:nth-child(6) > dt"
    val taxPaidRowValueSelector = "#main-content > div > div > dl > div:nth-child(6) > dd"
    val rowsSelector = "#main-content > div > div > dl > div"
    val buttonSelector = "#remove-claim-button-id"
    val linkSelector = "#do-not-remove-claim-link-id"
    val removeLinkHiddenSelector = "#do-not-remove-claim-link-id > span.govuk-visually-hidden"
  }

  private val underTest: RemoveClaimPageView = inject[RemoveClaimPageView]

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String

    val expectedEndDateRowKey: String
    val expectedEndDateQuestionRowValue: String
    val expectedEndDateRowValue: String

    val expectedStartDateRowValue: String
    val expectedAmountRowValue: String
    val expectedTaxPaidQuestionRowValue: String
    val expectedTaxPaidRowValue: String

    val removeButton: String
    val doNotRemoveLink: String
    val doNotRemoveLinkHiddenText: String

    val no: String

    def expectedEndDateQuestionRowKey(taxYear: Int, startDate: LocalDate): String
  }

  trait SpecificExpectedResults {
    val expectedStartDateRowKey: String
    val expectedAmountRowKey: (LocalDate, LocalDate) => String
    val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String
    val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val expectedHeading: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val expectedEndDateRowKey: String = "When did this claim end?"
    override val expectedEndDateQuestionRowValue: String = "Yes"
    override val expectedEndDateRowValue: String = s"13 August $taxYearEOY"
    override val removeButton: String = "Remove claim"
    override val doNotRemoveLink: String = "Don’t remove claim"
    override val doNotRemoveLinkHiddenText: String = "Don’t remove this Jobseeker’s Allowance Claim"
    override val expectedStartDateRowValue: String = s"23 April ${aClaimCYAModel.startDate.getYear}"
    override val expectedAmountRowValue: String = bigDecimalCurrency(aClaimCYAModel.amount.get.toString())
    override val expectedTaxPaidQuestionRowValue: String = "Yes"
    override val expectedTaxPaidRowValue: String = bigDecimalCurrency(aClaimCYAModel.taxPaid.get.toString())
    override val no = "No"

    override def expectedEndDateQuestionRowKey(taxYear: Int, startDate: LocalDate): String = s"Did this claim end between ${translatedDateFormatter(startDate)(defaultMessages)} and 5 April $taxYear?"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val expectedHeading: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val expectedEndDateRowKey: String = "When did this claim end?"
    override val expectedEndDateQuestionRowValue: String = "Iawn"
    override val expectedEndDateRowValue: String = s"13 Awst $taxYearEOY"
    override val removeButton: String = "Remove claim"
    override val doNotRemoveLink: String = "Don’t remove claim"
    override val doNotRemoveLinkHiddenText: String = "Don’t remove this Jobseeker’s Allowance Claim"
    override val expectedStartDateRowValue: String = s"23 Ebrill ${aClaimCYAModel.startDate.getYear}"
    override val expectedAmountRowValue: String = bigDecimalCurrency(aClaimCYAModel.amount.get.toString())
    override val expectedTaxPaidQuestionRowValue: String = "Iawn"
    override val expectedTaxPaidRowValue: String = bigDecimalCurrency(aClaimCYAModel.taxPaid.get.toString())
    override val no = "Na"

    override def expectedEndDateQuestionRowKey(taxYear: Int, startDate: LocalDate): String = s"Did this claim end between ${translatedDateFormatter(startDate)(welshMessages)} and 5 April $taxYear?"
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"How much Jobseeker’s Allowance did your client get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your client’s Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "When did your client start getting Jobseeker’s Allowance?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did your client get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your client’s Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did you get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "When did you start getting Jobseeker’s Allowance?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did you get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(IndividualSpecificExpectedEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(AgentSpecificExpectedEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(IndividualSpecificExpectedCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(AgentSpecificExpectedCY))
  )

  userScenarios.foreach { userScenario =>
    import Selectors._
    import userScenario.commonExpectedResults._
    import userScenario.specificExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${userScenario.isAgent}" should {
      "render page with a 'full' claim" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(aRemoveClaimPage).body)
        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        textOnPageCheck(get.expectedStartDateRowKey, startDateRowKeySelector)
        textOnPageCheck(expectedStartDateRowValue, startDateRowValueSelector)
        textOnPageCheck(expectedEndDateQuestionRowKey(taxYearEOY, aRemoveClaimPage.startDate), endDateQuestionRowKeySelector)
        textOnPageCheck(expectedEndDateQuestionRowValue, endDateQuestionRowValueSelector, "duplicate")
        textOnPageCheck(expectedEndDateRowKey, endDateRowKeySelector)
        textOnPageCheck(expectedEndDateRowValue, endDateRowValueSelector)
        textOnPageCheck(get.expectedAmountRowKey(aRemoveClaimPage.itemsFirstDate, aRemoveClaimPage.itemsSecondDate), amountRowKeySelector)
        textOnPageCheck(expectedAmountRowValue, amountRowValueSelector)
        textOnPageCheck(get.expectedTaxPaidQuestionRowKey(
          aRemoveClaimPage.itemsFirstDate, aRemoveClaimPage.itemsSecondDate), taxPaidQuestionRowKeySelector
        )
        textOnPageCheck(expectedTaxPaidQuestionRowValue, taxPaidQuestionRowValueSelector)
        textOnPageCheck(get.expectedTaxPaidRowKey(aRemoveClaimPage.itemsFirstDate, aRemoveClaimPage.itemsSecondDate), taxPaidRowKeySelector)
        textOnPageCheck(expectedTaxPaidRowValue, taxPaidRowValueSelector)
        checkElementsCount(6, rowsSelector)
        buttonCheck(removeButton, buttonSelector)
        linkCheck(doNotRemoveLink, linkSelector, ReviewClaimController.show(taxYearEOY, JobSeekersAllowance, aRemoveClaimPage.sessionDataId).url,
          Some(doNotRemoveLinkHiddenText), Some(removeLinkHiddenSelector))
      }

      "render page with a 'min' claim" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(aRemoveClaimPage.copy(endDateQuestion = Some(false), taxPaidQuestion = Some(false))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.no, endDateQuestionRowValueSelector)
        textOnPageCheck(userScenario.commonExpectedResults.no, amountRowValueSelector, "duplicate")
        checkElementsCount(4, rowsSelector)
        buttonCheck(removeButton, buttonSelector)
        linkCheck(doNotRemoveLink, linkSelector, ReviewClaimController.show(taxYearEOY, JobSeekersAllowance, aRemoveClaimPage.sessionDataId).url,
          Some(doNotRemoveLinkHiddenText), Some(removeLinkHiddenSelector))
      }
    }
  }
}
