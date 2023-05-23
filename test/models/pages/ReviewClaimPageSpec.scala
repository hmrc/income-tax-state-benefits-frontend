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

import models.BenefitType.JobSeekersAllowance
import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.providers.TaxYearProvider

import java.time.LocalDate

class ReviewClaimPageSpec extends UnitTest
  with TaxYearProvider {

  private val isInYear = true

  ".apply" should {
    "create correct page object" in {
      ReviewClaimPage.apply(aStateBenefitsUserData.taxYear, JobSeekersAllowance, isInYear, aStateBenefitsUserData, Some(aStateBenefit)) shouldBe ReviewClaimPage(
        taxYear = aStateBenefitsUserData.taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        isInYear = isInYear,
        isHmrcData = aStateBenefitsUserData.isHmrcData,
        isIgnored = aClaimCYAModel.dateIgnored.isDefined,
        itemsFirstDate = aClaimCYAModel.startDate,
        itemsSecondDate = aClaimCYAModel.endDate.get,
        startDate = aClaimCYAModel.startDate,
        endDateQuestion = aClaimCYAModel.endDateQuestion,
        endDate = aClaimCYAModel.endDate,
        amount = aClaimCYAModel.amount,
        taxPaidQuestion = aClaimCYAModel.taxPaidQuestion,
        taxPaid = aClaimCYAModel.taxPaid,
        priorStartDate = Some(aStateBenefit.startDate),
        priorEndDate = aStateBenefit.endDate,
        priorAmount = aStateBenefit.amount,
        priorTaxPaid = aStateBenefit.taxPaid
      )
    }

    "return page with itemsFirstDate equal to startDate when after start of financial year" in {
      val claim = aClaimCYAModel.copy(startDate = LocalDate.of(taxYearEOY - 1, 4, 6))
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claim))

      ReviewClaimPage.apply(aStateBenefitsUserData.taxYear, JobSeekersAllowance, isInYear, stateBenefitsUserData) shouldBe ReviewClaimPage(
        taxYear = aStateBenefitsUserData.taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        isInYear = isInYear,
        isHmrcData = aStateBenefitsUserData.isHmrcData,
        isIgnored = aClaimCYAModel.dateIgnored.isDefined,
        itemsFirstDate = claim.startDate,
        itemsSecondDate = claim.endDate.get,
        startDate = claim.startDate,
        endDateQuestion = claim.endDateQuestion,
        endDate = claim.endDate,
        amount = claim.amount,
        taxPaidQuestion = claim.taxPaidQuestion,
        taxPaid = claim.taxPaid,
        priorStartDate = None,
        priorEndDate = None,
        priorAmount = None,
        priorTaxPaid = None
      )
    }

    "return page with itemsFirstDate equal to start of financial year when startDate is before that" in {
      val claim = aClaimCYAModel.copy(startDate = LocalDate.of(taxYearEOY - 1, 4, 5))
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claim))

      ReviewClaimPage.apply(aStateBenefitsUserData.taxYear, JobSeekersAllowance, isInYear, stateBenefitsUserData) shouldBe ReviewClaimPage(
        taxYear = aStateBenefitsUserData.taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        isInYear = isInYear,
        isHmrcData = aStateBenefitsUserData.isHmrcData,
        isIgnored = aClaimCYAModel.dateIgnored.isDefined,
        itemsFirstDate = LocalDate.of(taxYearEOY - 1, 4, 6),
        itemsSecondDate = claim.endDate.get,
        startDate = claim.startDate,
        endDateQuestion = claim.endDateQuestion,
        endDate = claim.endDate,
        amount = claim.amount,
        taxPaidQuestion = claim.taxPaidQuestion,
        taxPaid = claim.taxPaid,
        priorStartDate = None,
        priorEndDate = None,
        priorAmount = None,
        priorTaxPaid = None
      )
    }

    "return page with itemsSecondDate equal to endDate when exists" in {
      val claimCYAModel = aClaimCYAModel.copy(endDate = Some(LocalDate.of(taxYearEOY, 1, 1)))
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      ReviewClaimPage.apply(aStateBenefitsUserData.taxYear, JobSeekersAllowance, isInYear, stateBenefitsUserData) shouldBe ReviewClaimPage(
        taxYear = aStateBenefitsUserData.taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        isInYear = isInYear,
        isHmrcData = aStateBenefitsUserData.isHmrcData,
        isIgnored = aClaimCYAModel.dateIgnored.isDefined,
        itemsFirstDate = claimCYAModel.startDate,
        itemsSecondDate = claimCYAModel.endDate.get,
        startDate = claimCYAModel.startDate,
        endDateQuestion = claimCYAModel.endDateQuestion,
        endDate = claimCYAModel.endDate,
        amount = claimCYAModel.amount,
        taxPaidQuestion = claimCYAModel.taxPaidQuestion,
        taxPaid = claimCYAModel.taxPaid,
        priorStartDate = None,
        priorEndDate = None,
        priorAmount = None,
        priorTaxPaid = None
      )
    }

    "return page with itemsSecondDate equal to end of financial year when endDate is missing" in {
      val claimCYAModel = aClaimCYAModel.copy(endDate = None)
      val stateBenefitsUserData = aStateBenefitsUserData.copy(claim = Some(claimCYAModel))

      ReviewClaimPage.apply(aStateBenefitsUserData.taxYear, JobSeekersAllowance, isInYear, stateBenefitsUserData) shouldBe ReviewClaimPage(
        taxYear = aStateBenefitsUserData.taxYear,
        benefitType = JobSeekersAllowance,
        sessionDataId = aStateBenefitsUserData.sessionDataId.get,
        isInYear = isInYear,
        isHmrcData = aStateBenefitsUserData.isHmrcData,
        isIgnored = aClaimCYAModel.dateIgnored.isDefined,
        itemsFirstDate = claimCYAModel.startDate,
        itemsSecondDate = LocalDate.of(taxYearEOY, 4, 5),
        startDate = claimCYAModel.startDate,
        endDateQuestion = claimCYAModel.endDateQuestion,
        endDate = claimCYAModel.endDate,
        amount = claimCYAModel.amount,
        taxPaidQuestion = claimCYAModel.taxPaidQuestion,
        taxPaid = claimCYAModel.taxPaid,
        priorStartDate = None,
        priorEndDate = None,
        priorAmount = None,
        priorTaxPaid = None
      )
    }
  }
}
