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
import controllers.jobseekers.routes.EndDateController
import forms.DateForm
import forms.DateForm.dateForm
import models.pages.jobseekers.EndDatePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.jobseekers.EndDatePageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EndDateController @Inject()(actionsProvider: ActionsProvider,
                                  pageView: EndDatePageView,
                                  claimService: ClaimService,
                                  errorHandler: ErrorHandler)
                                 (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.userSessionDataFor(taxYear, sessionDataId) { implicit request =>
    Ok(pageView(EndDatePage(taxYear, request.stateBenefitsUserData, dateForm())))
  }

  def submit(taxYear: Int,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.userSessionDataFor(taxYear, sessionDataId).async { implicit request =>
    val newForm = dateForm().bindFromRequest()
    val sessionData = request.stateBenefitsUserData
    newForm.copy(errors = DateForm.validateEndDate(newForm.get, taxYear, request.user.isAgent, sessionData.claim.get.startDate)).fold(
      formWithErrors => Future.successful(BadRequest(pageView(EndDatePage(taxYear, sessionData, formWithErrors)))),
      formData => claimService.updateEndDate(sessionData, formData.toLocalDate.get).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(uuid) => Redirect(EndDateController.show(taxYear, uuid))
      }
    )
  }
}
