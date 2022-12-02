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

import controllers.jobseekers.routes.JobSeekersAllowanceController
import forms.AmountForm.amount
import models.errors.HttpParserError
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.test.Helpers.{contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.mocks.{MockActionsProvider, MockErrorHandler, MockStateBenefitsService}
import views.html.pages.jobseekers.RemoveClaimView

import java.util.UUID

class RemoveClaimControllerSpec extends ControllerUnitTest
  with MockActionsProvider
  with MockStateBenefitsService
  with MockErrorHandler {

  private val pageView = inject[RemoveClaimView]
  private val sessionDataId = UUID.randomUUID()

  private val underTest = new RemoveClaimController(
    mockActionsProvider,
    pageView,
    mockStateBenefitsService,
    mockErrorHandler
  )

  ".show" should {
    "return a successful response" in {
      mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)

      val result = underTest.show(taxYearEOY, sessionDataId).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }

  ".submit" when {
    "given HMRC claim data" should {
      "return error when stateBenefitsService.ignoreClaim(...) returns error" in {
        mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
        mockIgnoreClaim(aUser, sessionDataId, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
        mockInternalServerError(InternalServerError)

        val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(s"$amount" -> "100")
        val result = underTest.submit(taxYearEOY, sessionDataId).apply(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "redirect to JobSeekersAllowanceController when stateBenefitsService.ignoreClaim(...) succeeds" in {
        mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
        mockIgnoreClaim(aUser, sessionDataId, Right(Unit))

        val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(s"$amount" -> "100")

        await(underTest.submit(taxYearEOY, sessionDataId).apply(request)) shouldBe
          Redirect(JobSeekersAllowanceController.show(taxYearEOY))
      }
    }

    "given customer claim data" should {
      val customerAddedData = aClaimCYAModel.copy(isHmrcData = false)

      "return error when stateBenefitsService.removeClaim(...) returns error" in {
        mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData.copy(claim = Some(customerAddedData)))
        mockRemoveClaim(aUser, sessionDataId, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
        mockInternalServerError(InternalServerError)

        val request = fakeIndividualRequest.withMethod(POST.method)
        val result = underTest.submit(taxYearEOY, sessionDataId).apply(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "redirect to JobSeekersAllowanceController when stateBenefitsService.removeClaim(...) succeeds" in {
        mockUserSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData.copy(claim = Some(customerAddedData)))
        mockRemoveClaim(aUser, sessionDataId, Right(Unit))

        val request = fakeIndividualRequest.withMethod(POST.method)

        await(underTest.submit(taxYearEOY, sessionDataId).apply(request)) shouldBe
          Redirect(JobSeekersAllowanceController.show(taxYearEOY))
      }
    }
  }
}
