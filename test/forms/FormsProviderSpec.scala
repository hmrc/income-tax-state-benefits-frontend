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

package forms

import models.BenefitType.{EmploymentSupportAllowance, JobSeekersAllowance}
import play.api.data.FormError
import support.UnitTest
import support.providers.TaxYearProvider

// TODO: This test should be deleted when all scenarios are tested in the Template tests
class FormsProviderSpec extends UnitTest
  with TaxYearProvider {

  private val minAmount = BigDecimal(-100)
  private val maxAmount = BigDecimal(100_000)
  private val validAmount = maxAmount - 1
  private val correctAmountData = Map(AmountForm.amount -> validAmount.toString)
  private val spacesAmountData = Map(AmountForm.amount -> "1 1 1")
  private val overMaximumAmount: Map[String, String] = Map(AmountForm.amount -> "100,000,000,000")
  private val belowMinimumAmount: Map[String, String] = Map(AmountForm.amount -> (minAmount - 1).toString)
  private val atMinimumAmount: Map[String, String] = Map(AmountForm.amount -> minAmount.toString)
  private val wrongKeyData = Map("wrongKey" -> validAmount.toString)
  private val wrongAmountFormat: Map[String, String] = Map(AmountForm.amount -> "123.45.6")
  private val incorrectCharacters: Map[String, String] = Map(AmountForm.amount -> "?")
  private val emptyData: Map[String, String] = Map.empty

  private val underTest = new FormsProvider()

  ".taxPaidAmountForm" should {
    "return a form that maps data when data is correct" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(correctAmountData).errors shouldBe Seq.empty
    }

    "return a form that maps data when data has spaces" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(spacesAmountData).errors shouldBe Seq.empty
    }

    "return a form that contains error when wrong key" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(wrongKeyData).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.taxPaidPage.empty.amount.error.individual"), Seq())
      )
    }

    "return form with Jobseeker's Allowance specific error when data is empty" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(emptyData).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.taxPaidPage.empty.amount.error.individual"), Seq())
      )
    }

    "return form with Employment Support Allowance specific error when data is empty" in {
      underTest.taxPaidAmountForm(EmploymentSupportAllowance, isAgent = false, maxAmount = maxAmount).bind(emptyData).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("employmentSupportAllowance.taxPaidPage.empty.amount.error.individual"), Seq())
      )
    }

    "return form with error when data is in wrong format" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(wrongAmountFormat).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("common.taxPaidPage.wrongFormat.amount.error"), Seq())
      )
    }

    "return form with error when data has invalid characters" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(incorrectCharacters).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("common.taxPaidPage.wrongFormat.amount.error"), Seq())
      )
    }

    "return form with error when amount is over maximum" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(overMaximumAmount).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("common.taxPaidPage.exceedsMax.amount.error"), Seq(maxAmount))
      )
    }

    "return form with error when amount is under zero" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(belowMinimumAmount).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("common.taxPaidPage.zeroOrLess.amount.error"), Seq(0))
      )
    }

    "return form with error when amount is equal to zero" in {
      underTest.taxPaidAmountForm(JobSeekersAllowance, isAgent = false, maxAmount = maxAmount).bind(atMinimumAmount).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("common.taxPaidPage.zeroOrLess.amount.error"), Seq(0))
      )
    }
  }
}
