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

import controllers.routes.ClaimsController
import models.BenefitType.JobSeekersAllowance
import play.api.http.HeaderNames
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import support.IntegrationTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import uk.gov.hmrc.http.HttpResponse

import java.util.UUID

class ReviewClaimControllerISpec extends IntegrationTest {

  private def url(taxYear: Int, sessionDataId: UUID): String =
    s"/update-and-submit-income-tax-return/state-benefits/$taxYear/jobseekers-allowance/$sessionDataId/review-claim"

  private def saveAndContinueUrl(taxYear: Int, sessionDataId: UUID): String = s"${url(taxYear, sessionDataId)}/save"

  private def restoreClaim(taxYear: Int, sessionDataId: UUID): String = s"${url(taxYear, sessionDataId)}/restore"

  private val sessionDataId = UUID.randomUUID()

  ".show" should {
    "render the ReviewJobSeekersAllowanceClaim page for in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        urlGet(url(taxYear, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the ReviewJobSeekersAllowanceClaim page for end of year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        urlGet(url(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)))
      }

      result.status shouldBe OK
    }
  }

  ".saveAndContinue" should {
    "redirect to Overview Page when in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        urlPost(saveAndContinueUrl(taxYear, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "persist amount and redirect to ReviewClaim" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        saveStateBenefitStub(aStateBenefitsUserData, HttpResponse(NO_CONTENT, ""))
        urlPost(saveAndContinueUrl(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = Map[String, String]())
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe ClaimsController.show(taxYearEOY, JobSeekersAllowance).url
    }
  }

  ".restoreClaim" should {
    "redirect to Overview Page when in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        urlPost(restoreClaim(taxYear, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "restore the given claim" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userSessionDataStub(aUser.nino, sessionDataId, HttpResponse(OK, Json.toJson(aStateBenefitsUserData).toString))
        restoreClaimStub(aUser.nino, sessionDataId, HttpResponse(NO_CONTENT, ""))
        urlPost(restoreClaim(taxYearEOY, sessionDataId), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)), body = Map[String, String]())
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe ClaimsController.show(taxYearEOY, JobSeekersAllowance).url
    }
  }
}
