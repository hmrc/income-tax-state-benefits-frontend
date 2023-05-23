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
import models.BenefitType
import models.requests.{UserPriorAndSessionDataRequest, UserSessionDataRequest}
import play.api.mvc.{ActionRefiner, Result}
import services.StateBenefitsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

case class UserPriorAndSessionDataRequestRefinerAction(taxYear: Int,
                                                       stateBenefitsService: StateBenefitsService,
                                                       benefitType: BenefitType,
                                                       errorHandler: ErrorHandler)
                                                      (implicit ec: ExecutionContext)
  extends ActionRefiner[UserSessionDataRequest, UserPriorAndSessionDataRequest] with FrontendHeaderCarrierProvider {

  override protected[actions] def executionContext: ExecutionContext = ec

  override protected[actions] def refine[A](input: UserSessionDataRequest[A]): Future[Either[Result, UserPriorAndSessionDataRequest[A]]] = {
    (input.stateBenefitsUserData.isCustomerOverride, input.stateBenefitsUserData.claim.get.benefitId) match {
      case (true, Some(benefitId)) =>
        stateBenefitsService.getPriorData(input.user, taxYear)(hc(input.request)).map {
          case Left(error) => Left(errorHandler.handleError(error.status)(input.request))
          case Right(incomeTaxUserData) =>
            val priorData = incomeTaxUserData.hmrcAllowancesFor(benefitType)
              .find(_.benefitId == benefitId)
            Right(UserPriorAndSessionDataRequest(input.stateBenefitsUserData, priorData, input.user, input.request))
        }
      case _ => Future.successful(Right(UserPriorAndSessionDataRequest(input.stateBenefitsUserData, None, input.user, input.request)))
    }
  }
}
