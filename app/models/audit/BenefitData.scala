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

import models.{CustomerAddedStateBenefit, StateBenefit}
import play.api.libs.json.{Json, OWrites}

import java.time.{Instant, LocalDate}

case class BenefitData(startDate: LocalDate,
                       endDate: Option[LocalDate] = None,
                       amount: Option[BigDecimal] = None,
                       dateIgnored: Option[Instant] = None) {

  lazy val isIgnored: Boolean = dateIgnored.isDefined
}

object BenefitData {
  implicit def writes: OWrites[BenefitData] = Json.writes[BenefitData]

  def mapFrom(stateBenefit: StateBenefit): BenefitData = BenefitData(
    startDate = stateBenefit.startDate,
    endDate = stateBenefit.endDate,
    amount = stateBenefit.amount,
    dateIgnored = stateBenefit.dateIgnored
  )

  def mapFrom(stateBenefit: CustomerAddedStateBenefit): BenefitData = BenefitData(
    startDate = stateBenefit.startDate,
    endDate = stateBenefit.endDate,
    amount = stateBenefit.amount,
    dateIgnored = None
  )
}
