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

package actions

import config.ErrorHandler
import controllers.routes.ClaimsController
import models.requests.UserSessionDataRequest
import models.{BenefitType, IncomeTaxUserData, StateBenefitsUserData}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.StateBenefitsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

case class SaveAndContinueFilterAction(taxYear: Int,
                                       benefitType: BenefitType,
                                       stateBenefitsService: StateBenefitsService,
                                       errorHandler: ErrorHandler)
                                      (implicit ec: ExecutionContext) extends ActionFilter[UserSessionDataRequest] with FrontendHeaderCarrierProvider {

  override protected[actions] def executionContext: ExecutionContext = ec

  override protected[actions] def filter[A](input: UserSessionDataRequest[A]): Future[Option[Result]] = {
    def hasUpdates(priorData: IncomeTaxUserData, sessionData: StateBenefitsUserData): Boolean =
      !StateBenefitsUserData(taxYear, benefitType, input.user, sessionData.claim.get.benefitId.get, priorData)
        .map(_.copy(sessionDataId = sessionData.sessionDataId))
        .contains(sessionData)

    if (input.stateBenefitsUserData.isPriorSubmission) {
      stateBenefitsService.getPriorData(input.user, taxYear)(hc(input.request)).map {
        case Left(error) => Some(errorHandler.handleError(error.status)(input.request))
        case Right(priorData) =>
          if (hasUpdates(priorData, input.stateBenefitsUserData)) None else Some(Redirect(ClaimsController.show(taxYear, benefitType)))
      }
    } else {
      Future.successful(None)
    }
  }
}
