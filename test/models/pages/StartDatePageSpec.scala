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

class StartDatePageSpec extends ControllerUnitTest
  with TaxYearProvider
  with MessagesProvider {

  private val pageForm = DateForm.dateForm()

  "StartDatePage.apply(...)" should {
    "return page with pre-filled form when start date is preset" in {
      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, pageForm) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = pageForm.fill(value = DateFormData(aStateBenefitsUserData.claim.get.startDate))
      )
    }

    "return page without pre-filled form when start date is not preset" in {
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = None)

      StartDatePage.apply(taxYear, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm
      )
    }

    "return page with pre-filled form with errors when form has errors" in {
      val formData = DateFormData(day = "6", month = "4", year = taxYear.toString)
      val form = pageForm.bind(Map(DateForm.day -> "6", DateForm.month -> "4", DateForm.year -> taxYear.toString))
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }

    "return page with error when empty day, month and year" in {

      val dayInput = ""
      val monthInput = ""
      val yearInput = ""
      val form = pageForm.bind(Map(DateForm.day -> dayInput, DateForm.month -> monthInput, DateForm.year -> yearInput))
      val formData = DateFormData(day = dayInput, month = monthInput, year = yearInput)
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))
      formWithErrors.errors.map { error =>
        error.key shouldBe ("emptyAll")
      }

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }

    "return page with error when empty day and month" in {

      val dayInput = ""
      val monthInput = ""
      val yearInput = taxYear.toString
      val form = pageForm.bind(Map(DateForm.day -> dayInput, DateForm.month -> monthInput, DateForm.year -> yearInput))
      val formData = DateFormData(day = dayInput, month = monthInput, year = yearInput)
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))
      formWithErrors.errors.map { error =>
        error.key shouldBe("emptyDayMonth")
      }

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }

    "return page with error when empty day and year" in {

      val dayInput = ""
      val monthInput = "5"
      val yearInput = ""
      val form = pageForm.bind(Map(DateForm.day -> dayInput, DateForm.month -> monthInput, DateForm.year -> yearInput))
      val formData = DateFormData(day = dayInput, month = monthInput, year = yearInput)
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))
      formWithErrors.errors.map { error =>
        error.key shouldBe ("emptyDayYear")
      }

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }
    "return page with error when empty month and year" in {

      val dayInput = "2"
      val monthInput = ""
      val yearInput = ""
      val form = pageForm.bind(Map(DateForm.day -> dayInput, DateForm.month -> monthInput, DateForm.year -> yearInput))
      val formData = DateFormData(day = dayInput, month = monthInput, year = yearInput)
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))
      formWithErrors.errors.map { error =>
        error.key shouldBe ("emptyMonthYear")
      }

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }

    "return page with error when empty year" in {

      val dayInput = "2"
      val monthInput = "3"
      val yearInput = ""
      val form = pageForm.bind(Map(DateForm.day -> dayInput, DateForm.month -> monthInput, DateForm.year -> yearInput))
      val formData = DateFormData(day = dayInput, month = monthInput, year = yearInput)
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))
      formWithErrors.errors.map { error =>
        error.key shouldBe ("emptyYear")
      }

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }
    "return page with error when empty month" in {

      val dayInput = "2"
      val monthInput = ""
      val yearInput = taxYear.toString
      val form = pageForm.bind(Map(DateForm.day -> dayInput, DateForm.month -> monthInput, DateForm.year -> yearInput))
      val formData = DateFormData(day = dayInput, month = monthInput, year = yearInput)
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))
      formWithErrors.errors.map { error =>
        error.key shouldBe ("emptyMonth")
      }

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }
    "return page with error when empty day" in {

      val dayInput = ""
      val monthInput = "4"
      val yearInput = taxYear.toString
      val form = pageForm.bind(Map(DateForm.day -> dayInput, DateForm.month -> monthInput, DateForm.year -> yearInput))
      val formData = DateFormData(day = dayInput, month = monthInput, year = yearInput)
      val formWithErrors = form.copy(errors = DateForm.validateStartDate(formData, taxYear, JobSeekersAllowance, isAgent = true, None))
      formWithErrors.errors.map { error =>
        error.key shouldBe ("emptyDay")
      }

      StartDatePage.apply(taxYear, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe StartDatePage(
        taxYear = taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }


  }
}
