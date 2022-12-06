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

package connectors.responses

import connectors.errors.{ApiError, SingleErrorBody}
import connectors.responses.SaveUserDataResponse.saveUserDataResponseReads
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import support.UnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import uk.gov.hmrc.http.HttpResponse

class SaveUserDataResponseSpec extends UnitTest {

  private val anyHeaders: Map[String, Seq[String]] = Map.empty
  private val anyMethod: String = "DELETE"
  private val anyUrl = "/any-url"

  private val underTest = saveUserDataResponseReads

  "saveUserDataResponseReads" should {
    "convert JsValue to SaveUserDataResponse" when {
      "status is NO_CONTENT and return unit" in {
        val jsValue: JsValue = Json.toJson(aStateBenefitsUserData)
        val httpResponse: HttpResponse = HttpResponse.apply(NO_CONTENT, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe SaveUserDataResponse(
          httpResponse,
          Right(Unit)
        )
      }

      "status is NOT_FOUND and any jsValue" in {
        val jsValue: JsValue = Json.parse(
          """
            |{}
            |""".stripMargin)
        val httpResponse: HttpResponse = HttpResponse.apply(NOT_FOUND, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe SaveUserDataResponse(
          httpResponse,
          Left(ApiError(NOT_FOUND, SingleErrorBody.parsingError))
        )
      }

      "status is INTERNAL_SERVER_ERROR and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe SaveUserDataResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is SERVICE_UNAVAILABLE and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(SERVICE_UNAVAILABLE, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe SaveUserDataResponse(
          httpResponse,
          Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is BAD_REQUEST and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(BAD_REQUEST, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe SaveUserDataResponse(
          httpResponse,
          Left(ApiError(BAD_REQUEST, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is UNPROCESSABLE_ENTITY and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(UNPROCESSABLE_ENTITY, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe SaveUserDataResponse(
          httpResponse,
          Left(ApiError(UNPROCESSABLE_ENTITY, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is OTHER and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(FAILED_DEPENDENCY, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe SaveUserDataResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }
    }
  }
}
