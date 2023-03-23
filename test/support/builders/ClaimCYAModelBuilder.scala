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

package support.builders

import models.ClaimCYAModel
import support.builders.StateBenefitBuilder.aStateBenefit

object ClaimCYAModelBuilder {

  val aClaimCYAModel: ClaimCYAModel = ClaimCYAModel(
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
