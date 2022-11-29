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

import support.UnitTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.CustomerAddedStateBenefitsDataBuilder.aCustomerAddedStateBenefitsData
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsDataBuilder.aStateBenefitsData
import support.builders.UserBuilder.aUser

import java.util.UUID

class StateBenefitsUserDataSpec extends UnitTest {

  private val anyTaxYear = 2022

  ".apply(taxYear: Int, user: User)" should {
    "create correct StateBenefitsUserData instance" in {
      StateBenefitsUserData.apply(anyTaxYear, aUser) shouldBe StateBenefitsUserData(
        sessionDataId = None,
        sessionId = aUser.sessionId,
        mtdItId = aUser.mtditid,
        nino = aUser.nino,
        taxYear = anyTaxYear,
        isPriorSubmission = false,
        claim = None
      )
    }
  }

  ".apply(taxYear: Int, user: User, benefitId: UUID, incomeTaxUserData: IncomeTaxUserData)" should {
    "create correct StateBenefitsUserData instance when benefitId not found" in {
      val unknownBenefitId = UUID.randomUUID()

      StateBenefitsUserData.apply(anyTaxYear, aUser, unknownBenefitId, anIncomeTaxUserData) shouldBe None
    }

    "create correct StateBenefitsUserData instance when benefitId found in HMRC added data" in {
      val stateBenefitsData = aStateBenefitsData.copy(jobSeekersAllowances = Some(Set(aStateBenefit)))
      val incomeTaxUserData = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(stateBenefitsData = stateBenefitsData)))

      StateBenefitsUserData.apply(anyTaxYear, aUser, aStateBenefit.benefitId, incomeTaxUserData) shouldBe Some(StateBenefitsUserData(
        sessionDataId = None,
        sessionId = aUser.sessionId,
        mtdItId = aUser.mtditid,
        nino = aUser.nino,
        taxYear = anyTaxYear,
        isPriorSubmission = true,
        claim = Some(ClaimCYAModel.mapFrom(aStateBenefit))
      ))
    }

    "create correct StateBenefitsUserData instance when benefitId found in Customer added data" in {
      val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(jobSeekersAllowances = Some(Set(aCustomerAddedStateBenefit)))
      val incomeTaxUserData = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(customerAddedStateBenefitsData = Some(customerAddedStateBenefitsData))))

      StateBenefitsUserData.apply(anyTaxYear, aUser, aCustomerAddedStateBenefit.benefitId, incomeTaxUserData) shouldBe Some(StateBenefitsUserData(
        sessionDataId = None,
        sessionId = aUser.sessionId,
        mtdItId = aUser.mtditid,
        nino = aUser.nino,
        taxYear = anyTaxYear,
        isPriorSubmission = true,
        claim = Some(ClaimCYAModel.mapFrom(aCustomerAddedStateBenefit))
      ))
    }
  }
}
