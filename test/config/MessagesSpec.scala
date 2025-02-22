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

package config

import play.api.i18n.MessagesApi
import support.ViewUnitTest

class MessagesSpec extends ViewUnitTest {

  private val exclusionKeys: Set[String] = Set(
    "global.error.fallbackClientError4xx.heading",
    "global.error.fallbackClientError4xx.message",
    "internal-server-error-template.heading",
    "back.text",
    "global.error.pageNotFound404.message",
    "internal-server-error-template.paragraph.1",
    "radios.yesnoitems.no",
    "phase.banner.before",
    "betaBar.banner.message.3",
    "radios.yesnoitems.yes",
    "global.error.badRequest400.message",
    "phase.banner.after",
    "betaBar.banner.message.2",
    "common.yes",
    "common.back",
    "phase.banner.link",
    "betaBar.banner.message.1",
    "common.no",
    "global.error.fallbackClientError4xx.title",
    "language.day.plural",
    "language.day.singular",
    "error.summary.title",
    "error.agent.title",
    "employmentSupportAllowance.reviewClaimPage.endDateQuestion.hiddenText.agent",
    "employmentSupportAllowance.reviewClaimPage.endDateQuestion.hiddenText.individual",
    "employmentSupportAllowance.reviewClaimPage.endDate.hiddenText.agent",
    "employmentSupportAllowance.reviewClaimPage.endDate.hiddenText.individual",
    "employmentSupportAllowance.reviewClaimPage.amount.hiddenText.agent",
    "employmentSupportAllowance.reviewClaimPage.amount.hiddenText.individual",
    "employmentSupportAllowance.reviewClaimPage.taxPaidQuestion.hiddenText.agent",
    "employmentSupportAllowance.reviewClaimPage.taxPaidQuestion.hiddenText.individual",
    "employmentSupportAllowance.reviewClaimPage.taxPaid.hiddenText.agent",
    "employmentSupportAllowance.reviewClaimPage.taxPaid.hiddenText.individual",
    "employmentSupportAllowance.reviewClaimPage.removeClaim.hiddenText",
    "jobSeekersAllowance.reviewClaimPage.endDateQuestion.hiddenText.agent",
    "jobSeekersAllowance.reviewClaimPage.endDateQuestion.hiddenText.individual",
    "jobSeekersAllowance.reviewClaimPage.endDate.hiddenText.agent",
    "jobSeekersAllowance.reviewClaimPage.endDate.hiddenText.individual",
    "jobSeekersAllowance.reviewClaimPage.amount.hiddenText.agent",
    "jobSeekersAllowance.reviewClaimPage.amount.hiddenText.individual",
    "jobSeekersAllowance.reviewClaimPage.taxPaidQuestion.hiddenText.agent",
    "jobSeekersAllowance.reviewClaimPage.taxPaidQuestion.hiddenText.individual",
    "jobSeekersAllowance.reviewClaimPage.taxPaid.hiddenText.agent",
    "jobSeekersAllowance.reviewClaimPage.taxPaid.hiddenText.individual",
    "jobSeekersAllowance.reviewClaimPage.removeClaim.hiddenText",
    "common.claimsPage.addMissingClaim.individual",
    "common.claimsPage.addMissingClaim.agent",
    "common.claimsPage.addAnotherClaim.individual",
    "common.claimsPage.addAnotherClaim.agent",
    "common.claimsPage.error.anotherClaim.individual",
    "common.claimsPage.error.anotherClaim.agent",
    "sectionCompletedState.title",
    "sectionCompletedState.hint",
    "sectionCompletedState.error.required"
  )

  private lazy val allLanguages: Map[String, Map[String, String]] = app.injector.instanceOf[MessagesApi].messages

  private val defaults = allLanguages("default")
  private val welsh = allLanguages("cy")

  "the messages file must have welsh translations" should {
    "check all keys in the default file other than those in the exclusion list has a corresponding translation" in {
      defaults.keys.foreach(
        key =>
          if (!exclusionKeys.contains(key)) {
            welsh.keys should contain(key)
          }
      )
    }
  }

  "the english messages file" should {
    "have no duplicate messages(values)" in {
      val messages: List[(String, String)] = defaults.filter(entry => !exclusionKeys.contains(entry._1)).toList

      val result = checkMessagesAreUnique(messages, messages, Set())

      result shouldBe Set()
    }
  }

  "the welsh messages file" should {
    "have no duplicate messages(values)" in {
      val messages: List[(String, String)] = welsh.filter(entry => !exclusionKeys.contains(entry._1)).toList

      val result = checkMessagesAreUnique(messages, messages, Set())

      result shouldBe Set()
    }
  }
  override protected val userScenarios: Seq[UserScenario[_, _]] = Seq.empty
}
