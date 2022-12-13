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

import play.api.libs.json.{Json, OFormat}

case class IncomeTaxUserData(stateBenefits: Option[AllStateBenefitsData] = None) {

  def hmrcAllowancesFor(benefitType: BenefitType): Set[StateBenefit] = benefitType match {
    case BenefitType.StatePension => ???
    case BenefitType.StatePensionLumpSum => ???
    case BenefitType.EmploymentSupportAllowance => hmrcEmploymentSupportAllowances
    case BenefitType.JobSeekersAllowance => hmrcJobSeekersAllowances
    case BenefitType.OtherStateBenefits => ???
  }

  def customerAllowancesFor(benefitType: BenefitType): Set[CustomerAddedStateBenefit] = benefitType match {
    case BenefitType.StatePension => ???
    case BenefitType.StatePensionLumpSum => ???
    case BenefitType.EmploymentSupportAllowance => customerEmploymentSupportAllowances
    case BenefitType.JobSeekersAllowance => customerJobSeekersAllowances
    case BenefitType.OtherStateBenefits => ???
  }

  lazy val hmrcEmploymentSupportAllowances: Set[StateBenefit] = stateBenefits
    .flatMap(_.stateBenefitsData)
    .flatMap(_.employmentSupportAllowances)
    .getOrElse(Set.empty)

  lazy val customerEmploymentSupportAllowances: Set[CustomerAddedStateBenefit] = stateBenefits
    .flatMap(_.customerAddedStateBenefitsData)
    .flatMap(_.employmentSupportAllowances)
    .getOrElse(Set.empty)

  lazy val hmrcJobSeekersAllowances: Set[StateBenefit] = stateBenefits
    .flatMap(_.stateBenefitsData)
    .flatMap(_.jobSeekersAllowances)
    .getOrElse(Set.empty)

  lazy val customerJobSeekersAllowances: Set[CustomerAddedStateBenefit] = stateBenefits
    .flatMap(_.customerAddedStateBenefitsData)
    .flatMap(_.jobSeekersAllowances)
    .getOrElse(Set.empty)
}

object IncomeTaxUserData {
  implicit val formats: OFormat[IncomeTaxUserData] = Json.format[IncomeTaxUserData]
}
