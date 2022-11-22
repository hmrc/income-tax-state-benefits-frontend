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

package models.pages.jobseekers

import models.StateBenefitsUserData

import java.time.LocalDate
import java.util.UUID

case class ReviewJobSeekersAllowanceClaimPage(taxYear: Int,
                                              sessionDataId: UUID,
                                              isInYear: Boolean,
                                              isUsingCustomerData: Boolean,
                                              startDate: Option[LocalDate],
                                              endDateQuestion: Option[Boolean],
                                              endDate: Option[LocalDate],
                                              amount: Option[BigDecimal],
                                              taxPaidQuestion: Option[Boolean],
                                              taxPaid: Option[BigDecimal])


object ReviewJobSeekersAllowanceClaimPage {

  def apply(taxYear: Int,
            isInYear: Boolean,
            stateBenefitsUserData: StateBenefitsUserData): ReviewJobSeekersAllowanceClaimPage = {

    ReviewJobSeekersAllowanceClaimPage(
      taxYear = taxYear,
      sessionDataId = stateBenefitsUserData.sessionDataId.get,
      isInYear = isInYear,
      isUsingCustomerData = !stateBenefitsUserData.isPriorSubmission,
      startDate = stateBenefitsUserData.claim.map(_.startDate),
      endDateQuestion = stateBenefitsUserData.claim.flatMap(_.endDateQuestion),
      endDate = stateBenefitsUserData.claim.flatMap(_.endDate),
      amount = stateBenefitsUserData.claim.flatMap(_.amount),
      taxPaidQuestion = stateBenefitsUserData.claim.flatMap(_.taxPaidQuestion),
      taxPaid = stateBenefitsUserData.claim.flatMap(_.taxPaid)
    )
  }
}