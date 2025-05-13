/*
 * Copyright 2025 HM Revenue & Customs
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
import models.session.UserSessionData
import play.api.http.Status._
import play.api.libs.json.JsSuccess
import services.PagerDutyLoggerService
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object SessionDataHttpResponse extends PagerDutyLoggerService with Parser {
  type SessionDataResponse = Either[ApiError, Option[UserSessionData]]

  override val parserName: String = "SessionDataHttpParser"

  implicit object SessionDataResponseReads extends HttpReads[SessionDataResponse] {
    override def read(method: String, url: String, response: HttpResponse): SessionDataResponse = {
      response.status match {
        case OK =>
          response.json.validate[UserSessionData] match {
            case JsSuccess(parsedModel, _) => Right(Some(parsedModel))
            case _ => badSuccessJsonResponse
          }
        case NOT_FOUND | NO_CONTENT =>
          Right(None)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(response, parserName)
          handleError(response, response.status)
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(response, parserName)
          handleError(response, response.status)
        case _ =>
          pagerDutyLog(response, parserName)
          handleError(response, INTERNAL_SERVER_ERROR)
      }
    }
  }
}
