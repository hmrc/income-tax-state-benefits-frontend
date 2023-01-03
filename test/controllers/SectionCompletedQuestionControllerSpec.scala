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

import controllers.routes.SummaryController
import forms.{FormsProvider, YesNoForm}
import models.BenefitType.JobSeekersAllowance
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.mvc.Results.Redirect
import play.api.test.Helpers.{contentAsString, contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.mocks.{MockActionsProvider, MockClaimService, MockErrorHandler}
import views.html.pages.SectionCompletedQuestionPageView

class SectionCompletedQuestionControllerSpec extends ControllerUnitTest
  with MockActionsProvider
  with MockClaimService
  with MockErrorHandler {

  private val pageView = inject[SectionCompletedQuestionPageView]

  private val underTest = new SectionCompletedQuestionController(
    actionsProvider = mockActionsProvider,
    formsProvider = new FormsProvider(),
    pageView = pageView,
    errorHandler = mockErrorHandler
  )

  ".show" should {
    "return a successful response" in {
      mockPriorDataFor(taxYearEOY, anIncomeTaxUserData)

      val result = underTest.show(taxYearEOY, JobSeekersAllowance).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }

  ".submit" should {
    "render page with error when validation of form fails" in {
      mockPriorDataFor(taxYearEOY, anIncomeTaxUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "")
      val result = underTest.submit(taxYearEOY, JobSeekersAllowance).apply(request)

      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-error-summary").isEmpty shouldBe false
    }

    "redirect to Summary page when Yes is submitted" in {
      mockPriorDataFor(taxYearEOY, anIncomeTaxUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")

      await(underTest.submit(taxYearEOY, JobSeekersAllowance)(request)) shouldBe Redirect(SummaryController.show(taxYearEOY))
    }

    "redirect to Summary page when No is submitted" in {
      mockPriorDataFor(taxYearEOY, anIncomeTaxUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "false")

      await(underTest.submit(taxYearEOY, JobSeekersAllowance)(request)) shouldBe Redirect(SummaryController.show(taxYearEOY))
    }
  }
}
