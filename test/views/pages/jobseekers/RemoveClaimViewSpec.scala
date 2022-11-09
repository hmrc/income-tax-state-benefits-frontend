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

import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.jobseekers.RemovePageBuilder.{removeFullClaimPage, removeMinClaimPage}
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.jobseekers.RemoveClaimView

import java.time.LocalDate

class RemoveClaimViewSpec extends ViewUnitTest {
  
  private object Selectors {
    val firstRowKeySelector = "#main-content > div > div > dl > div:nth-child(1) > dt"
    val firstRowValueSelector = "#main-content > div > div > dl > div:nth-child(1) > dd"
    val secondRowKeySelector = "#main-content > div > div > dl > div:nth-child(2) > dt"
    val secondRowValueSelector = "#main-content > div > div > dl > div:nth-child(2) > dd"
    val thirdRowKeySelector = "#main-content > div > div > dl > div:nth-child(3) > dt"
    val thirdRowValueSelector = "#main-content > div > div > dl > div:nth-child(3) > dd"
    val fourthRowKeySelector = "#main-content > div > div > dl > div:nth-child(4) > dt"
    val fourthRowValueSelector = "#main-content > div > div > dl > div:nth-child(4) > dd"
    val fifthRowKeySelector = "#main-content > div > div > dl > div:nth-child(5) > dt"
    val fifthRowValueSelector = "#main-content > div > div > dl > div:nth-child(5) > dd"
    val sixthRowKeySelector = "#main-content > div > div > dl > div:nth-child(6) > dt"
    val sixthRowValueSelector = "#main-content > div > div > dl > div:nth-child(6) > dd"
  }

  private val underTest: RemoveClaimView = inject[RemoveClaimView]

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedTitle: String
    val expectedHeading: String

    val secondRowKey: String
    val thirdRowKey: String
    val secondRowValue: String
    val thirdRowValue: String

    val firstRowValue: String
    val fourthRowValue: String
    val fifthRowValue: String
    val sixthRowValue: String

    val removeButton: String
    val doNotRemoveLink: String

    val no: String
  }

  trait SpecificExpectedResults {

    val firstRowKey: String
    val fourthRowKey: (LocalDate, LocalDate) => String
    val fifthRowKey: (LocalDate, LocalDate) => String
    val sixthRowKey: (LocalDate, LocalDate) => String

  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedTitle: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val expectedHeading: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val secondRowKey: String = "Did this claim end in the tax year ending 5 April 2022?"
    override val thirdRowKey: String = "When did this claim end?"
    override val secondRowValue: String = "Yes"
    override val thirdRowValue: String = s"13 August $taxYearEOY"
    override val removeButton: String = "Remove claim"
    override val doNotRemoveLink: String = "Don’t remove claim"
    override val firstRowValue: String = s"23 April $taxYearEOY"
    override val fourthRowValue: String = "£100"
    override val fifthRowValue: String = "Yes"
    override val sixthRowValue: String = "£50"
    override val no = "No"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedTitle: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val expectedHeading: String = "Are you sure you want to remove this Jobseeker’s Allowance claim?"
    override val secondRowKey: String = "Did this claim end in the tax year ending 5 April 2022?"
    override val thirdRowKey: String = "When did this claim end?"
    override val secondRowValue: String = "Iawn"
    override val thirdRowValue: String = s"13 Awst $taxYearEOY"
    override val removeButton: String = "Remove claim"
    override val doNotRemoveLink: String = "Don’t remove claim"
    override val firstRowValue: String = s"23 Ebrill $taxYearEOY"
    override val fourthRowValue: String = "£100"
    override val fifthRowValue: String = "Iawn"
    override val sixthRowValue: String = "£50"
    override val no = "Na"
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val firstRowKey: String = "When did your client start getting Jobseeker’s Allowance?"
    override val fourthRowKey : (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did your client get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val fifthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val sixthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your client’s Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val firstRowKey: String = "When did your client start getting Jobseeker’s Allowance?"
    override val fourthRowKey : (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did your client get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val fifthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did your client have any tax taken off their Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val sixthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your client’s Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val firstRowKey: String = "When did you start getting Jobseeker’s Allowance?"
    override val fourthRowKey : (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did you get between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val fifthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
    override val sixthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val firstRowKey: String = "When did you start getting Jobseeker’s Allowance?"
    override val fourthRowKey : (LocalDate, LocalDate) => String = (firstDate: LocalDate, secondDate) =>
      s"How much Jobseeker’s Allowance did you get between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val fifthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"Did you have any tax taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
    override val sixthRowKey: (LocalDate, LocalDate) => String = (firstDate, secondDate) =>
      s"How much tax was taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(IndividualSpecificExpectedEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(AgentSpecificExpectedEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(IndividualSpecificExpectedCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(AgentSpecificExpectedCY))
  )

  userScenarios.foreach { userScenario =>
    import userScenario.commonExpectedResults._

    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${userScenario.isAgent}" should {
      "render page with a 'full' claim" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(removeFullClaimPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.commonExpectedResults.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.firstRowKey, Selectors.firstRowKeySelector)
        textOnPageCheck(userScenario.commonExpectedResults.firstRowValue, Selectors.firstRowValueSelector)
        textOnPageCheck(userScenario.commonExpectedResults.secondRowKey, Selectors.secondRowKeySelector)
        textOnPageCheck(userScenario.commonExpectedResults.secondRowValue, Selectors.secondRowValueSelector, "duplicate")
        textOnPageCheck(userScenario.commonExpectedResults.thirdRowKey, Selectors.thirdRowKeySelector)
        textOnPageCheck(userScenario.commonExpectedResults.thirdRowValue, Selectors.thirdRowValueSelector)
        textOnPageCheck(userScenario.specificExpectedResults.get.fourthRowKey(removeFullClaimPage.titleFirstDate, removeFullClaimPage.titleSecondDate), Selectors.fourthRowKeySelector)
        textOnPageCheck(userScenario.commonExpectedResults.fourthRowValue, Selectors.fourthRowValueSelector)
        textOnPageCheck(userScenario.specificExpectedResults.get.fifthRowKey(removeFullClaimPage.titleFirstDate, removeFullClaimPage.titleSecondDate), Selectors.fifthRowKeySelector)
        textOnPageCheck(userScenario.commonExpectedResults.fifthRowValue, Selectors.fifthRowValueSelector)
        textOnPageCheck(userScenario.specificExpectedResults.get.sixthRowKey(removeFullClaimPage.titleFirstDate, removeFullClaimPage.titleSecondDate), Selectors.sixthRowKeySelector)
        textOnPageCheck(userScenario.commonExpectedResults.sixthRowValue, Selectors.sixthRowValueSelector)

      }
      "render page with a 'min' claim" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(removeMinClaimPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.no, Selectors.secondRowValueSelector)
        textOnPageCheck(userScenario.commonExpectedResults.no, Selectors.fourthRowValueSelector ,"duplicate")
      }
    }
  }

  }
