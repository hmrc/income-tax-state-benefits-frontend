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

package connectors

import connectors.errors.{ApiError, SingleErrorBody}
import models.IncomeTaxUserData
import org.scalamock.scalatest.MockFactory
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.mocks.MockPagerDutyLoggerService
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class StateBenefitsConnectorISpec extends ConnectorIntegrationTest
  with MockPagerDutyLoggerService
  with TaxYearProvider
  with MockFactory {

  private val sessionDataId = UUID.randomUUID()

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
    .withExtraHeaders("mtditid" -> aUser.mtditid, "X-Session-ID" -> aUser.sessionId)

  private val underTest = new StateBenefitsConnector(httpClient, mockPagerDutyLoggerService, appConfigStub)

  ".getIncomeTaxUserData(...)" should {
    "Return a success result" when {
      "BE returns a 404" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", HttpResponse(NOT_FOUND, "{}"))

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Right(IncomeTaxUserData())
      }

      "BE returns 200" in {
        val expectedResponse = Json.toJson(anIncomeTaxUserData.stateBenefits.get).toString

        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", HttpResponse(OK, expectedResponse))

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Right(anIncomeTaxUserData)
      }
    }

    "Return an error result" when {
      "submission returns a 500" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear",
          HttpResponse(INTERNAL_SERVER_ERROR, """{"code": "FAILED", "reason": "failed"}"""))
        mockPagerDutyLog("GetIncomeTaxUserDataResponse")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }

      "submission returns a 503" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear",
          HttpResponse(SERVICE_UNAVAILABLE, """{"code": "FAILED", "reason": "failed"}"""))
        mockPagerDutyLog("GetIncomeTaxUserDataResponse")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("FAILED", "failed")))
      }

      "submission returns an unexpected result" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear",
          HttpResponse(REQUEST_URI_TOO_LONG, """{"code": "FAILED", "reason": "failed"}"""))
        mockPagerDutyLog("GetIncomeTaxUserDataResponse")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }
    }
  }

  ".getUserSessionData(...)" should {
    "Return a success result" when {
      "BE returns 200" in {
        val expectedResponse = Json.toJson(aStateBenefitsUserData).toString()

        stubGetWithHeadersCheck(s"/session-data/nino/${aUser.nino}/session/$sessionDataId", HttpResponse(OK, expectedResponse))

        await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Right(aStateBenefitsUserData)
      }
    }

    "Return an error result" when {
      "submission returns a 200 but invalid json" in {
        stubGetWithHeadersCheck(s"/session-data/nino/${aUser.nino}/session/$sessionDataId",
          HttpResponse(OK, Json.toJson("""{"invalid": true}""").toString()))
        mockPagerDutyLog("GetUserSessionDataResponse")

        await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      }

      "BE returns a 404" in {
        stubGetWithHeadersCheck(s"/session-data/nino/${aUser.nino}/session/$sessionDataId", HttpResponse(NOT_FOUND, "{}"))
        mockPagerDutyLog("GetUserSessionDataResponse")

        await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Left(ApiError(404, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
      }

      "submission returns a 500" in {
        stubGetWithHeadersCheck(s"/session-data/nino/${aUser.nino}/session/$sessionDataId",
          HttpResponse(INTERNAL_SERVER_ERROR, """{"code": "FAILED", "reason": "failed"}"""))
        mockPagerDutyLog("GetUserSessionDataResponse")

        await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }

      "submission returns a 503" in {
        stubGetWithHeadersCheck(s"/session-data/nino/${aUser.nino}/session/$sessionDataId",
          HttpResponse(SERVICE_UNAVAILABLE, """{"code": "FAILED", "reason": "failed"}"""))
        mockPagerDutyLog("GetUserSessionDataResponse")

        await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("FAILED", "failed")))
      }

      "submission returns an unexpected result" in {
        stubGetWithHeadersCheck(s"/session-data/nino/${aUser.nino}/session/$sessionDataId",
          HttpResponse(REQUEST_URI_TOO_LONG, """{"code": "FAILED", "reason": "failed"}"""))
        mockPagerDutyLog("GetUserSessionDataResponse")

        await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }
    }
  }

  ".createSessionData(...)" should {
    "Return a success result when BE returns 201" in {
      createSessionDataStub("/session-data", aStateBenefitsUserData, HttpResponse(CREATED, Json.toJson(sessionDataId).toString))

      await(underTest.createSessionData(aStateBenefitsUserData)) shouldBe Right(sessionDataId)
    }

    "Return a Left/Failure when BE returns code different than 201" in {
      createSessionDataStub("/session-data", aStateBenefitsUserData, HttpResponse(NOT_FOUND, "{}"))
      mockPagerDutyLog("CreateSessionDataResponse")

      await(underTest.createSessionData(aStateBenefitsUserData)) shouldBe Left(ApiError(NOT_FOUND, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }
  }

  ".updateSessionData(...)" should {
    val nino = aStateBenefitsUserData.nino
    val sessionDataId = aStateBenefitsUserData.sessionDataId.get
    "Return a success result when BE returns NO_CONTENT" in {
      updateSessionDataStub(s"/session-data/nino/$nino/session/$sessionDataId", aStateBenefitsUserData, HttpResponse(NO_CONTENT, ""))

      await(underTest.updateSessionData(aStateBenefitsUserData)) shouldBe Right(())
    }

    "Return a Left/Failure when BE returns code different than NO_CONTENT" in {
      updateSessionDataStub(s"/session-data/nino/$nino/session/$sessionDataId", aStateBenefitsUserData, HttpResponse(NOT_FOUND, ""))
      mockPagerDutyLog("UpdateSessionDataResponse")

      await(underTest.updateSessionData(aStateBenefitsUserData)) shouldBe Left(ApiError(NOT_FOUND, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }
  }

  ".saveStateBenefit(...)" should {
    val url = "/income-tax"
    "Return a success result when BE returns 204" in {
      saveStateBenefitStub(url, HttpResponse(NO_CONTENT, ""))

      await(underTest.saveStateBenefit(aStateBenefitsUserData)) shouldBe Right(())
    }

    "Return a Left/Failure when BE returns code different than NO_CONTENT" in {
      saveStateBenefitStub(url, HttpResponse(NOT_FOUND, "{}"))
      mockPagerDutyLog("SaveUserDataResponse")

      await(underTest.saveStateBenefit(aStateBenefitsUserData)) shouldBe Left(ApiError(NOT_FOUND, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }
  }

  ".removeClaim(...)" should {
    val removeClaimUrl = s"/session-data/nino/${aUser.nino}/session/$sessionDataId"
    "Return an empty response in the success case when BE returns 204" in {
      removeClaimStub(removeClaimUrl, HttpResponse(NO_CONTENT, ""))

      await(underTest.removeClaim(aUser, sessionDataId)) shouldBe Right(())
    }

    "Return a Left/Failure when BE returns code different than 204" in {
      removeClaimStub(removeClaimUrl, HttpResponse(NOT_FOUND, ""))
      mockPagerDutyLog("RemoveClaimResponse")

      await(underTest.removeClaim(aUser, sessionDataId)) shouldBe Left(ApiError(NOT_FOUND, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }
  }

  ".restoreClaim(...)" should {
    val restoreUrl = s"/session-data/nino/${aUser.nino}/session/$sessionDataId/ignore"
    "Return an empty response in the success case when BE returns 204" in {
      restoreClaimStub(restoreUrl, HttpResponse(NO_CONTENT, ""))

      await(underTest.restoreClaim(aUser, sessionDataId)) shouldBe Right(())
    }

    "Return a Left/Failure when BE returns code different than 204" in {
      restoreClaimStub(restoreUrl, HttpResponse(NOT_FOUND, ""))
      mockPagerDutyLog("RestoreClaimResponse")

      await(underTest.restoreClaim(aUser, sessionDataId)) shouldBe
        Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
    }
  }
}
