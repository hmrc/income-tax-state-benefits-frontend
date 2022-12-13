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

package controllers.jobseekers

import actions.ActionsProvider
import config.{AppConfig, ErrorHandler}
import controllers.employmentsupport.routes.EmploymentSupportAllowanceController
import controllers.jobseekers.routes.JobSeekersAllowanceController
import models.BenefitType
import models.pages.jobseekers.RemoveClaimPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StateBenefitsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.jobseekers.RemoveClaimPageView

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
           sessionDataId: UUID,
           benefitTypeUrl: String): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, sessionDataId) { implicit request =>
    Ok(pageView(RemoveClaimPage(taxYear, request.stateBenefitsUserData, BenefitType.mapFrom(benefitTypeUrl))))
  }

  def submit(taxYear: Int,
             sessionDataId: UUID,
             benefitTypeUrl: String): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, sessionDataId).async { implicit request =>
    stateBenefitsService.removeClaim(request.user, sessionDataId).map {
      case Right(_) if benefitTypeUrl == "jobseekers-allowance" => Redirect(JobSeekersAllowanceController.show(taxYear))
      case Right(_) if benefitTypeUrl == "employment-support-allowance" => Redirect(EmploymentSupportAllowanceController.show(taxYear))
      case Left(_) => errorHandler.internalServerError()
    }
  }
}
