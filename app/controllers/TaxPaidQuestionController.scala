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
import controllers.routes.{AmountController, ReviewClaimController, TaxPaidController}
import forms.FormsProvider
import models.pages.TaxPaidQuestionPage
import models.{BenefitType, StateBenefitsUserData}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.TaxPaidQuestionPageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxPaidQuestionController @Inject()(actionsProvider: ActionsProvider,
                                          formsProvider: FormsProvider,
                                          pageView: TaxPaidQuestionPageView,
                                          claimService: ClaimService,
                                          errorHandler: ErrorHandler)
                                         (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           benefitType: BenefitType,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId) { implicit request =>
    val pageForm = formsProvider.taxTakenOffYesNoForm(taxYear, benefitType, request.user.isAgent, request.stateBenefitsUserData.claim.get)
    Ok(pageView(TaxPaidQuestionPage(taxYear, benefitType, request.stateBenefitsUserData, pageForm)))
  }

  def submit(taxYear: Int,
             benefitType: BenefitType,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId).async { implicit request =>
    formsProvider.taxTakenOffYesNoForm(taxYear, benefitType, request.user.isAgent, request.stateBenefitsUserData.claim.get).bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(pageView(TaxPaidQuestionPage(taxYear, benefitType, request.stateBenefitsUserData, formWithErrors)))),
      yesNoValue => claimService.updateTaxPaidQuestion(request.stateBenefitsUserData, yesNoValue).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(userData) => Redirect(getRedirectCall(taxYear, benefitType, userData))
      }
    )
  }

  private def getRedirectCall(taxYear: Int,
                              benefitType: BenefitType,
                              userData: StateBenefitsUserData): Call = {
    val sessionDataId = userData.sessionDataId.get
    val noAmount = userData.claim.get.amount.isEmpty
    if (userData.isFinished) {
      ReviewClaimController.show(taxYear, benefitType, sessionDataId)
    } else {
      if (noAmount) AmountController.show(taxYear, benefitType, sessionDataId) else TaxPaidController.show(taxYear, benefitType, sessionDataId)
    }
  }
}
