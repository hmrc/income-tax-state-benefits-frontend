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

sealed abstract class BenefitType(val typeName: String, val storedBenefitTypeName: String  )

object BenefitType {
//  case object StatePension extends BenefitType(typeName = "statePension")
//  case object StatePensionLumpSum extends BenefitType(typeName = "statePensionLumpSum")
  case object EmploymentSupportAllowance extends BenefitType(typeName = "employmentSupportAllowance", storedBenefitTypeName = "employment-support-allowance")
  case object JobSeekersAllowance extends BenefitType(typeName = "jobSeekersAllowance", storedBenefitTypeName = "job-seekers-allowance")
//  case object OtherStateBenefits extends BenefitType(typeName = "otherStateBenefits")
}
