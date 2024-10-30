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
import forms.FormsProvider
import models.pages.ClaimsPage
import models.{BenefitType, StateBenefitsUserData}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StateBenefitsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.InYearUtil.inYear
import utils.SessionHelper
import views.html.pages.ClaimsPageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimsController @Inject()(actionsProvider: ActionsProvider,
                                 pageView: ClaimsPageView,
                                 stateBenefitsService: StateBenefitsService,
                                 formsProvider: FormsProvider,
                                 errorHandler: ErrorHandler)
                                (implicit ec: ExecutionContext, mcc: MessagesControllerComponents, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def show(taxYear: Int,
           benefitType: BenefitType): Action[AnyContent] = actionsProvider.priorDataWithViewStateBenefitsAudit(taxYear, benefitType) { implicit request =>
    val form = formsProvider.addAnotherClaimYesNoForm(request.user.isAgent)
    Ok(pageView(ClaimsPage(taxYear, benefitType, inYear(taxYear), request.incomeTaxUserData, form)))
  }

  def submit(taxYear: Int,
             benefitType: BenefitType): Action[AnyContent] = actionsProvider
    .priorDataWithViewStateBenefitsAudit(taxYear, benefitType).async { implicit request =>

      formsProvider.addAnotherClaimYesNoForm(request.user.isAgent).bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(pageView(ClaimsPage(taxYear, benefitType, inYear(taxYear), request.incomeTaxUserData, formWithErrors)))),
        yesNoValue => if (yesNoValue) {
          stateBenefitsService.createSessionData(StateBenefitsUserData(taxYear, benefitType, request.user)).map {
            case Left(_) => errorHandler.internalServerError()
            case Right(uuid) => Redirect(routes.StartDateController.show(taxYear, benefitType, uuid))
          }
        }
        else {
          if (appConfig.sectionCompletedQuestionEnabled) {
            Future.successful(Redirect(routes.SectionCompletedStateController.show(taxYear, benefitType)))
          } else {
            Future.successful(Redirect(routes.SummaryController.show(taxYear)))
          }
        }
      )
    }
}
