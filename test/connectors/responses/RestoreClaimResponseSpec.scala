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
import connectors.responses.RestoreClaimResponse.restoreClaimResponseReads
import play.api.http.Status.{FAILED_DEPENDENCY, INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.libs.json.{JsValue, Json}
import support.UnitTest
import uk.gov.hmrc.http.HttpResponse

class RestoreClaimResponseSpec extends UnitTest {

  private val anyHeaders: Map[String, Seq[String]] = Map.empty
  private val anyMethod: String = "DELETE"
  private val anyUrl = "/any-url"

  private val underTest = restoreClaimResponseReads

  "restoreClaimResponseReads" should {
    "convert JsValue to RestoreClaimResponse" when {
      "status is NO_CONTENT and return unit" in {
        val httpResponse: HttpResponse = HttpResponse.apply(NO_CONTENT, Json.toJson("{}"), anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe RestoreClaimResponse(httpResponse, Right(()))
      }

      "status is INTERNAL_SERVER_ERROR and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(INTERNAL_SERVER_ERROR, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe RestoreClaimResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }

      "status is any other and jsValue for error" in {
        val jsValue: JsValue = Json.toJson(SingleErrorBody("some-code", "some-reason"))
        val httpResponse: HttpResponse = HttpResponse.apply(FAILED_DEPENDENCY, jsValue, anyHeaders)

        underTest.read(anyMethod, anyUrl, httpResponse) shouldBe RestoreClaimResponse(
          httpResponse,
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
        )
      }
    }
  }
}
