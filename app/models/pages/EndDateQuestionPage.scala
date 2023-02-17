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

package models.pages

import models.{BenefitType, StateBenefitsUserData}
import play.api.data.Form
import utils.InYearUtil.toDateWithinTaxYear

import java.time.LocalDate
import java.util.UUID

case class EndDateQuestionPage(taxYear: Int,
                               benefitType: BenefitType,
                               titleFirstDate: LocalDate,
                               sessionDataId: UUID,
                               form: Form[Boolean])

object EndDateQuestionPage {

  def apply(taxYear: Int,
            benefitType: BenefitType,
            stateBenefitsUserData: StateBenefitsUserData,
            form: Form[Boolean]): EndDateQuestionPage = {
    val optQuestionValue = stateBenefitsUserData.claim.flatMap(_.endDateQuestion)
    val titleFirstDate = toDateWithinTaxYear(taxYear, stateBenefitsUserData.claim.get.startDate)

    EndDateQuestionPage(
      taxYear = taxYear,
      benefitType = benefitType,
      titleFirstDate = titleFirstDate,
      stateBenefitsUserData.sessionDataId.get,
      form = optQuestionValue.fold(form)(questionValue => if (form.hasErrors) form else form.fill(questionValue))
    )
  }
}
