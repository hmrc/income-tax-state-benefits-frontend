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

package models

import models.BenefitDataType.{CustomerAdded, CustomerOverride, HmrcData}
import play.api.libs.json.{Json, OFormat}

import java.util.UUID

case class StateBenefitsUserData(benefitType: String,
                                 sessionDataId: Option[UUID] = None,
                                 sessionId: String,
                                 mtdItId: String,
                                 nino: String,
                                 taxYear: Int,
                                 benefitDataType: String,
                                 claim: Option[ClaimCYAModel]) {

  lazy val isPriorSubmission: Boolean = claim.exists(_.benefitId.isDefined)
  lazy val isNewClaim: Boolean = !isPriorSubmission
  lazy val isHmrcData: Boolean = benefitDataType == HmrcData.name
  lazy val isCustomerAdded: Boolean = benefitDataType == CustomerAdded.name
  lazy val isCustomerOverride: Boolean = benefitDataType == CustomerOverride.name
  lazy val isFinished: Boolean = claim.exists(_.isFinished)
}

object StateBenefitsUserData {
  implicit val format: OFormat[StateBenefitsUserData] = Json.format[StateBenefitsUserData]

  def apply(taxYear: Int,
            benefitType: BenefitType,
            user: User): StateBenefitsUserData = StateBenefitsUserData(
    benefitType = benefitType.typeName,
    sessionDataId = None,
    sessionId = user.sessionId,
    mtdItId = user.mtditid,
    nino = user.nino,
    taxYear = taxYear,
    benefitDataType = CustomerAdded.name,
    claim = None
  )

  def apply(taxYear: Int,
            benefitType: BenefitType,
            user: User,
            benefitId: UUID,
            incomeTaxUserData: IncomeTaxUserData): Option[StateBenefitsUserData] = {
    val optionalHmrcStateBenefit = incomeTaxUserData.hmrcAllowancesFor(benefitType).find(_.benefitId == benefitId)
    val optionalCustomerStateBenefit = incomeTaxUserData.customerAllowancesFor(benefitType).find(_.benefitId == benefitId)

    lazy val userData = StateBenefitsUserData(
      benefitType = benefitType.typeName,
      sessionDataId = None,
      sessionId = user.sessionId,
      mtdItId = user.mtditid,
      nino = user.nino,
      taxYear = taxYear,
      benefitDataType = HmrcData.name,
      claim = None
    )

    (optionalHmrcStateBenefit, optionalCustomerStateBenefit) match {
      case (Some(benefit), None) => Some(userData.copy(claim = Some(ClaimCYAModel.mapFrom(benefit))))
      case (None, Some(benefit)) => Some(userData.copy(benefitDataType = CustomerAdded.name, claim = Some(ClaimCYAModel.mapFrom(benefit))))
      case (Some(_), Some(benefit)) => Some(userData.copy(benefitDataType = CustomerOverride.name, claim = Some(ClaimCYAModel.mapFrom(benefit))))
      case (None, None) => None
    }
  }
}
