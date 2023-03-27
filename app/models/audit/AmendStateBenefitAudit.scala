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

import models.{BenefitType, IncomeTaxUserData, StateBenefitsUserData, User}
import play.api.libs.json.{Json, OWrites}

case class AmendStateBenefitAudit(taxYear: Int,
                                  userType: String,
                                  nino: String,
                                  mtdItId: String,
                                  benefitType: String,
                                  originalBenefitDetails: BenefitDetails,
                                  updatedBenefitDetails: BenefitDetails) {

  private val name = "AmendStateBenefitsClaim"

  def toAuditModel: AuditModel[AmendStateBenefitAudit] = AuditModel(name, name, this)
}

object AmendStateBenefitAudit {
  implicit def writes: OWrites[AmendStateBenefitAudit] = Json.writes[AmendStateBenefitAudit]

  def apply(user: User,
            benefitType: BenefitType,
            priorData: IncomeTaxUserData,
            sessionData: StateBenefitsUserData): AmendStateBenefitAudit = {
    val originalBenefitDetails = StateBenefitsUserData(sessionData.taxYear, benefitType, user, sessionData.claim.get.benefitId.get, priorData).get

    AmendStateBenefitAudit(
      taxYear = sessionData.taxYear,
      userType = user.affinityGroup,
      nino = sessionData.nino,
      mtdItId = sessionData.mtdItId,
      benefitType = sessionData.benefitType,
      originalBenefitDetails = BenefitDetails(originalBenefitDetails.claim.get),
      updatedBenefitDetails = BenefitDetails(sessionData.claim.get)
    )
  }
}
