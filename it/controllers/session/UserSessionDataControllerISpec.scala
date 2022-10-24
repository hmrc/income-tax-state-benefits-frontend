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

package controllers.session

import controllers.jobseekers.routes.StartDateController
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import support.IntegrationTest

import java.util.UUID

class UserSessionDataControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String =
    s"/update-and-submit-income-tax-return/state-benefits/$taxYear/session-data"

  ".create" should {
    "redirect to income tax submission overview when in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "create user session data and redirect to Start Date page" in {
      val sessionDataId = UUID.randomUUID()
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        stubPost(url = "/income-tax-state-benefits/session-data", status = OK, responseBody = Json.toJson(sessionDataId).toString())
        urlPost(url(taxYearEOY), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = Map[String, String]())
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe StartDateController.show(taxYearEOY, sessionDataId).url
    }
  }
}
