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

import models.BenefitType.JobSeekersAllowance
import models.StateBenefitsUserData
import support.UnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser

class AmendStateBenefitAuditSpec extends UnitTest {

  ".apply(...)" should {
    "create correct object" in {
      val sessionData = aStateBenefitsUserData
      val priorData = anIncomeTaxUserData

      val originalBenefitDetails = StateBenefitsUserData(sessionData.taxYear, JobSeekersAllowance, aUser, sessionData.claim.get.benefitId.get, priorData)
        .map(_.copy(sessionDataId = sessionData.sessionDataId)).get

      AmendStateBenefitAudit.apply(aUser, JobSeekersAllowance, priorData, sessionData) shouldBe AmendStateBenefitAudit(
        taxYear = sessionData.taxYear,
        userType = aUser.affinityGroup,
        nino = sessionData.nino,
        mtdItId = sessionData.mtdItId,
        benefitType = sessionData.benefitType,
        originalBenefitDetails = BenefitDetails(originalBenefitDetails.claim.get),
        updatedBenefitDetails = BenefitDetails(sessionData.claim.get)
      )
    }
  }
}
