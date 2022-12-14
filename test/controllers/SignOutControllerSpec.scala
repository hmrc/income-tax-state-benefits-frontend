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

package controllers

import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers.{redirectLocation, status}
import support.ControllerUnitTest

class SignOutControllerSpec extends ControllerUnitTest {

  private val underTest = new SignOutController()

  ".switchToLanguage" should {
    "return a redirect result" in {
      val result = underTest.signOut(isAgent = false).apply(fakeRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/sign-out-url?continue=%2Ffeedback-survey-url")
    }
  }
}
