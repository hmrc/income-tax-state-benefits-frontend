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

import controllers.routes.TaxPaidController
import forms.{AmountForm, FormsProvider}
import models.BenefitType.JobSeekersAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.TaxPaidPageBuilder.aTaxPaidPage
import utils.ViewUtils.translatedDateFormatter
import views.html.pages.TaxPaidPageView

import java.time.LocalDate

class TaxPaidPageViewSpec extends ViewUnitTest {

  private val underTest: TaxPaidPageView = inject[TaxPaidPageView]

  object Selectors {
    val paragraphTextSelector = "#main-content > div > div > p"
    val continueButtonFormSelector = "#main-content > div > div > form"
    val errorHref = "#amount"
    val buttonSelector: String = "#continue"
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHintText: String
    val expectedLabelText: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of tax taken off"
    override val expectedButtonText: String = "Continue"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHintText: String = "For example, £123.56"
    override val expectedLabelText: String = "Amount of tax taken off"
    override val expectedButtonText: String = "Continue"
  }

  trait SpecificExpectedResults {
    val expectedTitle: (LocalDate, LocalDate) => String
    val expectedErrorTitle: (LocalDate, LocalDate) => String
    val expectedP1Text: String
    val expectedErrorText: String

    def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String
  }

  object AgentSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 your client got after their claim ended."
    override val expectedErrorText: String = "Enter the amount of tax taken off your client’s Jobseeker’s Allowance"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = s"How much tax was taken off your client’s Jobseeker’s Allowance between " +
      s"${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
  }

  object AgentSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 your client got after their claim ended."
    override val expectedErrorText: String = "Enter the amount of tax taken off your client’s Jobseeker’s Allowance"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String = s"How much tax was taken off your client’s Jobseeker’s Allowance between " +
      s"${translatedDateFormatter(firstDate)(welshMessages)} and ${translatedDateFormatter(secondDate)(welshMessages)}?"
  }

  object IndividualSpecificExpectedEN extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 you got after your claim ended."
    override val expectedErrorText: String = "Enter the amount of tax taken off your Jobseeker’s Allowance"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String =
      s"How much tax was taken off your Jobseeker’s Allowance between ${translatedDateFormatter(firstDate)(defaultMessages)} and ${translatedDateFormatter(secondDate)(defaultMessages)}?"
  }

  object IndividualSpecificExpectedCY extends SpecificExpectedResults {
    override val expectedTitle: (LocalDate, LocalDate) => String = expectedHeading
    override val expectedErrorTitle: (LocalDate, LocalDate) => String = (startDate: LocalDate, endDate: LocalDate) => "Error: " + expectedHeading(startDate, endDate)
    override val expectedP1Text: String = "This amount will be on the P45 you got after your claim ended."
    override val expectedErrorText: String = "Enter the amount of tax taken off your Jobseeker’s Allowance"

    override def expectedHeading(firstDate: LocalDate, secondDate: LocalDate): String =
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
      val pageForm = new FormsProvider().taxPaidAmountForm(JobSeekersAllowance, isAgent = userScenario.isAgent, maxAmount = aClaimCYAModel.amount.get - 1)
      val pageModel = aTaxPaidPage.copy(form = pageForm)
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate), userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(get.expectedTitle(pageModel.titleFirstDate, pageModel.titleSecondDate))
        textOnPageCheck(get.expectedP1Text, paragraphTextSelector)
        amountBoxLabelCheck(expectedLabelText)
        amountBoxHintCheck(expectedHintText)
        formPostLinkCheck(TaxPaidController.submit(taxYearEOY, JobSeekersAllowance, pageModel.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)
        val page = pageModel.copy(form = pageModel.form.bind(Map(AmountForm.amount -> "")))
        implicit val document: Document = Jsoup.parse(underTest(page).body)

        titleCheck(get.expectedErrorTitle(page.titleFirstDate, page.titleSecondDate), userScenario.isWelsh)

        errorSummaryCheck(get.expectedErrorText, errorHref)
        errorAboveElementCheck(get.expectedErrorText)
      }
    }
  }
}
