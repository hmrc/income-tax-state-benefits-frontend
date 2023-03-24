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

package models.audit

import models.StateBenefitsUserData
import play.api.libs.json.{Json, OWrites}

case class UnIgnoreStateBenefitAudit(taxYear: Int,
                                     userType: String,
                                     nino: String,
                                     mtdItId: String,
                                     benefitType: String,
                                     benefitDetails: BenefitDetails) {

  private val name = "UnIgnoreStateBenefitAudit"

  def toAuditModel: AuditModel[UnIgnoreStateBenefitAudit] = AuditModel(name, name, this)
}

object UnIgnoreStateBenefitAudit {
  implicit def writes: OWrites[UnIgnoreStateBenefitAudit] = Json.writes[UnIgnoreStateBenefitAudit]

  def apply(userType: String,
            stateBenefitsUserData: StateBenefitsUserData): UnIgnoreStateBenefitAudit = UnIgnoreStateBenefitAudit(
    taxYear = stateBenefitsUserData.taxYear,
    userType = userType,
    nino = stateBenefitsUserData.nino,
    mtdItId = stateBenefitsUserData.mtdItId,
    benefitType = stateBenefitsUserData.benefitType,
    benefitDetails = BenefitDetails(stateBenefitsUserData.claim.get)
  )
}
