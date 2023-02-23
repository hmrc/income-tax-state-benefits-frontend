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

package views.templates.helpers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import support.ViewUnitTest
import views.html.templates.helpers.Heading

class HeadingSpec extends ViewUnitTest {

  override protected val userScenarios: Seq[UserScenario[_, _]] = Seq.empty

  private val underTest = inject[Heading]

  "Heading template" should {
    implicit val messages: Messages = getMessages(false)
    "render correct h1 (heading) element" in {
      val extraClass = "extra-class"
      val document: Document = Jsoup.parse(underTest("heading", extraClasses = extraClass, size = "s").body)

      val headingAndCaption = document.select(s"h1.govuk-heading-s.$extraClass")

      headingAndCaption.hasClass(extraClass) shouldBe true
    }
  }
}
