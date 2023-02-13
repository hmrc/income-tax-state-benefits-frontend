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

import forms.{DateForm, DateFormData}
import models.BenefitType.JobSeekersAllowance
import support.ControllerUnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.providers.{MessagesProvider, TaxYearProvider}

class EndDatePageSpec extends ControllerUnitTest
  with TaxYearProvider
  with MessagesProvider {

  private val pageForm = DateForm.dateForm()

  "EndDatePage.apply(...)" should {
    "return page with pre-filled form when end date is preset" in {
      EndDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, pageForm) shouldBe EndDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = pageForm.fill(value = DateFormData(aStateBenefitsUserData.claim.get.endDate.get))
      )
    }

    "return page without pre-filled form when end date is not preset" in {
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = None)

      EndDatePage.apply(taxYear, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe EndDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }

    "return page with pre-filled form with errors when form has errors" in {
      val formData = DateFormData(day = "6", month = "4", year = taxYear.toString)
      val form = pageForm.bind(Map(DateForm.day -> "6", DateForm.month -> "4", DateForm.year -> taxYear.toString))
      val formWithErrors = form.copy(errors = DateForm.validateEndDate(formData, taxYear, JobSeekersAllowance, isAgent = true, aStateBenefitsUserData.claim.get.startDate))

      EndDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe EndDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }
  }
}