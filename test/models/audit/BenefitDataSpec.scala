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

import support.UnitTest
import support.builders.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.audit.BenefitDataBuilder.aBenefitData
import support.utils.TaxYearUtils.taxYearEOY

import java.time.Instant

class BenefitDataSpec extends UnitTest {

  ".isIgnored" should {
    "return true when dateIgnored is defined" in {
      val underTest = aBenefitData.copy(dateIgnored = Some(Instant.parse(s"$taxYearEOY-11-17T19:23:00Z")))

      underTest.isIgnored shouldBe true
    }

    "return false when dateIgnored is not defined" in {
      val underTest = aBenefitData.copy(dateIgnored = None)

      underTest.isIgnored shouldBe false
    }
  }

  ".mapFrom" when {
    "given StateBenefit" should {
      "return correct object" in {
        BenefitData.mapFrom(aStateBenefit) shouldBe BenefitData(
          startDate = aStateBenefit.startDate,
          endDate = aStateBenefit.endDate,
          amount = aStateBenefit.amount,
          dateIgnored = aStateBenefit.dateIgnored
        )
      }
    }

    "given CustomerAddedStateBenefit" should {
      "return correct object" in {
        BenefitData.mapFrom(aCustomerAddedStateBenefit) shouldBe BenefitData(
          startDate = aCustomerAddedStateBenefit.startDate,
          endDate = aCustomerAddedStateBenefit.endDate,
          amount = aCustomerAddedStateBenefit.amount,
          dateIgnored = None
        )
      }
    }
  }
}
