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

case class RestoreClaimResponse(httpResponse: HttpResponse, result: Either[ApiError, Unit])

object RestoreClaimResponse {

  implicit val restoreClaimResponseReads: HttpReads[RestoreClaimResponse] = new HttpReads[RestoreClaimResponse] with Parser {

    override protected[connectors] val parserName: String = this.getClass.getSimpleName

    override def read(method: String, url: String, response: HttpResponse): RestoreClaimResponse = response.status match {
      case NO_CONTENT => RestoreClaimResponse(response, Right(()))
      case INTERNAL_SERVER_ERROR => RestoreClaimResponse(response, handleError(response, response.status))
      case _ => RestoreClaimResponse(response, handleError(response, INTERNAL_SERVER_ERROR))
    }
  }
}
