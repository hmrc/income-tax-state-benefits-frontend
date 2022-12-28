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

import controllers.routes.{ReviewClaimController, StartDateController}
import models.BenefitType.JobSeekersAllowance
import models.StateBenefitsUserData
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import support.IntegrationTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.UserBuilder.aUser

import java.util.UUID

class UserSessionDataControllerISpec extends IntegrationTest {

  private def url(taxYear: Int,
                  benefitId: Option[UUID] = None): String = {
    s"/update-and-submit-income-tax-return/state-benefits/$taxYear/session-data?benefitType=jobSeekersAllowance${benefitId.fold("")(id => s"&benefitId=$id")}"
  }

  private val sessionDataId = UUID.randomUUID()

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
      result.headers("Location").head shouldBe StartDateController.show(taxYearEOY, JobSeekersAllowance, sessionDataId).url
    }
  }

  ".loadToSession" should {
    "redirect to ReviewClaim when benefitId found and in year" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userPriorDataStub(aUser.nino, taxYear, anAllStateBenefitsData)
        createOrUpdateUserDataStub(StateBenefitsUserData(taxYear, JobSeekersAllowance, aUser, aStateBenefit.benefitId, anIncomeTaxUserData).get, sessionDataId)
        urlGet(url(taxYear, Some(aStateBenefit.benefitId)), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe ReviewClaimController.show(taxYear, JobSeekersAllowance, sessionDataId).url
    }

    "redirect to ReviewClaim when benefitId found" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userPriorDataStub(aUser.nino, taxYearEOY, anAllStateBenefitsData)
        createOrUpdateUserDataStub(StateBenefitsUserData(taxYearEOY, JobSeekersAllowance, aUser, aStateBenefit.benefitId, anIncomeTaxUserData).get, sessionDataId)
        urlGet(url(taxYearEOY, Some(aStateBenefit.benefitId)), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYearEOY)))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe ReviewClaimController.show(taxYearEOY, JobSeekersAllowance, sessionDataId).url
    }
  }
}
