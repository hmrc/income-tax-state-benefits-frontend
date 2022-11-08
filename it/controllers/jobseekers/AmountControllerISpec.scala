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

import controllers.jobseekers.routes.AmountController
import forms.AmountForm.amount
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser

import java.util.UUID

class AmountControllerISpec extends IntegrationTest {

  private def url(taxYear: Int, sessionDataId: UUID): String =
    s"/update-and-submit-income-tax-return/state-benefits/$taxYear/jobseekers-allowance/$sessionDataId/amount"

  private val sessionDataId = UUID.randomUUID()

  ".show" should {
    "redirect to Overview Page when in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, aStateBenefitsUserData)
        urlGet(url(taxYear, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "render the Amount page for end of year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, aStateBenefitsUserData)
        urlGet(url(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)))
      }

      result.status shouldBe OK
    }
  }

  ".submit" should {
    "redirect to income tax submission overview when in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, aStateBenefitsUserData)
        val formData = Map(s"$amount" -> "100")
        urlPost(url(taxYear, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = formData)
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "persist amount and redirect to next page" in {
      val modelWithNewAmount = aClaimCYAModel.copy(amount = Some(100))

      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, aStateBenefitsUserData)
        createOrUpdateUserDataStub(aStateBenefitsUserData.copy(claim = Some(modelWithNewAmount)), sessionDataId)
        val formData = Map(s"$amount" -> "100")
        urlPost(url(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = formData)
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe AmountController.show(taxYearEOY, sessionDataId).url
    }
  }
}
