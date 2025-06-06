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
import controllers.routes.ReviewClaimController
import forms.FormsProvider
import models.BenefitType
import models.pages.TaxPaidPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.TaxPaidPageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxPaidController @Inject()(actionsProvider: ActionsProvider,
                                  formsProvider: FormsProvider,
                                  pageView: TaxPaidPageView,
                                  claimService: ClaimService,
                                  errorHandler: ErrorHandler)
                                 (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int,
           benefitType: BenefitType,
           sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId) { implicit request =>
    val claimAmount = request.stateBenefitsUserData.claim.flatMap(_.amount).get
    val form = formsProvider.taxPaidAmountForm(benefitType, request.user.isAgent, maxAmount = claimAmount)
    Ok(pageView(TaxPaidPage(taxYear, benefitType, request.stateBenefitsUserData, form)))
  }

  def submit(taxYear: Int,
             benefitType: BenefitType,
             sessionDataId: UUID): Action[AnyContent] = actionsProvider.endOfYearSessionDataFor(taxYear, benefitType, sessionDataId).async { implicit request =>
    val claimAmount = request.stateBenefitsUserData.claim.flatMap(_.amount).get
    formsProvider.taxPaidAmountForm(benefitType, request.user.isAgent, maxAmount = claimAmount).bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(pageView(TaxPaidPage(taxYear, benefitType, request.stateBenefitsUserData, formWithErrors)))),
      amount => claimService.updateTaxPaidAmount(request.stateBenefitsUserData, amount).map {
        case Left(_) => errorHandler.internalServerError()
        case Right(userData) => Redirect(ReviewClaimController.show(taxYear, benefitType, userData.sessionDataId.get))
      }
    )
  }
}
