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

package connectors.responses

import connectors.Parser
import connectors.errors.ApiError
import models.{AllStateBenefitsData, IncomeTaxUserData}
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

case class GetIncomeTaxUserDataResponse(httpResponse: HttpResponse, result: Either[ApiError, IncomeTaxUserData])

object GetIncomeTaxUserDataResponse {

  implicit val getIncomeTaxUserDataResponseReads: HttpReads[GetIncomeTaxUserDataResponse] = new HttpReads[GetIncomeTaxUserDataResponse] with Parser {

    override protected[connectors] val parserName: String = this.getClass.getSimpleName

    override def read(method: String, url: String, response: HttpResponse): GetIncomeTaxUserDataResponse = response.status match {
      case OK => GetIncomeTaxUserDataResponse(response, extractResult(response))
      case NOT_FOUND => GetIncomeTaxUserDataResponse(response, Right(IncomeTaxUserData(None)))
      case INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE | BAD_REQUEST | UNPROCESSABLE_ENTITY =>
        GetIncomeTaxUserDataResponse(response, handleError(response, response.status))
      case _ => GetIncomeTaxUserDataResponse(response, handleError(response, INTERNAL_SERVER_ERROR))
    }

    private def extractResult(response: HttpResponse): Either[ApiError, IncomeTaxUserData] = {
      response.json.validate[AllStateBenefitsData]
        .fold[Either[ApiError, IncomeTaxUserData]](_ => badSuccessJsonResponse, parsedModel => Right(IncomeTaxUserData(Some(parsedModel))))
    }
  }
}
