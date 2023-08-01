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
import support.UnitTest

class DateFormSpec extends UnitTest {

  ".dateForm" should {
    "return a form that can bind day, month, year to DateFormData" in {
      DateForm.dateForm().bind(Map(day -> "1", month -> "2", year -> "2022")).get shouldBe
        DateFormData(day = "1", month = "2", year = "2022")
    }
    "return a form that can bind day, month, year to DateFormData even with spaces" in {
      DateForm.dateForm().bind(Map(day -> "0 8", month -> "1 2", year -> "20 22")).get shouldBe
        DateFormData(day = "0 8", month = "1 2", year = "20 22")
    }
  }
}
