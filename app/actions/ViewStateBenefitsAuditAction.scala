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

import models.BenefitType
import models.audit.ViewStateBenefitsAudit
import models.requests.UserPriorDataRequest
import play.api.mvc._
import services.AuditService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ViewStateBenefitsAuditAction @Inject()(taxYear: Int,
                                                  benefitType: BenefitType,
                                                  auditService: AuditService)
                                                 (implicit ec: ExecutionContext) extends ActionFilter[UserPriorDataRequest] with FrontendHeaderCarrierProvider {
  override protected[actions] def executionContext: ExecutionContext = ec

  override protected[actions] def filter[A](userPriorDataRequest: UserPriorDataRequest[A]): Future[Option[Result]] = Future.successful {
    val auditModel = ViewStateBenefitsAudit(taxYear, userPriorDataRequest.user, benefitType, userPriorDataRequest.incomeTaxUserData)
    auditService.sendAudit(auditModel.toAuditModel)(hc(userPriorDataRequest.request), ViewStateBenefitsAudit.writes)
    None
  }
}
