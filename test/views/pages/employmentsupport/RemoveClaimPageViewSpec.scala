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

import controllers.routes.ReviewClaimController
import models.BenefitType.EmploymentSupportAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.RemoveClaimPageBuilder.aRemoveClaimPage
import utils.ViewUtils.{bigDecimalCurrency, translatedDateFormatter, translatedTaxYearEndDateFormatter}
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
    override val expectedTitle: String = "Are you sure you want to remove this Employment and Support Allowance claim?"
    override val expectedHeading: String = "Are you sure you want to remove this Employment and Support Allowance claim?"
    override val expectedEndDateRowKey: String = "When did this claim end?"
    override val expectedEndDateQuestionRowValue: String = "Yes"
    override val expectedEndDateRowValue: String = s"13 August $taxYearEOY"
    override val removeButton: String = "Remove claim"
    override val doNotRemoveLink: String = "Don’t remove claim"
    override val doNotRemoveLinkHiddenText: String = "Don’t remove this Employment and Support Allowance Claim"
    override val expectedStartDateRowValue: String = s"23 April ${aClaimCYAModel.startDate.getYear}"
    override val expectedAmountRowValue: String = bigDecimalCurrency(aClaimCYAModel.amount.get.toString())
    override val expectedTaxPaidQuestionRowValue: String = "Yes"
    override val expectedTaxPaidRowValue: String = bigDecimalCurrency(aClaimCYAModel.taxPaid.get.toString())
    override val no = "No"

    override def expectedEndDateQuestionRowKey(taxYear: Int, startDate: LocalDate): String =
      s"Did this claim end between ${translatedDateFormatter(startDate)(defaultMessages)} and ${translatedTaxYearEndDateFormatter(taxYear)(defaultMessages)}?"
        .replace("\u00A0", " ")
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "A ydych yn siŵr eich bod am dynnu’r hawliad Lwfans Cyflogaeth a Chymorth hwn?"
    override val expectedHeading: String = "A ydych yn siŵr eich bod am dynnu’r hawliad Lwfans Cyflogaeth a Chymorth hwn?"
    override val expectedEndDateRowKey: String = "Pryd y daeth yr hawliad hwn i ben?"
    override val expectedEndDateQuestionRowValue: String = "Iawn"
    override val expectedEndDateRowValue: String = s"13 Awst $taxYearEOY"
    override val removeButton: String = "Tynnu’r hawliad"
    override val doNotRemoveLink: String = "Peidiwch â thynnu’r hawliad"
    override val doNotRemoveLinkHiddenText: String = "Peidiwch â thynnu’r hawliad Lwfans Cyflogaeth a Chymorth hwn"
    override val expectedStartDateRowValue: String = s"23 Ebrill ${aClaimCYAModel.startDate.getYear}"
    override val expectedAmountRowValue: String = bigDecimalCurrency(aClaimCYAModel.amount.get.toString())
    override val expectedTaxPaidQuestionRowValue: String = "Iawn"
    override val expectedTaxPaidRowValue: String = bigDecimalCurrency(aClaimCYAModel.taxPaid.get.toString())
    override val no = "Na"

    override def expectedEndDateQuestionRowKey(taxYear: Int, startDate: LocalDate): String =
      s"A wnaeth yr hawliad hwn ddod i ben rhwng ${translatedDateFormatter(startDate)(welshMessages)} a ${translatedTaxYearEndDateFormatter(taxYear)(welshMessages)}?"
        .replace("\u00A0", " ")
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "When did your client start getting Employment and Support Allowance?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"How much Employment and Support Allowance did your client get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"Did your client have any tax taken off their Employment and Support Allowance between " +
        s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
          .replace("\u00A0", " ")
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"How much tax was taken off your client’s Employment and Support Allowance between " +
        s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
          .replace("\u00A0", " ")
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "Pryd y gwnaeth eich cleient ddechrau cael Lwfans Cyflogaeth a Chymorth?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) => s"Faint o Lwfans Cyflogaeth a Chymorth a gafodd eich cleient rhwng " +
      s"${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"A ddidynnwyd unrhyw dreth o Lwfans Cyflogaeth a Chymorth eich cleient rhwng " +
        s"${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
          .replace("\u00A0", " ")
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"Faint o dreth a gafodd ei didynnu o Lwfans Cyflogaeth a Chymorth eich cleient rhwng " +
        s"${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
          .replace("\u00A0", " ")
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "When did you start getting Employment and Support Allowance?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) => s"How much Employment and Support Allowance did you get between " +
      s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"Did you have any tax taken off your Employment and Support Allowance between " +
        s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
          .replace("\u00A0", " ")
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"How much tax was taken off your Employment and Support Allowance between " +
        s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
          .replace("\u00A0", " ")
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedStartDateRowKey: String = "Pryd y gwnaethoch ddechrau cael Lwfans Cyflogaeth a Chymorth?"
    override val expectedAmountRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"Faint o Lwfans Cyflogaeth a Chymorth a gawsoch rhwng ${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTaxPaidQuestionRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"A ddidynnwyd unrhyw dreth o’ch Lwfans Cyflogaeth a Chymorth rhwng ${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")
    override val expectedTaxPaidRowKey: (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate: LocalDate) =>
      s"Faint o dreth a gafodd ei didynnu o’ch Lwfans Cyflogaeth a Chymorth rhwng ${translatedDateFormatter(firstDate)(welshMessages)} a ${translatedDateFormatter(secondDate)(welshMessages)}?"
        .replace("\u00A0", " ")
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
      val pageModel = aRemoveClaimPage.copy(benefitType = EmploymentSupportAllowance)
      "render page with a 'full' claim" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)
        textOnPageCheck(get.expectedStartDateRowKey, startDateRowKeySelector)
        textOnPageCheck(expectedStartDateRowValue, startDateRowValueSelector)
        textOnPageCheck(expectedEndDateQuestionRowKey(taxYearEOY, aRemoveClaimPage.startDate), endDateQuestionRowKeySelector)
        textOnPageCheck(expectedEndDateQuestionRowValue, endDateQuestionRowValueSelector, "duplicate")
        textOnPageCheck(expectedEndDateRowKey, endDateRowKeySelector)
        textOnPageCheck(expectedEndDateRowValue, endDateRowValueSelector)
        textOnPageCheck(get.expectedAmountRowKey(pageModel.itemsFirstDate, pageModel.itemsSecondDate), amountRowKeySelector)
        textOnPageCheck(expectedAmountRowValue, amountRowValueSelector)
        textOnPageCheck(get.expectedTaxPaidQuestionRowKey(
          pageModel.itemsFirstDate, pageModel.itemsSecondDate), taxPaidQuestionRowKeySelector
        )
        textOnPageCheck(expectedTaxPaidQuestionRowValue, taxPaidQuestionRowValueSelector)
        textOnPageCheck(get.expectedTaxPaidRowKey(pageModel.itemsFirstDate, pageModel.itemsSecondDate), taxPaidRowKeySelector)
        textOnPageCheck(expectedTaxPaidRowValue, taxPaidRowValueSelector)
        checkElementsCount(6, rowsSelector)
        buttonCheck(removeButton, buttonSelector)
        linkCheck(doNotRemoveLink, linkSelector, ReviewClaimController.show(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url,
          Some(doNotRemoveLinkHiddenText), Some(removeLinkHiddenSelector))
      }

      "render page with a 'min' claim" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(pageModel.copy(endDateQuestion = Some(false), taxPaidQuestion = Some(false))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle, userScenario.isWelsh)
        h1Check(expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.no, endDateQuestionRowValueSelector)
        textOnPageCheck(userScenario.commonExpectedResults.no, amountRowValueSelector, "duplicate")
        checkElementsCount(4, rowsSelector)
        buttonCheck(removeButton, buttonSelector)
        linkCheck(doNotRemoveLink, linkSelector, ReviewClaimController.show(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url,
          Some(doNotRemoveLinkHiddenText), Some(removeLinkHiddenSelector))
      }
    }
  }
}
