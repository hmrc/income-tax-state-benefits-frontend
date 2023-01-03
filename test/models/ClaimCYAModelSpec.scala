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

import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.StateBenefitBuilder.aStateBenefit

class ClaimCYAModelSpec extends UnitTest {

  ".isFinished" should {
    "return true when model has all the data" in {
      aClaimCYAModel.isFinished shouldBe true
    }

    "return true when endDateQuestion is false and rest of the data is completed" in {
      aClaimCYAModel.copy(endDateQuestion = Some(false), endDate = None).isFinished shouldBe true
    }

    "return true when taxPaidQuestion is false and rest of the dats is completed" in {
      aClaimCYAModel.copy(taxPaidQuestion = Some(false), taxPaid = None).isFinished shouldBe true
    }

    "return false" when {
      "endDateQuestion is Yes and endDate is empty" in {
        aClaimCYAModel.copy(endDateQuestion = Some(true), endDate = None).isFinished shouldBe false
      }

      "endDateQuestion is empty" in {
        aClaimCYAModel.copy(endDateQuestion = None).isFinished shouldBe false
      }

      "amount is empty" in {
        aClaimCYAModel.copy(amount = None).isFinished shouldBe false
      }

      "taxPaidQuestion is Yes and taxPaid is empty" in {
        aClaimCYAModel.copy(taxPaidQuestion = Some(true), taxPaid = None).isFinished shouldBe false
      }

      "taxPaidQuestion is empty" in {
        aClaimCYAModel.copy(taxPaidQuestion = None).isFinished shouldBe false
      }

      "taxPaid is empty" in {
        aClaimCYAModel.copy(taxPaid = None).isFinished shouldBe false
      }
    }
  }

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
        taxPaid = stateBenefit.taxPaid,
        isHmrcData = true
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
        taxPaid = aStateBenefit.taxPaid,
        isHmrcData = true
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
        taxPaid = stateBenefit.taxPaid,
        isHmrcData = false
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
        taxPaid = aCustomerAddedStateBenefit.taxPaid,
        isHmrcData = false
      )
    }
  }
}
