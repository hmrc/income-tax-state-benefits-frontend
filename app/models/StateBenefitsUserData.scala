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

package models

import play.api.libs.json.{Json, OFormat}

import java.util.UUID

case class StateBenefitsUserData(sessionDataId: Option[UUID] = None,
                                 sessionId: String,
                                 mtdItId: String,
                                 nino: String,
                                 taxYear: Int,
                                 isPriorSubmission: Boolean,
                                 claim: Option[ClaimCYAModel]) {

  lazy val isHmrcData: Boolean = claim.exists(_.isHmrcData)

  lazy val isFinished: Boolean = claim.exists(_.isFinished)
}

object StateBenefitsUserData {
  implicit val format: OFormat[StateBenefitsUserData] = Json.format[StateBenefitsUserData]

  def apply(taxYear: Int, user: User): StateBenefitsUserData = StateBenefitsUserData(
    sessionDataId = None,
    sessionId = user.sessionId,
    mtdItId = user.mtditid,
    nino = user.nino,
    taxYear = taxYear,
    isPriorSubmission = false,
    claim = None
  )

  def apply(taxYear: Int,
            user: User,
            benefitId: UUID,
            incomeTaxUserData: IncomeTaxUserData): Option[StateBenefitsUserData] = {
    val optionalHmrcStateBenefit = incomeTaxUserData.hmrcJobSeekersAllowances.find(item => item.benefitId == benefitId)
    val optionalCustomerStateBenefit = incomeTaxUserData.customerJobSeekersAllowances.find(item => item.benefitId == benefitId)

    lazy val stateBenefitsUserData = StateBenefitsUserData(
      sessionDataId = None,
      sessionId = user.sessionId,
      mtdItId = user.mtditid,
      nino = user.nino,
      taxYear = taxYear,
      isPriorSubmission = true,
      claim = None
    )

    (optionalHmrcStateBenefit, optionalCustomerStateBenefit) match {
      case (Some(benefit), _) => Some(stateBenefitsUserData.copy(claim = Some(ClaimCYAModel.mapFrom(benefit))))
      case (_, Some(benefit)) => Some(stateBenefitsUserData.copy(claim = Some(ClaimCYAModel.mapFrom(benefit))))
      case (None, None) => None
    }
  }
}
