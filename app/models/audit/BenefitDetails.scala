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

import models.ClaimCYAModel
import play.api.libs.json.{Json, OWrites}

import java.time.{Instant, LocalDate}

case class BenefitDetails(startDate: LocalDate,
                          endDate: Option[LocalDate] = None,
                          dateIgnored: Option[Instant] = None,
                          submittedOn: Option[Instant] = None,
                          amount: Option[BigDecimal] = None,
                          taxPaid: Option[BigDecimal] = None)

object BenefitDetails {
  implicit def writes: OWrites[BenefitDetails] = Json.writes[BenefitDetails]

  def apply(claimCYAModel: ClaimCYAModel): BenefitDetails = BenefitDetails(
    startDate = claimCYAModel.startDate,
    endDate = claimCYAModel.endDate,
    dateIgnored = claimCYAModel.dateIgnored,
    submittedOn = claimCYAModel.submittedOn,
    amount = claimCYAModel.amount,
    taxPaid = claimCYAModel.taxPaid
  )
}
