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

import play.api.mvc.Call
import support.UnitTest
import support.providers.MessagesProvider
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class ViewUtilsSpec extends UnitTest
  with MessagesProvider {

  ".toYesOrNo" should {
    "return translation of common.yes when given value is true" in {
      ViewUtils.toYesOrNo(value = true) shouldBe messages("common.yes")
    }

    "return translation of common.no when given value is false" in {
      ViewUtils.toYesOrNo(value = false) shouldBe messages("common.no")
    }
  }

  ".bigDecimalCurrency" should {
    "Place comma in appropriate place when given amount over 999" in {
      ViewUtils.bigDecimalCurrency("45000.10") shouldBe "£45,000.10"
    }
  }

  ".translatedDateFormatter" should {
    "translate date" in {
      val date = LocalDate.parse("2002-01-01")
      ViewUtils.translatedDateFormatter(date) shouldBe
        date.getDayOfMonth.toString + " " + messages("common." + date.getMonth.toString.toLowerCase) + " " + date.getYear.toString
    }
  }

  "ariaHiddenChangeLink" should {
    "make link hidden in" in {
      ViewUtils.ariaHiddenChangeLink("Example") shouldBe HtmlContent(s"""<span aria-hidden="true">Example</span>""")
    }
  }

  "toSummaryListRow" should {
    "make a summary list row from specified data" in {
      ViewUtils.toSummaryListRow(HtmlContent("exampleKey"), HtmlContent("exampleValue"), keyClasses = "exampleKeyClasses", actionClasses = "exampleActionClasses",
        valueClasses = "exampleValueClasses", actions = Some(Seq((Call("GET", "exampleActionUrl"), "exampleActionText", Some("exampleActionHiddenText"))))) shouldBe
        SummaryListRow(
          key = Key(content = HtmlContent("exampleKey"), classes = "exampleKeyClasses"),
          value = Value(content = HtmlContent("exampleValue"), classes = "exampleValueClasses"),
          actions = Some(Actions(items = Seq(ActionItem(
            href = "exampleActionUrl",
            content = HtmlContent("<span aria-hidden=\"true\">exampleActionText</span>"),
            Some("exampleActionHiddenText"),
            classes = "")),
            classes = "exampleActionClasses"
          ))
        )
    }

    "make a summary list row from minimum data" in {
      ViewUtils.toSummaryListRow(HtmlContent("exampleKey"), HtmlContent("exampleValue")) shouldBe
        SummaryListRow(
          key = Key(content = HtmlContent("exampleKey"), classes = "govuk-!-width-one-third"),
          value = Value(content = HtmlContent("exampleValue"), classes = "govuk-!-width-one-third"),
          actions = None
        )
    }
  }
}
