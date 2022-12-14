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

import models.BenefitType.JobSeekersAllowance
import play.api.data.FormError
import support.UnitTest
import support.providers.TaxYearProvider

class FormsProviderSpec extends UnitTest
  with TaxYearProvider {

  private val anyBoolean = true
  private val amount: String = 123.0.toString
  private val correctBooleanData = Map(YesNoForm.yesNo -> anyBoolean.toString)
  private val correctAmountData = Map(AmountForm.amount -> amount)
  private val spacesAmountData = Map(AmountForm.amount -> "12 11 1")
  private val overMaximumAmount: Map[String, String] = Map(AmountForm.amount -> "100,000,000,000")
  private val underZeroAmount: Map[String, String] = Map(AmountForm.amount -> "-100")
  private val zeroAmount: Map[String, String] = Map(AmountForm.amount -> "-100")
  private val wrongKeyData = Map("wrongKey" -> amount)
  private val wrongAmountFormat: Map[String, String] = Map(AmountForm.amount -> "123.45.6")
  private val incorrectCharacters: Map[String, String] = Map(AmountForm.amount -> "?")
  private val emptyData: Map[String, String] = Map.empty

  private val underTest = new FormsProvider()

  ".endDateYesNoForm" should {
    "return a form that maps data when data is correct" in {
      underTest.endDateYesNoForm(taxYear = taxYear).bind(correctBooleanData).errors shouldBe Seq.empty
    }

    "return a form that contains error when data is incorrect" in {
      underTest.endDateYesNoForm(taxYear = taxYear).bind(wrongKeyData).errors shouldBe Seq(
        FormError("value", Seq("common.endDateQuestionPage.error"), Seq(taxYear.toString))
      )
    }
  }

  ".jsaAmountForm" should {
    "return a form that maps data when data is correct" in {
      underTest.amountForm(JobSeekersAllowance).bind(correctAmountData).errors shouldBe Seq.empty
    }

    "return a form that maps data when data has spaces" in {
      underTest.amountForm(JobSeekersAllowance).bind(spacesAmountData).errors shouldBe Seq.empty
    }

    "return a form that contains error when wrong key" in {
      underTest.amountForm(JobSeekersAllowance).bind(wrongKeyData).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.amountPage.empty.amount.error"), Seq())
      )
    }

    "return form with error when data is empty" in {
      underTest.amountForm(JobSeekersAllowance).bind(emptyData).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.amountPage.empty.amount.error"), Seq())
      )
    }

    "return form with error when data is wrongFormat" in {
      underTest.amountForm(JobSeekersAllowance).bind(wrongAmountFormat).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.amountPage.wrongFormat.amount.error"), Seq())
      )
    }

    "return form with error when data has invalid characters" in {
      underTest.amountForm(JobSeekersAllowance).bind(incorrectCharacters).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.amountPage.wrongFormat.amount.error"), Seq())
      )
    }

    "return form with error when data is overMaximum" in {
      underTest.amountForm(JobSeekersAllowance).bind(overMaximumAmount).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.amountPage.exceedsMax.amount.error"), Seq())
      )
    }

    "return form with error when data is under zero" in {
      underTest.amountForm(JobSeekersAllowance).bind(underZeroAmount).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.amountPage.lessThanZero.amount.error"), Seq())
      )
    }

    "return form with error when data is equal to zero" in {
      underTest.amountForm(JobSeekersAllowance).bind(zeroAmount).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobSeekersAllowance.amountPage.lessThanZero.amount.error"), Seq())
      )
    }
  }
}
