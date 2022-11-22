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
import models.pages.jobseekers.ReviewClaimPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{InYearUtil, SessionHelper}
import views.html.pages.jobseekers.ReviewClaimPageView

import java.util.UUID
import javax.inject.Inject

class ReviewClaimController @Inject()(actionsProvider: ActionsProvider,
                                      pageView: ReviewClaimPageView)
                                     (implicit mcc: MessagesControllerComponents, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.userSessionDataFor(taxYear, sessionDataId) { implicit request =>
    Ok(pageView(ReviewClaimPage(taxYear, InYearUtil.inYear(taxYear), request.stateBenefitsUserData)))
  }
}


