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

import forms.YesNoForm
import models.BenefitType.JobSeekersAllowance
import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.providers.TaxYearProvider

class EndDateQuestionPageSpec extends UnitTest
  with TaxYearProvider {

  private val anyQuestionValue = true

  private val pageForm = YesNoForm.yesNoForm("", Seq.empty)

  "EndDateQuestionPage.apply" should {
    "return page with pre-filled form when endDateQuestion has value" in {
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(endDateQuestion = Some(anyQuestionValue))))

      EndDateQuestionPage.apply(taxYearEOY, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe EndDateQuestionPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = aStateBenefitsUserData.claim.get.startDate,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm.fill(value = anyQuestionValue)
      )
    }

    "return page without pre-filled form when endDateQuestion is None" in {
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(endDateQuestion = None)))

      EndDateQuestionPage.apply(taxYearEOY, JobSeekersAllowance, stateBenefitsUserData, pageForm) shouldBe EndDateQuestionPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = aStateBenefitsUserData.claim.get.startDate,
        sessionDataId = stateBenefitsUserData.sessionDataId.get,
        form = pageForm,
      )
    }

    "return page with pre-filled form with errors when form has errors" in {
      val formWithErrors = pageForm.bind(Map("wrong-key" -> "wrong-value"))

      EndDateQuestionPage.apply(taxYearEOY, JobSeekersAllowance, aStateBenefitsUserData, formWithErrors) shouldBe EndDateQuestionPage(
        taxYear = taxYearEOY,
        benefitType = JobSeekersAllowance,
        titleFirstDate = aStateBenefitsUserData.claim.get.startDate,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        form = formWithErrors
      )
    }
  }
}
