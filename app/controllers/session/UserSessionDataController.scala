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
import controllers.jobseekers.routes.{JobSeekersAllowanceController, ReviewClaimController, StartDateController}
import models.StateBenefitsUserData
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

  def create(taxYear: Int): Action[AnyContent] = actionsProvider.endOfYear(taxYear).async { implicit request =>
    stateBenefitsService.createOrUpdate(StateBenefitsUserData(taxYear, request.user)).map {
      case Left(_) => errorHandler.internalServerError()
      case Right(uuid) => Redirect(StartDateController.show(taxYear, uuid))
    }
  }

  def loadToSession(taxYear: Int, benefitId: UUID): Action[AnyContent] = actionsProvider.userPriorDataFor(taxYear).async { implicit request =>
    StateBenefitsUserData(taxYear, request.user, benefitId, request.incomeTaxUserData) match {
      case None => Future.successful(Redirect(JobSeekersAllowanceController.show(taxYear)))
      case Some(stateBenefitsUserData) => stateBenefitsService.createOrUpdate(stateBenefitsUserData).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(uuid) => Redirect(ReviewClaimController.show(taxYear, uuid))
      }
    }
  }
}
