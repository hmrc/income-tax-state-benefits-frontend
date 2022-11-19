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

import controllers.jobseekers.routes.{AmountController, EndDateController}
import forms.YesNoForm
import forms.jobseekers.FormsProvider
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.test.Helpers.{contentAsString, contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.mocks.{MockActionsProvider, MockClaimService, MockErrorHandler}
import views.html.pages.jobseekers.DidClaimEndInTaxYearPageView

import java.util.UUID

class DidClaimEndInTaxYearControllerSpec extends ControllerUnitTest
  with MockActionsProvider
  with MockClaimService
  with MockErrorHandler {

  private val pageView = inject[DidClaimEndInTaxYearPageView]
  private val sessionDataId = UUID.randomUUID()

  private val underTest = new DidClaimEndInTaxYearController(
    actionsProvider = mockActionsProvider,
    formsProvider = new FormsProvider(),
    pageView = pageView,
    claimService = mockClaimService,
    errorHandler = mockErrorHandler
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

    "redirect to End Date page when Yes is submitted" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = true, Right(sessionDataId))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")

      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe Redirect(EndDateController.show(taxYearEOY, sessionDataId))
    }

    "redirect To Amount page when No is submitted" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = false, Right(sessionDataId))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "false")

      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe Redirect(AmountController.show(taxYearEOY, sessionDataId))
    }

    "handle internal server error when updating end date question fails" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = true, Left(()))
      mockInternalServerError(InternalServerError)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")
      val result = underTest.submit(taxYearEOY, sessionDataId).apply(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
