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
import controllers.jobseekers.routes.ReviewClaimController
import forms.jobseekers.FormsProvider
import models.pages.jobseekers.TaxPaidPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.jobseekers.TaxPaidPageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxPaidController @Inject()(actionsProvider: ActionsProvider,
                                  formsProvider: FormsProvider,
                                  pageView: TaxPaidPageView,
                                  claimService: ClaimService,
                                  errorHandler: ErrorHandler)
                                 (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int, sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, sessionDataId) { implicit request =>
    Ok(pageView(TaxPaidPage(taxYear, request.stateBenefitsUserData, formsProvider.taxPaidAmountForm())))
  }

  def submit(taxYear: Int,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, sessionDataId).async { implicit request =>
    formsProvider.taxPaidAmountForm().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(pageView(TaxPaidPage(taxYear, request.stateBenefitsUserData, formWithErrors)))),
      amount => claimService.updateTaxPaidAmount(request.stateBenefitsUserData, amount).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(userData) => Redirect(ReviewClaimController.show(taxYear, userData.sessionDataId.get))
      }
    )
  }
}

