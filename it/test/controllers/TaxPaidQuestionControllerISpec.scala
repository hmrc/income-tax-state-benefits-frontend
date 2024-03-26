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

package controllers

import controllers.routes.ReviewClaimController
import forms.YesNoForm
import models.BenefitType.JobSeekersAllowance
import play.api.http.HeaderNames
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import support.IntegrationTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import uk.gov.hmrc.http.HttpResponse

import java.util.UUID

class TaxPaidQuestionControllerISpec extends IntegrationTest {

  private def url(taxYear: Int, sessionDataId: UUID): String =
    s"/update-and-submit-income-tax-return/state-benefits/$taxYear/jobseekers-allowance/$sessionDataId/tax-taken-off"

  private val sessionDataId = aStateBenefitsUserData.sessionDataId.get

  ".show" should {
    "redirect to Overview Page when in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        urlGet(url(taxYear, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "render the TaxTakenOff page for end of year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        urlGet(url(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)))
      }

      result.status shouldBe OK
    }
  }

  ".submit" should {
    "redirect to income tax submission overview when in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        val formData = Map(YesNoForm.yesNo -> "true")
        urlPost(url(taxYear, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = formData)
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "redirect to ReviewClaim page when answer is Yes" in {
      val modelWithExpectedData = aClaimCYAModel.copy(endDateQuestion = Some(true))
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        updateSessionDataStub(aStateBenefitsUserData.copy(claim = Some(modelWithExpectedData)), HttpResponse(NO_CONTENT, ""))
        val formData = Map(YesNoForm.yesNo -> "true")
        urlPost(url(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = formData)
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe ReviewClaimController.show(taxYearEOY, JobSeekersAllowance, sessionDataId).url
    }

    "redirect to ReviewJobSeekersAllowanceClaim page when answer is No" in {
      val modelWithExpectedData = aClaimCYAModel.copy(taxPaidQuestion = Some(false), taxPaid = None)
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        updateSessionDataStub(aStateBenefitsUserData.copy(claim = Some(modelWithExpectedData)), HttpResponse(NO_CONTENT, ""))
        val formData = Map(YesNoForm.yesNo -> "false")
        urlPost(url(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = formData)
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe ReviewClaimController.show(taxYearEOY, JobSeekersAllowance, sessionDataId).url
    }
  }
}
