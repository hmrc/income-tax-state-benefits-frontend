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
import models.pages.TaxPaidQuestionPage
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.utils.TaxYearUtils.taxYearEOY

import java.time.LocalDate
import java.util.UUID

object TaxPaidQuestionPageBuilder {

  val aTaxPaidQuestionPage: TaxPaidQuestionPage = TaxPaidQuestionPage(
    taxYear = taxYearEOY,
    benefitType = JobSeekersAllowance,
    titleFirstDate = LocalDate.parse(s"$taxYearEOY-01-01"),
    titleSecondDate = aClaimCYAModel.endDate.get,
    sessionDataId = UUID.randomUUID(),
    hasEndDate = aClaimCYAModel.endDateQuestion.get,
    form = YesNoForm.yesNoForm("some.error.message.key")
  )
}
