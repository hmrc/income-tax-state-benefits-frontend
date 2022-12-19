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

package controllers.jobseekers

import controllers.routes.SummaryController
import forms.YesNoForm
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.UserBuilder.aUser

class SectionCompletedQuestionControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String =
    s"/update-and-submit-income-tax-return/state-benefits/$taxYear/jobseekers-allowance/section-completed"

  ".show" should {
    "render the page" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userPriorDataStub(aUser.nino, taxYearEOY, anAllStateBenefitsData)
        urlGet(url(taxYearEOY), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)))
      }

      result.status shouldBe OK
    }
  }

  ".submit" should {
    "redirect to JobSeekers summary page when answer is Yes" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userPriorDataStub(aUser.nino, taxYearEOY, anAllStateBenefitsData)
        val formData = Map(YesNoForm.yesNo -> "true")
        urlPost(url(taxYearEOY), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = formData)
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe SummaryController.show(taxYearEOY).url
    }

    "redirect to JobSeekers summary page when answer is No" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userPriorDataStub(aUser.nino, taxYearEOY, anAllStateBenefitsData)
        val formData = Map(YesNoForm.yesNo -> "false")
        urlPost(url(taxYearEOY), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = formData)
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe SummaryController.show(taxYearEOY).url
    }
  }
}
