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

import forms.YesNoForm
import models.BenefitType.JobSeekersAllowance
import models.pages.EndDateQuestionPage
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.utils.TaxYearUtils.taxYearEOY

import java.util.UUID

object EndDateQuestionPageBuilder {

  val aEndDateQuestionPage: EndDateQuestionPage = EndDateQuestionPage(
    taxYear = taxYearEOY,
    benefitType = JobSeekersAllowance,
    titleFirstDate = aStateBenefitsUserData.claim.get.startDate,
    sessionDataId = UUID.randomUUID(),
    form = YesNoForm.yesNoForm("default.error", Seq.empty)
  )
}
