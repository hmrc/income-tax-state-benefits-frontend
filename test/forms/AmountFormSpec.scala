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

import forms.AmountForm._
import play.api.data.{Form, FormError}
import support.UnitTest

class AmountFormSpec extends UnitTest {

  private def theForm(): Form[BigDecimal] = {
    amountForm("nothing to see here", "too small", -101, "too big", wrongFormatKey = "this not good")
  }

  private val testCurrencyValid = 1000
  private val testCurrencyWithSpaces = "100 0. 00"
  private val testCurrencyEmpty = ""
  private val testCurrencyInvalidInt = "!"
  private val testCurrencyInvalidFormat = 12345.123
  private val testCurrencyTooBig = "100000000000.00"
  private val testCurrencyZeroAmount = 0
  private val testCurrencyNegativeAmount = -100

  "The AmountForm" should {
    "correctly validate a currency" when {
      "a valid currency is entered" in {
        val testInput = Map(amount -> testCurrencyValid.toString)
        val expected = testCurrencyValid
        val actual = theForm().bind(testInput).value
        actual shouldBe Some(expected)
      }

      "zero is entered" in {
        val testInput = Map(amount -> testCurrencyZeroAmount.toString)
        val expected = testCurrencyZeroAmount
        val actual = theForm().bind(testInput).value
        actual shouldBe Some(expected)
      }

      "a negative number is entered" in {
        val testInput = Map(amount -> testCurrencyNegativeAmount.toString)
        val expected = testCurrencyNegativeAmount
        val actual = theForm().bind(testInput).value
        actual shouldBe Some(expected)
      }
    }

    "correctly validate a currency with spaces" when {
      "a valid currency is entered" in {
        val testInput = Map(amount -> testCurrencyWithSpaces)
        val expected = testCurrencyValid
        val actual = theForm().bind(testInput).value
        actual shouldBe Some(expected)
      }
    }

    "invalidate an empty currency" in {
      val testInput = Map(amount -> testCurrencyEmpty)
      val emptyTest = theForm().bind(testInput)
      emptyTest.errors should contain(FormError(amount, "nothing to see here"))
    }

    "invalidate currency that includes invalid characters" in {
      val testInput = Map(amount -> testCurrencyInvalidInt)
      val invalidCharTest = theForm().bind(testInput)
      invalidCharTest.errors should contain(FormError(amount, "this not good"))
    }

    "invalidate a currency that has incorrect formatting" in {
      val testInput = Map(amount -> testCurrencyInvalidFormat.toString)
      val invalidFormatTest = theForm().bind(testInput)
      invalidFormatTest.errors should contain(FormError(amount, "this not good"))
    }

    "invalidate a currency that is too big" in {
      val testInput = Map(amount -> testCurrencyTooBig)
      val bigCurrencyTest = theForm().bind(testInput)
      bigCurrencyTest.errors should contain(FormError(amount, "too big", Seq(BigDecimal(100_000_000_000d))))
    }
  }
}
