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

package controllers.jobseekers

import models.BenefitType.JobSeekersAllowance
import play.api.http.Status.OK
import play.api.test.Helpers.{contentType, status}
import support.ControllerUnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.mocks.MockActionsProvider
import views.html.pages.jobseekers.ClaimsPageView

class ClaimsControllerSpec extends ControllerUnitTest
  with MockActionsProvider {

  private val pageView = inject[ClaimsPageView]

  private val underTest = new ClaimsController(
    mockActionsProvider,
    pageView
  )

  "show" should {
    "return a successful response" in {
      mockPriorDataFor(taxYear, anIncomeTaxUserData)

      val result = underTest.show(taxYear, JobSeekersAllowance).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }
}
