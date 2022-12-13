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
import controllers.jobseekers.routes.{EndDateQuestionController, ReviewClaimController}
import forms.jobseekers.FormsProvider
import models.BenefitType.mapFrom
import models.StateBenefitsUserData
import models.pages.jobseekers.StartDatePage
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.jobseekers.StartDatePageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartDateController @Inject()(actionsProvider: ActionsProvider,
                                    formsProvider: FormsProvider,
                                    pageView: StartDatePageView,
                                    claimService: ClaimService,
                                    errorHandler: ErrorHandler)
                                   (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           sessionDataId: UUID,
           benefitTypeUrl: String): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, sessionDataId) { implicit request =>
    val benefitType = mapFrom(benefitTypeUrl)
    Ok(pageView(StartDatePage(taxYear, request.stateBenefitsUserData, formsProvider.startDateForm(taxYear, request.user.isAgent, benefitType), benefitType)))
  }

  def submit(taxYear: Int,
             sessionDataId: UUID,
             benefitTypeUrl: String): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, sessionDataId).async { implicit request =>
    val benefitType = mapFrom(benefitTypeUrl)
    formsProvider.startDateForm(taxYear, request.user.isAgent, benefitType).bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(pageView(StartDatePage(taxYear, request.stateBenefitsUserData, formWithErrors, benefitType)))),
      formData => claimService.updateStartDate(request.stateBenefitsUserData, formData.toLocalDate.get).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(userData) => Redirect(getRedirectCall(taxYear, userData, benefitTypeUrl))
      }
    )
  }

  private def getRedirectCall(taxYear: Int,
                              userData: StateBenefitsUserData,
                              benefitTypeUrl: String): Call = {
    val sessionDataId = userData.sessionDataId.get
    if (userData.isFinished) ReviewClaimController.show(taxYear, sessionDataId, benefitTypeUrl) else EndDateQuestionController.show(taxYear, sessionDataId, benefitTypeUrl)
  }
}
