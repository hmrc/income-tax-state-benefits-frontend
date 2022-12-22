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

package models.pages

import forms.DateFormData
import models.{BenefitType, StateBenefitsUserData}
import play.api.data.Form

import java.time.LocalDate
import java.util.UUID

case class EndDatePage(taxYear: Int,
                       benefitType: BenefitType,
                       sessionDataId: UUID,
                       form: Form[DateFormData])

object EndDatePage {

  def apply(taxYear: Int,
            benefitType: BenefitType,
            stateBenefitsUserData: StateBenefitsUserData,
            form: Form[DateFormData]): EndDatePage = {
    val optEndDate: Option[LocalDate] = stateBenefitsUserData.claim.flatMap(_.endDate)

    EndDatePage(
      taxYear = taxYear,
      benefitType = benefitType,
      stateBenefitsUserData.sessionDataId.get,
      form = optEndDate.fold(form)(localDate => if (form.hasErrors) form else form.fill(DateFormData(localDate)))
    )
  }
}
