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

import play.api.libs.json.{Json, OFormat}

import java.time.{Instant, LocalDate}
import java.util.UUID

case class ClaimCYAModel(benefitId: Option[UUID] = None,
                         startDate: LocalDate,
                         endDateQuestion: Option[Boolean] = None,
                         endDate: Option[LocalDate] = None,
                         dateIgnored: Option[Instant] = None,
                         submittedOn: Option[Instant] = None,
                         amount: Option[BigDecimal] = None,
                         taxPaidQuestion: Option[Boolean] = None,
                         taxPaid: Option[BigDecimal] = None) {

  lazy val isFinished: Boolean = {
    val endDateIsFinished: Boolean = endDateQuestion match {
      case None => false
      case Some(false) => true
      case Some(true) => endDate.isDefined
    }
    val taxPaidIsFinished: Boolean = taxPaidQuestion match {
      case None => false
      case Some(false) => true
      case Some(true) => taxPaid.isDefined
    }

    endDateIsFinished &&
      amount.isDefined &&
      taxPaidIsFinished
  }
}

object ClaimCYAModel {
  implicit val format: OFormat[ClaimCYAModel] = Json.format[ClaimCYAModel]

  def mapFrom(stateBenefit: StateBenefit): ClaimCYAModel = ClaimCYAModel(
    benefitId = Some(stateBenefit.benefitId),
    startDate = stateBenefit.startDate,
    endDateQuestion = Some(stateBenefit.endDate.isDefined),
    endDate = stateBenefit.endDate,
    dateIgnored = stateBenefit.dateIgnored,
    submittedOn = stateBenefit.submittedOn,
    amount = stateBenefit.amount,
    taxPaidQuestion = Some(stateBenefit.taxPaid.isDefined),
    taxPaid = stateBenefit.taxPaid
  )

  def mapFrom(customerAddedStateBenefit: CustomerAddedStateBenefit): ClaimCYAModel = ClaimCYAModel(
    benefitId = Some(customerAddedStateBenefit.benefitId),
    startDate = customerAddedStateBenefit.startDate,
    endDateQuestion = Some(customerAddedStateBenefit.endDate.isDefined),
    endDate = customerAddedStateBenefit.endDate,
    dateIgnored = None,
    submittedOn = customerAddedStateBenefit.submittedOn,
    amount = customerAddedStateBenefit.amount,
    taxPaidQuestion = Some(customerAddedStateBenefit.taxPaid.isDefined),
    taxPaid = customerAddedStateBenefit.taxPaid
  )
}
