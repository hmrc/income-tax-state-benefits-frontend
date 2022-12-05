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

package support.builders

import models.{BenefitType, StateBenefitsUserData}
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.UserBuilder.aUser
import support.utils.TaxYearUtils.taxYearEOY

import java.util.UUID

object StateBenefitsUserDataBuilder {

  val aStateBenefitsUserData: StateBenefitsUserData = StateBenefitsUserData(
    benefitType = BenefitType.JobSeekersAllowance.typeName,
    sessionDataId = Some(UUID.fromString("558238ef-d2ff-4839-bd6d-307324d6fe37")),
    sessionId = aUser.sessionId,
    mtdItId = aUser.mtditid,
    nino = aUser.nino,
    taxYear = taxYearEOY,
    isPriorSubmission = false,
    claim = Some(aClaimCYAModel)
  )
}
