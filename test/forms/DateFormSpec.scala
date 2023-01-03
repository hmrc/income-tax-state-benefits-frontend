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

import forms.DateForm.{day, month, year}
import play.api.data.FormError
import support.UnitTest

class DateFormSpec extends UnitTest {

  private val dateForm = DateForm.dateForm(
    emptyDayKey = "empty.day",
    emptyMonthKey = "empty.month",
    emptyYearKey = "empty.year",
    invalidDateKey = "invalid.date",
    tooLongAgoKey = Some("too.long.ago")
  )

  "DateForm" should {
    "return form with no errors when data is correct" in {
      dateForm.bind(Map(day -> "1", month -> "1", year -> "2022")).errors shouldBe empty
    }

    "return empty field error" when {
      "day field is empty" in {
        dateForm.bind(Map(day -> "", month -> "1", year -> "2022")).errors shouldBe Seq(FormError(day, Seq("empty.day")))
      }

      "month field is empty" in {
        dateForm.bind(Map(day -> "1", month -> "", year -> "2022")).errors shouldBe Seq(FormError(month, Seq("empty.month")))
      }

      "year field is empty" in {
        dateForm.bind(Map(day -> "1", month -> "1", year -> "")).errors shouldBe Seq(FormError(year, Seq("empty.year")))
      }
    }

    "return multiple empty field errors" when {
      "multiple fields are empty" in {
        dateForm.bind(Map(day -> "", month -> "", year -> "")).errors shouldBe Seq(
          FormError(day, Seq("empty.day")),
          FormError(month, Seq("empty.month")),
          FormError(year, Seq("empty.year"))
        )
      }
    }

    "return error when form data is not a date" when {
      "day is not a number" in {
        dateForm.bind(Map(day -> "xxx", month -> "1", year -> "2022")).errors shouldBe Seq(FormError("", "invalid.date"))
      }

      "month is not a number" in {
        dateForm.bind(Map(day -> "1", month -> "xxx", year -> "2022")).errors shouldBe Seq(FormError("", "invalid.date"))
      }

      "year is not a number" in {
        dateForm.bind(Map(day -> "1", month -> "1", year -> "xxxx")).errors shouldBe Seq(FormError("", "invalid.date"))
      }

      "impossible date" in {
        dateForm.bind(Map(day -> "30", month -> "2", year -> "2022")).errors shouldBe Seq(FormError("", "invalid.date"))
      }
    }

    "return error when form date is earlier than 1900-01-01" in {
      dateForm.bind(Map(day -> "1", month -> "1", year -> "1900")).errors shouldBe Seq(FormError("", "too.long.ago"))
    }
  }
}
