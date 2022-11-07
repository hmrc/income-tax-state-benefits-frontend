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

package forms

import models.{ClaimCYAModel, StateBenefitsUserData}
import play.api.data.Form
import play.api.i18n.Messages

import java.time.LocalDate
import javax.inject.Singleton
import utils.InYearUtil.toDateWithinTaxYear
import utils.ViewUtils.translatedDateFormatter


@Singleton
class FormsProvider() {

  def endDateYesNoForm(taxYear: Int): Form[Boolean] = YesNoForm.yesNoForm(
    missingInputError = "jobseekers.didClaimEndInTaxYear.error", Seq(taxYear.toString)
  )

  def jsaAmountForm(): Form[BigDecimal] = AmountForm.amountForm(
    emptyFieldKey = "jobseekers.amountPage.empty.amount.error"
  )
  def taxTakenOffForm(isAgent: Boolean): Form[Boolean] = YesNoForm.yesNoForm(
    s"jobseekers.taxTakenOff.error.${if (isAgent) "agent" else "individual"}")

  def taxTakenOffForm(isAgent: Boolean, taxYear: Int, stateBenefitsUserData: StateBenefitsUserData)(implicit messages: Messages): Form[Boolean] = {
    val claimCYAModel: ClaimCYAModel = stateBenefitsUserData.claim.get
    val titleFirstDate = translatedDateFormatter(toDateWithinTaxYear(taxYear, claimCYAModel.startDate))
    val titleSecondDate = translatedDateFormatter(claimCYAModel.endDate.getOrElse(LocalDate.parse(s"$taxYear-04-05")))
    YesNoForm.yesNoForm(
      s"jobseekers.taxTakenOff.error.${if (isAgent) "agent" else "individual"}", Seq(titleFirstDate, titleSecondDate))
  }
}
