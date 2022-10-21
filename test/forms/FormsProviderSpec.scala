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

import play.api.data.FormError
import support.UnitTest
import support.providers.TaxYearProvider

class FormsProviderSpec extends UnitTest
  with TaxYearProvider {

  private val anyBoolean = true
  private val amount: String = 123.0.toString
  private val correctBooleanData = Map(YesNoForm.yesNo -> anyBoolean.toString)
  private val correctAmountData = Map(AmountForm.amount -> amount)
  private val overMaximumAmount: Map[String, String] = Map(AmountForm.amount -> "100,000,000,000")
  private val wrongKeyData = Map("wrongKey" -> amount)
  private val wrongAmountFormat: Map[String, String] = Map(AmountForm.amount -> "123.45.6")
  private val emptyData: Map[String, String] = Map.empty

  private val underTest = new FormsProvider()

  ".endDateYesNoForm" should {
    "return a form that maps data when data is correct" in {
      underTest.endDateYesNoForm(taxYear = taxYear).bind(correctBooleanData).errors shouldBe Seq.empty
    }

    "return a form that contains error when data is incorrect" in {
      underTest.endDateYesNoForm(taxYear = taxYear).bind(wrongKeyData).errors shouldBe Seq(
        FormError("value", Seq("jobseekers.didClaimEndInTaxYear.error"), Seq(taxYear.toString))
      )
    }
  }

  ".jsaAmountForm" should {
    "return a form that maps data when data is correct" in {
      underTest.jsaAmountForm().bind(correctAmountData).errors shouldBe Seq.empty
    }

    "return a form that contains error when wrong key" in {
      underTest.jsaAmountForm().bind(wrongKeyData).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobseekers.amountPage.empty.amount.error"), Seq())
      )
    }

    "return form with error when isAgent is true and data is empty" in {
      underTest.jsaAmountForm().bind(emptyData).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("jobseekers.amountPage.empty.amount.error"), Seq())
      )
    }

    "return form with error when data is wrongFormat" in {
      underTest.jsaAmountForm().bind(wrongAmountFormat).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("common.error.invalid_currency_format"), Seq())
      )
    }

    "return form with error when data is overMaximum" in {
      underTest.jsaAmountForm().bind(overMaximumAmount).errors shouldBe Seq(
        FormError(AmountForm.amount, Seq("common.error.amountMaxLimit"), Seq())
      )
    }
  }
}
