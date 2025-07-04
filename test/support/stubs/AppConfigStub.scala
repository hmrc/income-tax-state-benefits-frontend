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

package support.stubs

import config.{AppConfig, AppConfigImpl}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.RequestHeader
import support.utils.TaxYearUtils.taxYearEOY
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigStub {

  def config(sectionCompletedQuestion: Boolean = false): AppConfig = new AppConfigImpl(mock[ServicesConfig]) {
    override lazy val timeoutDialogCountdown: Int = 120
    override lazy val timeoutDialogTimeout: Int = 900
    override lazy val defaultTaxYear: Int = taxYearEOY
    override lazy val welshLanguageEnabled: Boolean = true
    override lazy val sectionCompletedQuestionEnabled: Boolean = sectionCompletedQuestion

    override lazy val signInUrl: String = "/signIn"
    override lazy val incomeTaxSubmissionIvRedirect: String = "/update-and-submit-income-tax-return/iv-uplift"
    override lazy val viewAndChangeEnterUtrUrl: String = "/report-quarterly/income-and-expenses/view/agents/client-utr"
    override lazy val viewAndChangeViewUrlAgent: String = "/report-quarterly/income-and-expenses/view/agents"
    override lazy val incomeTaxSubmissionBaseUrl: String = "/income-tax-submission-base-url"
    override lazy val signOutUrl: String = "/sign-out-url"

    override lazy val stateBenefitsServiceBaseUrl: String = "http://localhost:11111"

    override lazy val taxYearErrorFeature: Boolean = false

    override def incomeTaxSubmissionOverviewUrl(taxYear: Int): String = s"/$taxYear/income-tax-return-overview"

    override def contactUrl(isAgent: Boolean): String = "/contact-url"

    override def betaFeedbackUrl(request: RequestHeader, isAgent: Boolean) = "/beta-feedback-url"

    override def feedbackSurveyUrl(isAgent: Boolean): String = "/feedback-survey-url"
  }
}
