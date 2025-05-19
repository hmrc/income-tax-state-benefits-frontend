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
import controllers.routes.{ReviewClaimController, TaxPaidQuestionController}
import forms.{DateForm, FormsProvider}
import models.pages.EndDatePage
import models.{BenefitType, StateBenefitsUserData}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.EndDatePageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EndDateController @Inject()(actionsProvider: ActionsProvider,
                                  formsProvider: FormsProvider,
                                  pageView: EndDatePageView,
                                  claimService: ClaimService,
                                  errorHandler: ErrorHandler)
                                 (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int,
           benefitType: BenefitType,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId) { implicit request =>
    Ok(pageView(EndDatePage(taxYear, benefitType, request.stateBenefitsUserData, DateForm.dateForm())))
  }

  def submit(taxYear: Int,
             benefitType: BenefitType,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId).async { implicit request =>
    val sessionData = request.stateBenefitsUserData
    val simpleDateForm = DateForm.dateForm().bindFromRequest()
    formsProvider.validatedEndDateForm(simpleDateForm, taxYear, benefitType, request.user.isAgent, sessionData.claim.get.startDate).fold(
      formWithErrors => Future.successful(BadRequest(pageView(EndDatePage(taxYear, benefitType, sessionData, formWithErrors)))),
      formData => claimService.updateEndDate(sessionData, formData.toLocalDate.get).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(userData) => Redirect(getRedirectCall(taxYear, benefitType, userData))
      }
    )
  }

  private def getRedirectCall(taxYear: Int,
                              benefitType: BenefitType,
                              userData: StateBenefitsUserData) = {
    val sessionDataId = userData.sessionDataId.get
    val isFinished = userData.isFinished
    if (isFinished) ReviewClaimController.show(taxYear, benefitType, sessionDataId) else TaxPaidQuestionController.show(taxYear, benefitType, sessionDataId)
  }
}
