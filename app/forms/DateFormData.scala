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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import scala.util.Try

case class DateFormData(day: String,
                        month: String,
                        year: String) {

  lazy val toLocalDate: Option[LocalDate] = Try(LocalDate.of(
    year.filterNot(_.isWhitespace).toInt,
    month.filterNot(_.isWhitespace).toInt,
    day.filterNot(_.isWhitespace).toInt)
  ).toOption

  lazy val isValidLocalDate: Boolean = toLocalDate.isDefined
}

object DateFormData {
  implicit val format: OFormat[DateFormData] = Json.format[DateFormData]

  def apply(localDate: LocalDate): DateFormData = DateFormData(
    day = localDate.getDayOfMonth.toString,
    month = localDate.getMonthValue.toString,
    year = localDate.getYear.toString
  )
}




