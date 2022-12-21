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

package actions

import config.ErrorHandler
import controllers.jobseekers.routes.ClaimsController
import models.BenefitType
import models.requests.{AuthorisationRequest, UserSessionDataRequest}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.StateBenefitsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

case class UserSessionDataRequestRefinerAction(taxYear: Int,
                                               benefitType: BenefitType,
                                               sessionDataId: UUID,
                                               stateBenefitsService: StateBenefitsService,
                                               errorHandler: ErrorHandler)
                                              (implicit ec: ExecutionContext)
  extends ActionRefiner[AuthorisationRequest, UserSessionDataRequest] with FrontendHeaderCarrierProvider {

  override protected[actions] def executionContext: ExecutionContext = ec

  override protected[actions] def refine[A](input: AuthorisationRequest[A]): Future[Either[Result, UserSessionDataRequest[A]]] = {
    stateBenefitsService.getUserSessionData(input.user, sessionDataId)(hc(input.request)).map {
      case Left(error) if error.status == NOT_FOUND => Left(Redirect(ClaimsController.show(taxYear, benefitType)))
      case Left(_) => Left(errorHandler.handleError(INTERNAL_SERVER_ERROR)(input.request))
      case Right(stateBenefitsUserData) => Right(UserSessionDataRequest(stateBenefitsUserData, input.user, input.request))
    }
  }
}
