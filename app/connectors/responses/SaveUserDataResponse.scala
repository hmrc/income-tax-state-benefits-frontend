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

import connectors.Parser
import connectors.errors.ApiError
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

case class SaveUserDataResponse(httpResponse: HttpResponse, result: Either[ApiError, Unit])

object SaveUserDataResponse {

  implicit val saveUserDataResponseReads: HttpReads[SaveUserDataResponse] = new HttpReads[SaveUserDataResponse] with Parser {

    override protected[connectors] val parserName: String = this.getClass.getSimpleName

    override def read(method: String, url: String, response: HttpResponse): SaveUserDataResponse = response.status match {
      case NO_CONTENT => SaveUserDataResponse(response, Right(()))
      case NOT_FOUND | INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE | BAD_REQUEST | UNPROCESSABLE_ENTITY =>
        SaveUserDataResponse(response, handleError(response, response.status))
      case _ => SaveUserDataResponse(response, handleError(response, INTERNAL_SERVER_ERROR))
    }
  }
}
