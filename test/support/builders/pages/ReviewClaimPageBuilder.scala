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

package support.builders.pages

import models.BenefitType.JobSeekersAllowance
import models.pages.ReviewClaimPage
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.utils.TaxYearUtils.taxYearEOY

object ReviewClaimPageBuilder {

  val aReviewClaimPage: ReviewClaimPage = new ReviewClaimPage(
    taxYear = taxYearEOY,
    benefitType = JobSeekersAllowance,
    sessionDataId = aStateBenefitsUserData.sessionDataId.get,
    isInYear = false,
    isCustomerAdded = !aStateBenefitsUserData.isPriorSubmission,
    isIgnored = false,
    itemsFirstDate = aClaimCYAModel.startDate,
    itemsSecondDate = aClaimCYAModel.endDate.get,
    startDate = aClaimCYAModel.startDate,
    endDateQuestion = aClaimCYAModel.endDateQuestion,
    endDate = aClaimCYAModel.endDate,
    amount = aClaimCYAModel.amount,
    taxPaidQuestion = aClaimCYAModel.taxPaidQuestion,
    taxPaid = aClaimCYAModel.taxPaid
  )
}
