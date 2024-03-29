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

import controllers.routes.EndDateQuestionController
import forms.{FormsProvider, YesNoForm}
import models.BenefitType.EmploymentSupportAllowance
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.pages.EndDateQuestionPageBuilder.aEndDateQuestionPage
import utils.ViewUtils.{translatedDateFormatter, translatedTaxYearEndDateFormatter}
import views.html.pages.EndDateQuestionPageView

import java.time.LocalDate

class EndDateQuestionPageViewSpec extends ViewUnitTest {

  private val formsProvider = new FormsProvider()
  private val underTest: EndDateQuestionPageView = inject[EndDateQuestionPageView]

  object Selectors {
    val continueButtonFormSelector = "#main-content > div > div > form"
    val errorHref = "#value"
    val buttonSelector: String = "#continue"
  }

  trait CommonExpectedResults {
    val expectedYesText: String
    val expectedNoText: String
    val expectedButtonText: String

    def expectedHeading(taxYear: Int, startDate: LocalDate): String

    def expectedTitle(taxYear: Int, startDate: LocalDate): String

    def expectedErrorTitle(taxYear: Int, startDate: LocalDate): String

    def expectedErrorText(taxYear: Int, startDate: LocalDate): String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedButtonText: String = "Continue"

    override def expectedHeading(taxYear: Int, startDate: LocalDate): String =
      s"Did this claim end between ${translatedDateFormatter(startDate)(defaultMessages)} and ${translatedTaxYearEndDateFormatter(taxYear)(defaultMessages)}?"
        .replace("\u00A0", " ")

    override def expectedTitle(taxYear: Int, startDate: LocalDate): String = expectedHeading(taxYear, startDate)

    override def expectedErrorTitle(taxYear: Int, startDate: LocalDate): String = s"Error: ${expectedTitle(taxYear, startDate)}"

    override def expectedErrorText(taxYear: Int, startDate: LocalDate): String =
      s"Select yes if this claim ended between ${translatedDateFormatter(startDate)(defaultMessages)} and ${translatedTaxYearEndDateFormatter(taxYear)(defaultMessages)}"
        .replace("\u00A0", " ")
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedButtonText: String = "Yn eich blaen"

    override def expectedHeading(taxYear: Int, startDate: LocalDate): String =
      s"A wnaeth yr hawliad hwn ddod i ben rhwng ${translatedDateFormatter(startDate)(welshMessages)} a ${translatedTaxYearEndDateFormatter(taxYear)(welshMessages)}?"
        .replace("\u00A0", " ")

    override def expectedTitle(taxYear: Int, startDate: LocalDate): String = expectedHeading(taxYear, startDate)

    override def expectedErrorTitle(taxYear: Int, startDate: LocalDate): String = s"Gwall: ${expectedTitle(taxYear, startDate)}"

    override def expectedErrorText(taxYear: Int, startDate: LocalDate): String =
      s"Dewiswch ‘Iawn’ os daeth yr hawliad hwn i ben rhwng ${translatedDateFormatter(startDate)(welshMessages)} a ${translatedTaxYearEndDateFormatter(taxYear)(welshMessages)}"
        .replace("\u00A0", " ")
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, _]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, None),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, None)
  )

  userScenarios.foreach { userScenario =>
    import Selectors._
    import userScenario.commonExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)}" should {
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aEndDateQuestionPage.copy(benefitType = EmploymentSupportAllowance)
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedTitle(taxYearEOY, pageModel.titleFirstDate), userScenario.isWelsh)
        h1Check(expectedHeading(taxYearEOY, pageModel.titleFirstDate), isFieldSetH1 = true)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(expectedNoText, radioNumber = 2, checked = false)
        formPostLinkCheck(EndDateQuestionController.submit(taxYearEOY, EmploymentSupportAllowance, pageModel.sessionDataId).url, continueButtonFormSelector)
        buttonCheck(expectedButtonText, buttonSelector)
      }

      "render page with filled in form using selected 'Yes' value" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aEndDateQuestionPage.copy(benefitType = EmploymentSupportAllowance, form = aEndDateQuestionPage.form.fill(value = true))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(expectedTitle(taxYearEOY, pageModel.titleFirstDate), userScenario.isWelsh)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = true)
      }

      "render page with empty selection error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.endDateYesNoForm(taxYearEOY, aClaimCYAModel.startDate)
        val pageModel = aEndDateQuestionPage.copy(benefitType = EmploymentSupportAllowance, form = form.bind(Map(YesNoForm.yesNo -> "")))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(expectedErrorTitle(taxYearEOY, aEndDateQuestionPage.titleFirstDate), userScenario.isWelsh)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(expectedNoText, radioNumber = 2, checked = false)

        errorSummaryCheck(expectedErrorText(taxYearEOY, aEndDateQuestionPage.titleFirstDate), errorHref)
        errorAboveElementCheck(expectedErrorText(taxYearEOY, aEndDateQuestionPage.titleFirstDate), userScenario.isWelsh)
      }

      "render page with wrong data error" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form = formsProvider.endDateYesNoForm(taxYearEOY, aClaimCYAModel.startDate)
        val pageModel = aEndDateQuestionPage.copy(benefitType = EmploymentSupportAllowance, form = form.bind(Map("wrongKey" -> "wrongValue")))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(expectedErrorTitle(taxYearEOY, pageModel.titleFirstDate), userScenario.isWelsh)
        radioButtonCheck(expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(expectedNoText, radioNumber = 2, checked = false)

        errorSummaryCheck(expectedErrorText(taxYearEOY, pageModel.titleFirstDate), errorHref)
        errorAboveElementCheck(expectedErrorText(taxYearEOY, pageModel.titleFirstDate), userScenario.isWelsh)
      }
    }
  }
}
