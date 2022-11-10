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

package support.builders.pages.jobseekers

import forms.DateForm.dateForm
import forms.FormsProvider
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import models.pages.jobseekers.{DidClaimEndInTaxYearPage, TaxTakenOffPage}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.i18n.Messages.implicitMessagesProviderToMessages
import support.utils.TaxYearUtils.taxYearEOY
import support.utils.TaxYearUtils.taxYear
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel

import java.time.LocalDate
import java.util.UUID

object TaxTakenOffPageBuilder {
  val aTaxTakenOffPage: TaxTakenOffPage = TaxTakenOffPage(
    taxYear = taxYearEOY,
    titleFirstDate = LocalDate.parse(s"$taxYearEOY-04-23"),
    titleSecondDate = aClaimCYAModel.endDate.get,
    sessionDataId = UUID.randomUUID(),
    // Todo
    form = new FormsProvider().taxTakenOffForm(isAgent = true, taxYear, aStateBenefitsUserData){
      ???
    }
  )

}