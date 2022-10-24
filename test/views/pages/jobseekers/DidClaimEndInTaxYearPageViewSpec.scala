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

import controllers.jobseekers.routes.DidClaimEndInTaxYearController
import forms.YesNoForm
import models.requests.UserSessionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import support.builders.pages.jobseekers.DidClaimEndInTaxYearPageBuilder.aDidClaimEndInTaxYearPage
import views.html.pages.jobseekers.DidClaimEndInTaxYearPageView

class DidClaimEndInTaxYearPageViewSpec extends ViewUnitTest {

  private val underTest: DidClaimEndInTaxYearPageView = inject[DidClaimEndInTaxYearPageView]

  object Selectors {
    val continueButtonFormSelector = "#main-content > div > div > form"
    val expectedErrorHref = "#value"
    val buttonSelector: String = "#continue"
  }

  trait CommonExpectedResults {
    val expectedHeading: Int => String
    val expectedTitle: Int => String
    val expectedCaption: Int => String
    val expectedYesText: String
    val expectedNoText: String
    val expectedButtonText: String
    val expectedErrorText: Int => String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedHeading: Int => String = (taxYear: Int) => s"Did this claim end in the tax year ending 5 April $taxYear?"
    override val expectedTitle: Int => String = expectedHeading
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedButtonText: String = "Continue"
    override val expectedErrorText: Int => String = (taxYear: Int) => s"Select yes if this claim ended in the tax year ending 5 April $taxYear"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedHeading: Int => String = (taxYear: Int) => s"Did this claim end in the tax year ending 5 April $taxYear?"
    override val expectedTitle: Int => String = expectedHeading
    override val expectedCaption: Int => String = (taxYear: Int) => s"Jobseeker’s Allowance for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedButtonText: String = "Continue"
    override val expectedErrorText: Int => String = (taxYear: Int) => s"Select yes if this claim ended in the tax year ending 5 April $taxYear"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, _]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, None),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, None)
  )

  userScenarios.foreach { userScenario =>
    import userScenario.commonExpectedResults._
    s"language is ${welshTest(userScenario.isWelsh)}" should {
      "render page with empty form" which {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(underTest(aDidClaimEndInTaxYearPage).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle(taxYearEOY), userScenario.isWelsh)
        captionCheck(expectedCaption(taxYearEOY))
        h1Check(userScenario.commonExpectedResults.expectedHeading(taxYearEOY))
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)
        formPostLinkCheck(DidClaimEndInTaxYearController.submit(taxYearEOY, aDidClaimEndInTaxYearPage.sessionDataId).url, Selectors.continueButtonFormSelector)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.buttonSelector)
      }

      "render page with filled in form using selected 'Yes' value" ignore {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aDidClaimEndInTaxYearPage.copy(form = aDidClaimEndInTaxYearPage.form.fill(value = true))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedTitle(taxYearEOY), userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = true)
      }

      "render page with empty selection error" ignore {
        implicit val userSessionDataRequest: UserSessionDataRequest[AnyContent] = getUserSessionDataRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val pageModel = aDidClaimEndInTaxYearPage.copy(form = aDidClaimEndInTaxYearPage.form.bind(Map(YesNoForm.yesNo -> "")))
        implicit val document: Document = Jsoup.parse(underTest(pageModel).body)

        titleCheck(userScenario.commonExpectedResults.expectedTitle(taxYearEOY), userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, radioNumber = 2, checked = false)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText(taxYearEOY), Selectors.expectedErrorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText(taxYearEOY))
      }
    }
  }
}