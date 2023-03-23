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

package actions

import config.{AppConfig, ErrorHandler}
import models.BenefitType
import models.requests.{AuthorisationRequest, UserPriorDataRequest, UserSessionDataRequest}
import play.api.mvc.{ActionBuilder, AnyContent}
import services.{AuditService, StateBenefitsService}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ActionsProvider @Inject()(authAction: AuthorisedAction,
                                stateBenefitsService: StateBenefitsService,
                                auditService: AuditService,
                                errorHandler: ErrorHandler,
                                appConfig: AppConfig)
                               (implicit ec: ExecutionContext) {

  // TODO: Make this private after Claims and Summary pages auditing is complete.
  def priorDataFor(taxYear: Int): ActionBuilder[UserPriorDataRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(UserPriorDataRequestRefinerAction(taxYear, stateBenefitsService, errorHandler))

  def priorDataWithViewStateBenefitsAudit(taxYear: Int, benefitType: BenefitType): ActionBuilder[UserPriorDataRequest, AnyContent] =
    priorDataFor(taxYear)
      .andThen(ViewStateBenefitsAuditAction(taxYear, benefitType, auditService))

  def endOfYearSessionDataFor(taxYear: Int, benefitType: BenefitType, sessionDataId: UUID): ActionBuilder[UserSessionDataRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(EndOfYearFilterAction(taxYear, appConfig))
      .andThen(UserSessionDataRequestRefinerAction(taxYear, benefitType, sessionDataId, stateBenefitsService, errorHandler))

  def reviewClaimSessionDataFor(taxYear: Int, benefitType: BenefitType, sessionDataId: UUID): ActionBuilder[UserSessionDataRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(UserSessionDataRequestRefinerAction(taxYear, benefitType, sessionDataId, stateBenefitsService, errorHandler))
      .andThen(ReviewClaimFilterAction(taxYear, benefitType))

  def reviewClaimSaveAndContinue(taxYear: Int, benefitType: BenefitType, sessionDataId: UUID): ActionBuilder[UserSessionDataRequest, AnyContent] =
    endOfYearSessionDataFor(taxYear, benefitType, sessionDataId)
      .andThen(ReviewClaimFilterAction(taxYear, benefitType))
      .andThen(SaveAndContinueFilterAction(taxYear, benefitType, stateBenefitsService, errorHandler))

  def endOfYear(taxYear: Int): ActionBuilder[AuthorisationRequest, AnyContent] =
    authAction
      .andThen(TaxYearAction(taxYear, appConfig, ec))
      .andThen(EndOfYearFilterAction(taxYear, appConfig))
}
