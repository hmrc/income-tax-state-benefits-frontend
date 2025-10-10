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

import com.google.inject.ImplementedBy
import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {
  def signOutUrl: String
  def welshLanguageEnabled: Boolean
  def sectionCompletedQuestionEnabled: Boolean
  def sessionCookieServiceEnabled: Boolean
  def defaultTaxYear: Int
  def languageMap: Map[String, Lang]
  def timeoutDialogTimeout: Int
  def timeoutDialogCountdown: Int
  def signInUrl: String
  def vcSessionServiceBaseUrl: String
  def incomeTaxSubmissionStartUrl(taxYear: Int): String
  def incomeTaxSubmissionIvRedirect: String
  def incomeTaxSubmissionOverviewUrl(taxYear: Int): String
  def commonTaskListUrl(taxYear: Int): String
  def contactUrl(isAgent: Boolean): String
  def feedbackSurveyUrl(isAgent: Boolean): String
  def viewAndChangeEnterUtrUrl: String
  def viewAndChangeViewUrlAgent: String
  def betaFeedbackUrl(request: RequestHeader, isAgent: Boolean): String
  def stateBenefitsServiceBaseUrl: String
  def taxYearErrorFeature: Boolean

  // TODO: Get rid of this
  def routeToSwitchLanguage: String => Call
}

@Singleton
class AppConfigImpl @Inject()(servicesConfig: ServicesConfig) extends AppConfig {

  private lazy val stateBenefitsUrlKey = "microservice.services.income-tax-state-benefits.url"

  private lazy val incomeTaxSubmissionFrontendUrlKey = "microservice.services.income-tax-submission-frontend.url"
  private lazy val basGatewayFrontendUrlKey = "microservice.services.bas-gateway-frontend.url"
  private lazy val feedbackFrontendUrlKey = "microservice.services.feedback-frontend.url"
  private lazy val contactFormServiceIndividualKey = "update-and-submit-income-tax-return"
  // TODO: The key is missing in CIS and Employment. Verify if still needed.
  private lazy val contactFormServiceAgentKey = "update-and-submit-income-tax-return-agent"
  private lazy val contactFrontendUrlKey = "microservice.services.contact-frontend.url"
  private lazy val viewAndChangeUrlKey = "microservice.services.view-and-change.url"
  private lazy val signInContinueUrlKey = "microservice.services.sign-in.continueUrl"

  private lazy val applicationUrl: String = servicesConfig.getString("microservice.url")
  private lazy val basGatewayUrl = servicesConfig.getString(basGatewayFrontendUrlKey)
  private lazy val feedbackFrontendUrl = servicesConfig.getString(feedbackFrontendUrlKey)
  private lazy val contactFrontEndUrl = servicesConfig.getString(contactFrontendUrlKey)
  private lazy val vcBaseUrl: String = servicesConfig.getString(viewAndChangeUrlKey)
  private lazy val signInBaseUrl: String = servicesConfig.getString("microservice.services.sign-in.url")
  private lazy val signInContinueBaseUrl: String = servicesConfig.getString(signInContinueUrlKey)
  private lazy val signInContinueUrlRedirect: String = URLEncoder.encode(signInContinueBaseUrl, "UTF-8")
  private lazy val signInOrigin = servicesConfig.getString("appName")

  lazy val signOutUrl: String = s"$basGatewayUrl/bas-gateway/sign-out-without-state"
  lazy val welshLanguageEnabled: Boolean = servicesConfig.getBoolean(key = "feature-switch.welshLanguageEnabled")
  def sectionCompletedQuestionEnabled: Boolean = servicesConfig.getBoolean(key = "feature-switch.sectionCompletedQuestionEnabled")
  lazy val sessionCookieServiceEnabled: Boolean = servicesConfig.getBoolean("feature-switch.sessionCookieServiceEnabled")
  lazy val defaultTaxYear: Int = servicesConfig.getInt(key = "defaultTaxYear")
  lazy val languageMap: Map[String, Lang] = Map("english" -> Lang("en"), "cymraeg" -> Lang("cy"))
  lazy val timeoutDialogTimeout: Int = servicesConfig.getInt("timeoutDialogTimeout")
  lazy val timeoutDialogCountdown: Int = servicesConfig.getInt("timeoutDialogCountdown")
  lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrlRedirect&origin=$signInOrigin"
  lazy val vcSessionServiceBaseUrl: String = servicesConfig.baseUrl("income-tax-session-data")
  def incomeTaxSubmissionBaseUrl: String = servicesConfig.getString(incomeTaxSubmissionFrontendUrlKey) +
    servicesConfig.getString(key = "microservice.services.income-tax-submission-frontend.context")

  def incomeTaxSubmissionStartUrl(taxYear: Int): String = incomeTaxSubmissionBaseUrl + "/" + taxYear + "/start"

  def incomeTaxSubmissionIvRedirect: String = incomeTaxSubmissionBaseUrl +
    servicesConfig.getString("microservice.services.income-tax-submission-frontend.iv-redirect")

  def incomeTaxSubmissionOverviewUrl(taxYear: Int): String = incomeTaxSubmissionBaseUrl + "/" + taxYear +
    servicesConfig.getString("microservice.services.income-tax-submission-frontend.overview")

  def commonTaskListUrl(taxYear: Int): String = incomeTaxSubmissionBaseUrl + "/" + taxYear + "/tasklist"

  def contactUrl(isAgent: Boolean): String = s"$contactFrontEndUrl/contact/contact-hmrc?service=${contactFormServiceIdentifier(isAgent)}"

  def contactFormServiceIdentifier(isAgent: Boolean): String = if (isAgent) contactFormServiceAgentKey else contactFormServiceIndividualKey

  def feedbackSurveyUrl(isAgent: Boolean): String = s"$feedbackFrontendUrl/feedback/${contactFormServiceIdentifier(isAgent)}"

  def viewAndChangeEnterUtrUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"
  def viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents"

  def betaFeedbackUrl(request: RequestHeader, isAgent: Boolean): String = {
    val requestUri = URLEncoder.encode(applicationUrl + request.uri, "UTF-8")
    val contactFormService = contactFormServiceIdentifier(isAgent)
    s"$contactFrontEndUrl/contact/beta-feedback?service=$contactFormService&backUrl=$requestUri"
  }

  lazy val stateBenefitsServiceBaseUrl: String = s"${servicesConfig.getString(stateBenefitsUrlKey)}/income-tax-state-benefits"

  def taxYearErrorFeature: Boolean = servicesConfig.getBoolean("taxYearErrorFeatureSwitch")

  // TODO: Get rid of this
  def routeToSwitchLanguage: String => Call =
    (lang: String) => controllers.routes.LanguageSwitchController.switchToLanguage(lang)
}
