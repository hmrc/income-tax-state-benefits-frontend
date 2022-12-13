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

package controllers.session

import actions.ActionsProvider
import config.ErrorHandler
import controllers.employmentsupport.routes.EmploymentSupportAllowanceController
import controllers.jobseekers.routes.{JobSeekersAllowanceController, ReviewClaimController, StartDateController}
import models.BenefitType.{EmploymentSupportAllowance, JobSeekersAllowance}
import models.{BenefitType, StateBenefitsUserData}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StateBenefitsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserSessionDataController @Inject()(actionsProvider: ActionsProvider,
                                          stateBenefitsService: StateBenefitsService,
                                          errorHandler: ErrorHandler)
                                         (implicit ec: ExecutionContext, mcc: MessagesControllerComponents)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def create(taxYear: Int,
             benefitType: String): Action[AnyContent] = actionsProvider.endOfYear(taxYear).async { implicit request =>
    val benefitTypeUrl = benefitType match {
      case "employmentSupportAllowance" => "employment-support-allowance"
      case "jobSeekersAllowance" => "jobseekers-allowance"
    }
    stateBenefitsService.createOrUpdate(StateBenefitsUserData(taxYear, request.user, benefitType)).map {
      case Left(_) => errorHandler.internalServerError()
      case Right(uuid) => Redirect(StartDateController.show(taxYear, uuid, benefitTypeUrl))
    }
  }

  def loadToSession(taxYear: Int,
                    benefitId: UUID,
                    benefitType: String,
                    benefitTypeUrl: String): Action[AnyContent] = actionsProvider.userPriorDataFor(taxYear).async { implicit request =>
    StateBenefitsUserData(taxYear, request.user, benefitId, request.incomeTaxUserData, benefitType) match {
      case None if BenefitType(benefitType) == JobSeekersAllowance => Future.successful(Redirect(JobSeekersAllowanceController.show(taxYear)))
      case None if BenefitType(benefitType) == EmploymentSupportAllowance => Future.successful(Redirect(EmploymentSupportAllowanceController.show(taxYear)))
      case Some(stateBenefitsUserData) => stateBenefitsService.createOrUpdate(stateBenefitsUserData).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(uuid) => Redirect(ReviewClaimController.show(taxYear, uuid, benefitTypeUrl))
      }
    }
  }
}
