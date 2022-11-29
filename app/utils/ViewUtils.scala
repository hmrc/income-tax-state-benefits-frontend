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

package utils

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate
import scala.util.Try

object ViewUtils {

  def toYesOrNo(value: Boolean)(implicit messages: Messages): String = {
    if (value) messages("common.yes") else messages("common.no")
  }

  def bigDecimalCurrency(value: String, currencySymbol: String = "£"): String =
    Try(BigDecimal(value))
      .map(amount => currencySymbol + f"$amount%1.2f".replace(".00", ""))
      .getOrElse(value)
      .replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",")

  def translatedDateFormatter(date: LocalDate)(implicit messages: Messages): String = {
    val translatedMonth = messages("common." + date.getMonth.toString.toLowerCase)
    date.getDayOfMonth + " " + translatedMonth + " " + date.getYear
  }

  def toSummaryListRow(key: HtmlContent,
                       value: HtmlContent,
                       keyClasses: String = "govuk-!-width-one-third",
                       valueClasses: String = "govuk-!-width-one-third",
                       actionClasses: String = "govuk-!-width-one-third",
                       actions: Option[Seq[(Call, String, Option[String])]] = None): SummaryListRow = {
    SummaryListRow(
      key = Key(content = key, keyClasses),
      value = Value(content = value, classes = valueClasses),
      actions = actions.map { action =>
        Actions(
          items = action.map {
            case (call, linkText, visuallyHiddenText) => ActionItem(
              href = call.url,
              content = ariaHiddenChangeLink(linkText),
              visuallyHiddenText = visuallyHiddenText)
          },
          classes = if (actions.isEmpty) "" else actionClasses
        )
      })
  }

  def ariaHiddenChangeLink(linkText: String): HtmlContent = {
    HtmlContent(s"""<span aria-hidden="true">$linkText</span>""")
  }
}
