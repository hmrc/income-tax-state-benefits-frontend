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

import controllers.jobseekers.routes.ClaimsController
import models.BenefitType.JobSeekersAllowance
import models.errors.HttpParserError
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.test.Helpers.{contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.mocks.{MockActionsProvider, MockErrorHandler, MockStateBenefitsService}
import views.html.pages.jobseekers.ReviewClaimPageView

import java.util.UUID

class ReviewClaimControllerSpec extends ControllerUnitTest
  with MockActionsProvider
  with MockStateBenefitsService
  with MockErrorHandler {

  private val pageView = inject[ReviewClaimPageView]
  private val sessionDataId = UUID.randomUUID()

  private val underTest = new ReviewClaimController(
    mockActionsProvider,
    pageView,
    mockStateBenefitsService,
    mockErrorHandler
  )

  "show" should {
    "return a successful response" in {
      mockSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)

      val result = underTest.show(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }

  ".saveAndContinue" should {
    "handle internal server error when updating saveStateBenefit fails" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)
      mockSaveStateBenefit(aStateBenefitsUserData, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockInternalServerError(InternalServerError)

      val result = underTest.saveAndContinue(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(fakeIndividualRequest.withMethod(POST.method))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to JobSeekersAllowance Page on successful save of StateBenefit" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)
      mockSaveStateBenefit(aStateBenefitsUserData, Right(()))

      val request = fakeIndividualRequest.withMethod(POST.method)

      await(underTest.saveAndContinue(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(request)) shouldBe
        Redirect(ClaimsController.show(taxYearEOY, JobSeekersAllowance))
    }
  }

  ".restoreClaim" should {
    "handle internal server error when restoreClaim fails" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)
      mockRestoreClaim(aUser, sessionDataId, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockInternalServerError(InternalServerError)

      val result = underTest.restoreClaim(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(fakeIndividualRequest.withMethod(POST.method))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to JobSeekersAllowance Page on successful claim restore" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)
      mockRestoreClaim(aUser, sessionDataId, Right(()))

      val request = fakeIndividualRequest.withMethod(POST.method)

      await(underTest.restoreClaim(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(request)) shouldBe
        Redirect(ClaimsController.show(taxYearEOY, JobSeekersAllowance))
    }
  }
}
