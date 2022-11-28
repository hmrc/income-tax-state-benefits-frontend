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
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.StateBenefitBuilder.aStateBenefit

class ClaimCYAModelSpec extends UnitTest {

  ".mapFrom(stateBenefit: StateBenefit)" should {
    "return ClaimCYAModel from given StateBenefit when endDate and taxPaid are not defined" in {
      val stateBenefit = aStateBenefit.copy(endDate = None, taxPaid = None)

      ClaimCYAModel.mapFrom(stateBenefit) shouldBe ClaimCYAModel(
        benefitId = Some(stateBenefit.benefitId),
        startDate = stateBenefit.startDate,
        endDateQuestion = Some(false),
        endDate = stateBenefit.endDate,
        dateIgnored = stateBenefit.dateIgnored,
        submittedOn = stateBenefit.submittedOn,
        amount = stateBenefit.amount,
        taxPaidQuestion = Some(false),
        taxPaid = stateBenefit.taxPaid
      )
    }

    "return ClaimCYAModel from given StateBenefit when endDate and taxPaid are defined" in {
      ClaimCYAModel.mapFrom(aStateBenefit) shouldBe ClaimCYAModel(
        benefitId = Some(aStateBenefit.benefitId),
        startDate = aStateBenefit.startDate,
        endDateQuestion = Some(true),
        endDate = aStateBenefit.endDate,
        dateIgnored = aStateBenefit.dateIgnored,
        submittedOn = aStateBenefit.submittedOn,
        amount = aStateBenefit.amount,
        taxPaidQuestion = Some(true),
        taxPaid = aStateBenefit.taxPaid
      )
    }
  }

  ".mapFrom(customerAddedStateBenefit: CustomerAddedStateBenefit)" should {
    "return ClaimCYAModel from given StateBenefit when endDate and taxPaid are not defined" in {
      val stateBenefit = aCustomerAddedStateBenefit.copy(endDate = None, taxPaid = None)

      ClaimCYAModel.mapFrom(stateBenefit) shouldBe ClaimCYAModel(
        benefitId = Some(stateBenefit.benefitId),
        startDate = stateBenefit.startDate,
        endDateQuestion = Some(false),
        endDate = stateBenefit.endDate,
        dateIgnored = None,
        submittedOn = stateBenefit.submittedOn,
        amount = stateBenefit.amount,
        taxPaidQuestion = Some(false),
        taxPaid = stateBenefit.taxPaid
      )
    }

    "return ClaimCYAModel from given StateBenefit when endDate and taxPaid are defined" in {
      ClaimCYAModel.mapFrom(aCustomerAddedStateBenefit) shouldBe ClaimCYAModel(
        benefitId = Some(aCustomerAddedStateBenefit.benefitId),
        startDate = aCustomerAddedStateBenefit.startDate,
        endDateQuestion = Some(true),
        endDate = aCustomerAddedStateBenefit.endDate,
        dateIgnored = None,
        submittedOn = aCustomerAddedStateBenefit.submittedOn,
        amount = aCustomerAddedStateBenefit.amount,
        taxPaidQuestion = Some(true),
        taxPaid = aCustomerAddedStateBenefit.taxPaid
      )
    }
  }
}
