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

package actions

import config.{AppConfig, ErrorHandler}
import models.requests.{AuthorisationRequest, UserPriorDataRequest, UserSessionDataRequest}
import play.api.mvc.{ActionBuilder, AnyContent}
import services.StateBenefitsService

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ActionsProvider @Inject()(authAction: AuthorisedAction,
                                stateBenefitsService: StateBenefitsService,
                                errorHandler: ErrorHandler,
                                appConfig: AppConfig)
                               (implicit ec: ExecutionContext) {

  def priorDataFor(taxYear: Int): ActionBuilder[UserPriorDataRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(UserPriorDataRequestRefinerAction(taxYear, stateBenefitsService, errorHandler))

  def endOfYearSessionDataFor(taxYear: Int, sessionDataId: UUID): ActionBuilder[UserSessionDataRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(EndOfYearFilterAction(taxYear, appConfig))
      .andThen(UserSessionDataRequestRefinerAction(sessionDataId, stateBenefitsService, errorHandler))

  def sessionDataFor(taxYear: Int, sessionDataId: UUID): ActionBuilder[UserSessionDataRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(UserSessionDataRequestRefinerAction(sessionDataId, stateBenefitsService, errorHandler))

  def endOfYear(taxYear: Int): ActionBuilder[AuthorisationRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(EndOfYearFilterAction(taxYear, appConfig))
}
