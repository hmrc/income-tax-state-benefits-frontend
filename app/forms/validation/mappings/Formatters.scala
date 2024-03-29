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

package forms.validation.mappings

import forms.AmountBoundaries
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch

trait Formatters {

  private[mappings] def stringFormatter(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None => Left(Seq(FormError(key, errorKey)))
        case Some(s) => Right(s.trim)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value.trim)
  }

  private[mappings] def currencyFormatter(requiredKey: String,
                                          boundaries: AmountBoundaries,
                                          invalidNumericKey: String,
                                          args: Seq[String] = Seq.empty[String]): Formatter[BigDecimal] = new Formatter[BigDecimal] {
    private val baseFormatter = stringFormatter(requiredKey)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      betweenLimits(validAmount(baseFormatter
        .bind(key, data)
        .map(_.replace(",", ""))
        .map(_.replace("£", ""))
        .map(_.replaceAll("""\s""", "")), key), key)
    }

    override def unbind(key: String, value: BigDecimal): Map[String, String] = baseFormatter.unbind(key, value.toString)

    private def validAmount(input: Either[Seq[FormError], String], key: String): Either[Seq[FormError], BigDecimal] = {

      val is2dp = """-?\d+|\d*\.\d{1,2}"""
      val validNumeric = """-?[0-9.]*"""

      input.flatMap {
        case s if s.isEmpty => Left(Seq(FormError(key, requiredKey, args)))
        case s if !s.matches(validNumeric) => Left(Seq(FormError(key, invalidNumericKey, args)))
        case s if !s.matches(is2dp) => Left(Seq(FormError(key, invalidNumericKey, args)))
        case s =>
          nonFatalCatch
            .either(BigDecimal(s.replaceAll("£", "")))
            .left.map(_ => Seq(FormError(key, invalidNumericKey, args)))
      }
    }

    private def betweenLimits(input: Either[Seq[FormError], BigDecimal], key: String): Either[Seq[FormError], BigDecimal] = {
      input.flatMap {
        case value if value <= boundaries.exclusiveMin => Left(Seq(FormError(key, boundaries.exclusiveMinMsgKey, Seq(boundaries.exclusiveMin))))
        case value if value >= boundaries.exclusiveMax => Left(Seq(FormError(key, boundaries.exclusiveMaxMsgKey, Seq(boundaries.exclusiveMax))))
        case value => Right(value)
      }
    }
  }
}
