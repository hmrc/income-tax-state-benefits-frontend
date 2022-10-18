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

package connectors

import connectors.errors.{ApiError, SingleErrorBody}
import models.IncomeTaxUserData
import org.scalamock.scalatest.MockFactory
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import support.builders.models.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.models.UserBuilder.aUser
import support.mocks.MockPagerDutyLoggerService
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class StateBenefitsConnectorISpec extends ConnectorIntegrationTest
  with MockPagerDutyLoggerService
  with TaxYearProvider
  with MockFactory {

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
    .withExtraHeaders("mtditid" -> aUser.mtditid, "X-Session-ID" -> aUser.sessionId)

  private val underTest = new StateBenefitsConnector(httpClient, mockPagerDutyLoggerService, appConfigStub)

  "StateBenefitsConnector" should {
    "Return a success result" when {
      "BE returns a 404" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", NOT_FOUND, responseBody = "{}")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Right(IncomeTaxUserData())
      }

      "BE returns 200" in {
        val expectedResponse = Json.toJson(anIncomeTaxUserData.stateBenefits.get).toString()

        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", OK, expectedResponse,
          "X-Session-ID" -> aUser.sessionId, "mtditid" -> aUser.mtditid)

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Right(anIncomeTaxUserData)
      }
    }

    "Return an error result" when {
      "submission returns a 200 but invalid json" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", OK,
          Json.toJson("""{"invalid": true}""").toString())
        mockPagerDutyLog("GetIncomeTaxUserDataResponse")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      }

      "submission returns a 500" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", INTERNAL_SERVER_ERROR,
          """{"code": "FAILED", "reason": "failed"}""")
        mockPagerDutyLog("GetIncomeTaxUserDataResponse")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }

      "submission returns a 503" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", SERVICE_UNAVAILABLE,
          """{"code": "FAILED", "reason": "failed"}""")
        mockPagerDutyLog("GetIncomeTaxUserDataResponse")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("FAILED", "failed")))
      }

      "submission returns an unexpected result" in {
        stubGetWithHeadersCheck(s"/prior-data/nino/${aUser.nino}/tax-year/$taxYear", REQUEST_URI_TOO_LONG,
          """{"code": "FAILED", "reason": "failed"}""")
        mockPagerDutyLog("GetIncomeTaxUserDataResponse")

        await(underTest.getIncomeTaxUserData(aUser, taxYear)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }
    }
  }
}
