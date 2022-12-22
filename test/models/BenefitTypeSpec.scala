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

import models.BenefitType._
import support.UnitTest

class BenefitTypeSpec extends UnitTest {

  "BenefitType objects" should {
    "have correct type names" in {
//      StatePension.typeName shouldBe "statePension"
//      StatePensionLumpSum.typeName shouldBe "statePensionLumpSum"
      EmploymentSupportAllowance.typeName shouldBe "employmentSupportAllowance"
      JobSeekersAllowance.typeName shouldBe "jobSeekersAllowance"
//      OtherStateBenefits.typeName shouldBe "otherStateBenefits"
    }
  }
}
