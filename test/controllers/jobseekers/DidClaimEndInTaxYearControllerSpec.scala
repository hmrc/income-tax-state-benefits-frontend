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

import controllers.jobseekers.routes.{DidClaimEndInTaxYearController, EndDateController}
import forms.{FormsProvider, YesNoForm}
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.mvc.Results.Redirect
import play.api.test.Helpers.{contentAsString, contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.mocks.MockActionsProvider
import views.html.pages.jobseekers.DidClaimEndInTaxYearPageView

import java.util.UUID

class DidClaimEndInTaxYearControllerSpec extends ControllerUnitTest
  with MockActionsProvider {

  private val pageView = inject[DidClaimEndInTaxYearPageView]
  private val sessionDataId = UUID.randomUUID()

  private val underTest = new DidClaimEndInTaxYearController(
    actionsProvider = mockActionsProvider,
    formsProvider = new FormsProvider(),
    pageView = pageView
  )

  ".show" should {
    "return a successful response" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)

      val result = underTest.show(taxYearEOY, sessionDataId).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }

  ".submit" should {
    "render page with error when validation of form fails" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "")
      val result = underTest.submit(taxYearEOY, sessionDataId).apply(request)

      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      val document = Jsoup.parse(contentAsString(result))
      document.select("#error-summary-title").isEmpty shouldBe false
    }

    "redirect End Date page when Yes is submitted" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")
      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe
        Redirect(EndDateController.show(taxYearEOY, sessionDataId))
    }

    "redirect To Same (for now... it will be updated) page when No is submitted" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "false")
      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe
        Redirect(DidClaimEndInTaxYearController.show(taxYearEOY, sessionDataId))
    }
  }
}