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

import forms.jobseekers.FormsProvider
import forms.{DateForm, DateFormData}
import support.ControllerUnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.providers.{MessagesProvider, TaxYearProvider}

class StartDatePageSpec extends ControllerUnitTest
  with TaxYearProvider
  with MessagesProvider {

  private val pageForm = new FormsProvider().startDateForm(taxYear, isAgent = false)

  "StartDatePage.apply(...)" should {
    "return page with pre-filled form when start date is preset" in {
      StartDatePage.apply(taxYear, aStateBenefitsUserData, pageForm) shouldBe StartDatePage(
        taxYear = taxYear,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = pageForm.fill(value = DateFormData(aStateBenefitsUserData.claim.get.startDate))
      )
    }

    "return page without pre-filled form when start date is not preset" in {
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = None)

      StartDatePage.apply(taxYear, stateBenefitsUserData, pageForm) shouldBe StartDatePage(
        taxYear = taxYear,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }

    "return page with pre-filled form with errors when form has errors" in {
      val formWithErrors = pageForm.bind(Map(DateForm.day -> "6", DateForm.month -> "4", DateForm.year -> taxYear.toString))

      StartDatePage.apply(taxYear, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = pageForm.bind(Map(DateForm.day -> "6", DateForm.month -> "4", DateForm.year -> taxYear.toString))
      )
    }
  }
}
