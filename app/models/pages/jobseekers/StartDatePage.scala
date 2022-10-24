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

import forms.DateFormData
import models.StateBenefitsUserData
import play.api.data.Form

import java.time.LocalDate
import java.util.UUID

case class StartDatePage(taxYear: Int,
                         sessionDataId: UUID,
                         form: Form[DateFormData])

object StartDatePage {

  def apply(taxYear: Int,
            stateBenefitsUserData: StateBenefitsUserData,
            form: Form[DateFormData]): StartDatePage = {
    val optStartDate: Option[LocalDate] = stateBenefitsUserData.claim.map(_.startDate)

    StartDatePage(
      taxYear = taxYear,
      stateBenefitsUserData.id.get,
      form = optStartDate.fold(form)(localDate => if (form.hasErrors) form else form.fill(DateFormData(localDate)))
    )
  }
}
