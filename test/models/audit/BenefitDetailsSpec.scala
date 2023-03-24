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
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel

class BenefitDetailsSpec extends UnitTest {

  ".apply(...)" should {
    "create object from ClaimCYAModel" in {
      BenefitDetails.apply(aClaimCYAModel) shouldBe BenefitDetails(
        startDate = aClaimCYAModel.startDate,
        endDate = aClaimCYAModel.endDate,
        dateIgnored = aClaimCYAModel.dateIgnored,
        submittedOn = aClaimCYAModel.submittedOn,
        amount = aClaimCYAModel.amount,
        taxPaid = aClaimCYAModel.taxPaid
      )
    }
  }
}
