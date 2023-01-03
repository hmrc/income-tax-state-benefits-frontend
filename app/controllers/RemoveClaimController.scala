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

package controllers

import actions.ActionsProvider
import config.{AppConfig, ErrorHandler}
import controllers.routes.ClaimsController
import models.BenefitType
import models.pages.RemoveClaimPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StateBenefitsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.RemoveClaimPageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RemoveClaimController @Inject()(actionsProvider: ActionsProvider,
                                      pageView: RemoveClaimPageView,
                                      stateBenefitsService: StateBenefitsService,
                                      errorHandler: ErrorHandler)
                                     (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           benefitType: BenefitType,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId) { implicit request =>
    Ok(pageView(RemoveClaimPage(taxYear, benefitType, request.stateBenefitsUserData)))
  }

  def submit(taxYear: Int,
             benefitType: BenefitType,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId).async { implicit request =>
    stateBenefitsService.removeClaim(request.user, sessionDataId).map {
      case Right(_) => Redirect(ClaimsController.show(taxYear, benefitType))
      case Left(_) => errorHandler.internalServerError()
    }
  }
}
