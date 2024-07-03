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

import forms.{FormsProvider, YesNoForm}
import models.BenefitType.JobSeekersAllowance
import models.StateBenefitsUserData
import models.errors.HttpParserError
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.test.Helpers.{contentAsString, contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.UserBuilder.aUser
import support.mocks.{MockActionsProvider, MockErrorHandler, MockStateBenefitsService}
import views.html.pages.ClaimsPageView

import java.util.UUID

class ClaimsControllerSpec extends ControllerUnitTest
  with MockActionsProvider with MockStateBenefitsService with MockErrorHandler {

  private val pageView = inject[ClaimsPageView]

  private val underTest = new ClaimsController(
    mockActionsProvider,
    pageView,
    stateBenefitsService = mockStateBenefitsService,
    formsProvider = new FormsProvider(),
    errorHandler = mockErrorHandler
  )

  "show" should {
    "return a successful response" in {
      mockPriorDataWithViewStateBenefitsAudit(taxYear, JobSeekersAllowance, anIncomeTaxUserData)

      val result = underTest.show(taxYear, JobSeekersAllowance).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }

  "submit" should {
    "render page with error when validation of form fails" in {
      mockPriorDataWithViewStateBenefitsAudit(taxYearEOY, JobSeekersAllowance, anIncomeTaxUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "")
      val result = underTest.submit(taxYearEOY, JobSeekersAllowance).apply(request)

      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-error-summary").isEmpty shouldBe false
    }

    "redirect to summary page if claim is false" in {
      mockPriorDataWithViewStateBenefitsAudit(taxYearEOY, JobSeekersAllowance, anIncomeTaxUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "false")

      await(underTest.submit(taxYearEOY, JobSeekersAllowance)(request)) shouldBe
        Redirect(routes.SummaryController.show(taxYearEOY))
    }

    "return error when stateBenefitsService.createSessionData(...) returns Left" in {
      mockPriorDataWithViewStateBenefitsAudit(taxYearEOY, JobSeekersAllowance, anIncomeTaxUserData)
      mockCreateSessionData(StateBenefitsUserData(taxYearEOY, JobSeekersAllowance, aUser), Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockInternalServerError(InternalServerError)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")
      val result = underTest.submit(taxYearEOY, JobSeekersAllowance).apply(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to StartDateController when stateBenefitsService.createSessionData(...) returns value" in {
      val sessionDataId = UUID.randomUUID()

      mockPriorDataWithViewStateBenefitsAudit(taxYearEOY, JobSeekersAllowance, anIncomeTaxUserData)
      mockCreateSessionData(StateBenefitsUserData(taxYearEOY, JobSeekersAllowance, aUser), Right(sessionDataId))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")
      val result = await(underTest.submit(taxYearEOY, JobSeekersAllowance).apply(request))

      result shouldBe Redirect(routes.StartDateController.show(taxYearEOY, JobSeekersAllowance, sessionDataId))
    }

  }

}
