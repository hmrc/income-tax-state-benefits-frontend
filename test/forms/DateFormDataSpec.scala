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

import support.UnitTest
import support.builders.forms.DateFormDataBuilder.aDateFormData

import java.time.LocalDate

class DateFormDataSpec extends UnitTest {

  ".toLocalDate" should {
    "return local date when data correct" in {
      aDateFormData.copy(day = "1", month = "1", year = "2022").toLocalDate shouldBe Some(LocalDate.parse("2022-01-01"))
    }

    "return None when data incorrect" in {
      aDateFormData.copy(day = "xxx", month = "1", year = "2022").toLocalDate shouldBe None
    }
  }

  ".isValidLocalDate" should {
    "return true when data correct" in {
      aDateFormData.copy(day = "1", month = "1", year = "2022").isValidLocalDate shouldBe true
    }

    "return false when data incorrect" in {
      aDateFormData.copy(day = "xxx", month = "1", year = "2022").isValidLocalDate shouldBe false
    }
  }
}
