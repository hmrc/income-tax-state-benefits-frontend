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
import config.AppConfig
import models.BenefitType
import models.pages.ClaimsPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.InYearUtil.inYear
import utils.SessionHelper
import views.html.pages.ClaimsPageView

import javax.inject.Inject

class ClaimsController @Inject()(actionsProvider: ActionsProvider,
                                 pageView: ClaimsPageView)
                                (implicit mcc: MessagesControllerComponents, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           benefitType: BenefitType): Action[AnyContent] = actionsProvider.priorDataWithViewStateBenefitsAudit(taxYear, benefitType) { implicit request =>
    Ok(pageView(ClaimsPage(taxYear, benefitType, inYear(taxYear), request.incomeTaxUserData)))
  }
}
