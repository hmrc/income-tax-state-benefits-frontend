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

import models.BenefitType.{EmploymentSupportAllowance, JobSeekersAllowance}
import support.UnitTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.CustomerAddedStateBenefitsDataBuilder.aCustomerAddedStateBenefitsData
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsDataBuilder.aStateBenefitsData

class IncomeTaxUserDataSpec extends UnitTest {

  ".hmrcAllowancesFor(...)" should {
    "return empty set" when {
      "when stateBenefits is None" in {
        val underTest = anIncomeTaxUserData.copy(stateBenefits = None)

        underTest.hmrcAllowancesFor(JobSeekersAllowance) shouldBe Set.empty
      }

      "when HMRC benefitType is None" in {
        val stateBenefitsData = aStateBenefitsData.copy(jobSeekersAllowances = None)
        val underTest = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(stateBenefitsData = Some(stateBenefitsData))))

        underTest.hmrcAllowancesFor(JobSeekersAllowance) shouldBe Set.empty
      }

      "when HMRC benefitType is empty Set" in {
        val stateBenefitsData = aStateBenefitsData.copy(employmentSupportAllowances = Some(Set.empty))
        val underTest = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(stateBenefitsData = Some(stateBenefitsData))))

        underTest.hmrcAllowancesFor(EmploymentSupportAllowance) shouldBe Set.empty
      }
    }

    "return HMRC benefitType when exist" in {
      val stateBenefitsData = aStateBenefitsData.copy(jobSeekersAllowances = Some(Set(aStateBenefit)))
      val underTest = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(stateBenefitsData = Some(stateBenefitsData))))

      underTest.hmrcAllowancesFor(JobSeekersAllowance) shouldBe Set(aStateBenefit)
    }
  }

  ".customerJobSeekersAllowances" should {
    "return empty set" when {
      "when stateBenefits is None" in {
        val underTest = anIncomeTaxUserData.copy(stateBenefits = None)

        underTest.customerAllowancesFor(EmploymentSupportAllowance) shouldBe Set.empty
      }

      "when customer AddedStateBenefitsData is None" in {
        val underTest = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(customerAddedStateBenefitsData = None)))

        underTest.customerAllowancesFor(JobSeekersAllowance) shouldBe Set.empty
      }

      "when customer benefitType is None" in {
        val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(employmentSupportAllowances = None)
        val underTest = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(customerAddedStateBenefitsData = Some(customerAddedStateBenefitsData))))

        underTest.customerAllowancesFor(EmploymentSupportAllowance) shouldBe Set.empty
      }

      "when benefitType is empty Set" in {
        val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(employmentSupportAllowances = Some(Set.empty))
        val underTest = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(customerAddedStateBenefitsData = Some(customerAddedStateBenefitsData))))

        underTest.customerAllowancesFor(EmploymentSupportAllowance) shouldBe Set.empty
      }
    }

    "return benefitType when exist" in {
      val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData.copy(jobSeekersAllowances = Some(Set(aCustomerAddedStateBenefit)))
      val underTest = anIncomeTaxUserData.copy(stateBenefits = Some(anAllStateBenefitsData.copy(customerAddedStateBenefitsData = Some(customerAddedStateBenefitsData))))

      underTest.customerAllowancesFor(JobSeekersAllowance) shouldBe Set(aCustomerAddedStateBenefit)
    }
  }
}
