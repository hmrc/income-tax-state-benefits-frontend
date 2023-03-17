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
import support.UnitTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.CustomerAddedStateBenefitsDataBuilder.aCustomerAddedStateBenefitsData
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsDataBuilder.aStateBenefitsData
import support.builders.UserBuilder.aUser
import support.providers.TaxYearProvider

class ViewStateBenefitsAuditSpec extends UnitTest
  with TaxYearProvider {

  ".apply" should {
    "create correct Audit object" in {
      val stateBenefitsData = aStateBenefitsData.copy(jobSeekersAllowances = Some(Set(aStateBenefit, aStateBenefit.copy(dateIgnored = None))))
      val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(jobSeekersAllowances = Some(Set(aCustomerAddedStateBenefit)))
      val incomeTaxUserData = anIncomeTaxUserData.copy(Some(anAllStateBenefitsData.copy(Some(stateBenefitsData), Some(customerAddedStateBenefitsData))))

      ViewStateBenefitsAudit.apply(taxYear, aUser, JobSeekersAllowance, incomeTaxUserData) shouldBe ViewStateBenefitsAudit(
        taxYear = taxYear,
        userType = aUser.affinityGroup,
        nino = aUser.nino,
        mtdItId = aUser.mtditid,
        benefitType = JobSeekersAllowance.typeName,
        benefitsData = Set(BenefitData.mapFrom(aStateBenefit.copy(dateIgnored = None)), BenefitData.mapFrom(aCustomerAddedStateBenefit)),
        ignoredBenefitsData = Set(BenefitData.mapFrom(aStateBenefit))
      )
    }
  }
}
