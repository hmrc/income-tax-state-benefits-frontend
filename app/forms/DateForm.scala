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

import filters.InputFilters
import forms.validation.mappings.MappingUtil.dateMapping
import play.api.data.Form

object DateForm extends InputFilters {

  val formValuesPrefix = "value-for"

  val day: String = s"$formValuesPrefix-day"
  val month: String = s"$formValuesPrefix-month"
  val year: String = s"$formValuesPrefix-year"

  def dateForm(emptyDayKey: String,
               emptyMonthKey: String,
               emptyYearKey: String,
               invalidDateKey: String,
               tooLongAgoKey: Option[String] = None): Form[DateFormData] = Form(
    dateMapping(emptyDayKey, emptyMonthKey, emptyYearKey, invalidDateKey, tooLongAgoKey)
  )
}
