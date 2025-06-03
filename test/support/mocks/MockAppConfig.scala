/*
 * Copyright 2024 HM Revenue & Customs
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

package support.mocks

import config.AppConfig
import org.scalamock.handlers.CallHandler0
import org.scalamock.scalatest.MockFactory

trait MockAppConfig extends MockFactory {
  val mockAppConfig: AppConfig = mock[AppConfig]

  val baseUrl = "/update-and-submit-income-tax-return/state-benefits"
  val viewAndChangeUrl: String = "/report-quarterly/income-and-expenses/view/agents/client-utr"
  val signInUrl: String = s"$baseUrl/signIn"
  val sessionCookieServiceEnabled: Boolean = false

  def mockSignInUrl(): CallHandler0[String] =
    (() => mockAppConfig.signInUrl)
      .expects()
      .returning(signInUrl)
      .anyNumberOfTimes()

  def mockViewAndChangeUrl(): CallHandler0[String] =
    (() => mockAppConfig.viewAndChangeEnterUtrUrl)
      .expects()
      .returning(viewAndChangeUrl)
      .anyNumberOfTimes()

  def mockSessionServiceEnabled(response: Boolean): CallHandler0[Boolean] =
    (() => mockAppConfig.sessionCookieServiceEnabled)
      .expects()
      .returning(response)
}
