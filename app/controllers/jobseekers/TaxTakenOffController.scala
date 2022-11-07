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
import config.AppConfig
import controllers.jobseekers.routes.TaxTakenOffController
import forms.{FormsProvider, YesNoForm}
import play.api.i18n.I18nSupport
import play.api.data.Form
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.jobseekers.TaxTakenOffPageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.pages.jobseekers.TaxTakenOffPage
import play.api.Logging
import play.api.mvc.{Action, AnyContent}


class TaxTakenOffController @Inject()(actionsProvider: ActionsProvider,
                                      formsProvider: FormsProvider,
                                      pageView: TaxTakenOffPageView)(implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with SessionHelper with Logging {

  def show(taxYear: Int,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.userSessionDataFor(taxYear, sessionDataId) { implicit request =>
    Ok(pageView(TaxTakenOffPage(taxYear, request.stateBenefitsUserData, formsProvider.taxTakenOffForm(request.user.isAgent, taxYear))))
  }

  def submit(taxYear: Int,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.userSessionDataFor(taxYear, sessionDataId).async { implicit request =>
    formsProvider.taxTakenOffForm(request.user.isAgent, taxYear).bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(pageView(TaxTakenOffPage(taxYear, request.stateBenefitsUserData, formWithErrors)))),
      yesNoAnswer => if (yesNoAnswer) {
        Future(Redirect(TaxTakenOffController.show(taxYear, sessionDataId)))
      } else {
        Future(Redirect(TaxTakenOffController.show(taxYear, sessionDataId)))
      }
    )
  }
}