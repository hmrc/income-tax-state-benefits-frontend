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

import config.AppConfig
import connectors.errors.ApiError
import connectors.responses._
import models.{IncomeTaxUserData, StateBenefitsUserData, User}
import services.PagerDutyLoggerService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StateBenefitsConnector @Inject()(httpClient: HttpClient,
                                       pagerDutyLoggerService: PagerDutyLoggerService,
                                       appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def getIncomeTaxUserData(user: User, taxYear: Int)
                          (implicit hc: HeaderCarrier): Future[Either[ApiError, IncomeTaxUserData]] = {
    val response = getIncomeTaxUserData(taxYear, user.nino)(hc.withExtraHeaders(headers = "mtditid" -> user.mtditid))

    response.map { response: GetIncomeTaxUserDataResponse =>
      if (response.result.isLeft) pagerDutyLoggerService.pagerDutyLog(response.httpResponse, response.getClass.getSimpleName)
      response.result
    }
  }

  def getUserSessionData(user: User, sessionDataId: UUID)
                        (implicit hc: HeaderCarrier): Future[Either[ApiError, StateBenefitsUserData]] = {
    val response = getUserSessionData(user.nino, sessionDataId)(hc.withExtraHeaders(headers = "mtditid" -> user.mtditid))

    response.map { response: GetUserSessionDataResponse =>
      if (response.result.isLeft) pagerDutyLoggerService.pagerDutyLog(response.httpResponse, response.getClass.getSimpleName)
      response.result
    }
  }

  def createOrUpdate(stateBenefitsUserData: StateBenefitsUserData)
                    (implicit hc: HeaderCarrier): Future[Either[ApiError, UUID]] = {
    val response = createOrUpdateData(stateBenefitsUserData)(hc.withExtraHeaders(headers = "mtditid" -> stateBenefitsUserData.mtdItId))

    response.map { response: CreateOrUpdateUserDataResponse =>
      if (response.result.isLeft) pagerDutyLoggerService.pagerDutyLog(response.httpResponse, response.getClass.getSimpleName)
      response.result
    }
  }

  def saveStateBenefit(stateBenefitsUserData: StateBenefitsUserData)
                      (implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    val response = saveUserData(stateBenefitsUserData)(hc.withExtraHeaders(headers = "mtditid" -> stateBenefitsUserData.mtdItId))

    response.map { response: SaveUserDataResponse =>
      if (response.result.isLeft) pagerDutyLoggerService.pagerDutyLog(response.httpResponse, response.getClass.getSimpleName)
      response.result
    }
  }

  def removeClaim(user: User, sessionDataId: UUID)(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    val response = removeClaimData(user.nino, sessionDataId)(hc.withExtraHeaders(headers = "mtditid" -> user.mtditid))

    response.map { response: RemoveClaimResponse =>
      if (response.result.isLeft) pagerDutyLoggerService.pagerDutyLog(response.httpResponse, response.getClass.getSimpleName)
      response.result
    }
  }

  def restoreClaim(user: User, sessionDataId: UUID)(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] = {
    val response = restoreClaimData(user.nino, sessionDataId)(hc.withExtraHeaders(headers = "mtditid" -> user.mtditid))

    response.map { response: RestoreClaimResponse =>
      if (response.result.isLeft) pagerDutyLoggerService.pagerDutyLog(response.httpResponse, response.getClass.getSimpleName)
      response.result
    }
  }

  private def getIncomeTaxUserData(taxYear: Int, nino: String)
                                  (implicit hc: HeaderCarrier): Future[GetIncomeTaxUserDataResponse] = {
    val stateBenefitsBEUrl = appConfig.stateBenefitsServiceBaseUrl + s"/prior-data/nino/$nino/tax-year/$taxYear"
    httpClient.GET[GetIncomeTaxUserDataResponse](stateBenefitsBEUrl)
  }

  private def saveUserData(stateBenefitsUserData: StateBenefitsUserData)
                          (implicit hc: HeaderCarrier): Future[SaveUserDataResponse] = {
    val stateBenefitsBEUrl = appConfig.stateBenefitsServiceBaseUrl + "/income-tax"
    httpClient.PUT[StateBenefitsUserData, SaveUserDataResponse](stateBenefitsBEUrl, stateBenefitsUserData)
  }

  private def getUserSessionData(nino: String, sessionDataId: UUID)
                                (implicit hc: HeaderCarrier): Future[GetUserSessionDataResponse] = {
    val stateBenefitsBEUrl = appConfig.stateBenefitsServiceBaseUrl + s"/session-data/nino/$nino/session/$sessionDataId"
    httpClient.GET[GetUserSessionDataResponse](stateBenefitsBEUrl)
  }

  private def createOrUpdateData(stateBenefitsUserData: StateBenefitsUserData)
                                (implicit hc: HeaderCarrier): Future[CreateOrUpdateUserDataResponse] = {
    val stateBenefitsBEUrl = appConfig.stateBenefitsServiceBaseUrl + s"/session-data"
    httpClient.POST[StateBenefitsUserData, CreateOrUpdateUserDataResponse](stateBenefitsBEUrl, stateBenefitsUserData)
  }

  private def removeClaimData(nino: String, sessionDataId: UUID)
                             (implicit hc: HeaderCarrier): Future[RemoveClaimResponse] = {
    val stateBenefitsBEUrl = appConfig.stateBenefitsServiceBaseUrl + s"/session-data/nino/$nino/session/$sessionDataId"
    httpClient.DELETE[RemoveClaimResponse](stateBenefitsBEUrl)
  }

  private def restoreClaimData(nino: String, sessionDataId: UUID)
                              (implicit hc: HeaderCarrier): Future[RestoreClaimResponse] = {
    val stateBenefitsBEUrl = appConfig.stateBenefitsServiceBaseUrl + s"/session-data/nino/$nino/session/$sessionDataId/ignore"
    httpClient.DELETE[RestoreClaimResponse](stateBenefitsBEUrl)
  }
}
