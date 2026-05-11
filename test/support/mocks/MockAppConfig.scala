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
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

trait MockAppConfig extends MockitoSugar {
  val mockAppConfig: AppConfig = mock[AppConfig]

  val baseUrl = "/update-and-submit-income-tax-return/state-benefits"
  val viewAndChangeUrl: String = "/report-quarterly/income-and-expenses/view/agents/client-utr"
  val signInUrl: String = s"$baseUrl/signIn"
  val sessionCookieServiceEnabled: Boolean = false

  def mockSignInUrl(): Unit =
    when(mockAppConfig.signInUrl).thenReturn(signInUrl)

  def mockViewAndChangeUrl(): Unit =
    when(mockAppConfig.viewAndChangeEnterUtrUrl).thenReturn(viewAndChangeUrl)

  def mockSessionServiceEnabled(response: Boolean): Unit =
    when(mockAppConfig.sessionCookieServiceEnabled).thenReturn(response)
}
