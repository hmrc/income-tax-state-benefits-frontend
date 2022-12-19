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
import controllers.routes.SummaryController
import forms.jobseekers.FormsProvider
import models.pages.jobseekers.SectionCompletedQuestionPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.jobseekers.SectionCompletedQuestionPageView

import javax.inject.Inject

class SectionCompletedQuestionController @Inject()(actionsProvider: ActionsProvider,
                                                   formsProvider: FormsProvider,
                                                   pageView: SectionCompletedQuestionPageView,
                                                   errorHandler: ErrorHandler)
                                                  (implicit mcc: MessagesControllerComponents, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int): Action[AnyContent] = actionsProvider.priorDataFor(taxYear) { implicit request =>
    Ok(pageView(SectionCompletedQuestionPage(taxYear, formsProvider.sectionCompletedYesNoForm())))
  }

  def submit(taxYear: Int): Action[AnyContent] = actionsProvider.priorDataFor(taxYear) { implicit request =>
    formsProvider.sectionCompletedYesNoForm().bindFromRequest().fold(
      formWithErrors => BadRequest(pageView(SectionCompletedQuestionPage(taxYear, formWithErrors))),
      _ => Redirect(SummaryController.show(taxYear))
    )
  }
}
