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
import controllers.routes.{ReviewClaimController, TaxPaidController}
import forms.FormsProvider
import models.pages.AmountPage
import models.{BenefitType, StateBenefitsUserData}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionHelper
import views.html.pages.AmountPageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmountController @Inject()(actionsProvider: ActionsProvider,
                                 formsProvider: FormsProvider,
                                 pageView: AmountPageView,
                                 claimService: ClaimService,
                                 errorHandler: ErrorHandler)
                                (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           benefitType: BenefitType,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId) { implicit request =>
    val taxPaid = request.stateBenefitsUserData.claim.flatMap(_.taxPaid)
    val form = formsProvider.amountForm(benefitType, request.user.isAgent, minAmount = taxPaid)
    Ok(pageView(AmountPage(taxYear, benefitType, request.stateBenefitsUserData, form)))
  }

  def submit(taxYear: Int,
             benefitType: BenefitType,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId).async { implicit request =>
    val taxPaid = request.stateBenefitsUserData.claim.flatMap(_.taxPaid)
    formsProvider.amountForm(benefitType, request.user.isAgent, minAmount = taxPaid).bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(pageView(AmountPage(taxYear, benefitType, request.stateBenefitsUserData, formWithErrors)))),
      amount => claimService.updateAmount(request.stateBenefitsUserData, amount).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(userData) => Redirect(getRedirectCall(taxYear, benefitType, userData))
      }
    )
  }

  private def getRedirectCall(taxYear: Int,
                              benefitType: BenefitType,
                              userData: StateBenefitsUserData) = {
    val sessionDataId = userData.sessionDataId.get
    val wasTaxPaid = userData.claim.get.taxPaidQuestion.get
    if (wasTaxPaid && !userData.isFinished) {
      TaxPaidController.show(taxYear, benefitType, sessionDataId)
    } else {
      ReviewClaimController.show(taxYear, benefitType, sessionDataId)
    }
  }
}
