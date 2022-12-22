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

package controllers

import controllers.routes.{ReviewClaimController, TaxPaidQuestionController}
import forms.AmountForm.amount
import forms.FormsProvider
import models.BenefitType.JobSeekersAllowance
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.test.Helpers.{contentAsString, contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.mocks.{MockActionsProvider, MockClaimService, MockErrorHandler}
import views.html.pages.AmountPageView

class AmountControllerSpec extends ControllerUnitTest
  with MockActionsProvider
  with MockClaimService
  with MockErrorHandler {

  private val pageView = inject[AmountPageView]
  private val sessionDataId = aStateBenefitsUserData.sessionDataId.get

  private val underTest = new AmountController(
    mockActionsProvider,
    new FormsProvider(),
    pageView,
    mockClaimService,
    mockErrorHandler
  )

  ".show" should {
    "return a successful response" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)

      val result = underTest.show(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }

  ".submit" should {
    "render page with error when validation of form fails" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(s"$amount" -> "")
      val result = underTest.submit(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(request)

      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-error-summary").isEmpty shouldBe false
    }

    "handle internal server error when updating amount fails" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)
      mockUpdateAmount(aStateBenefitsUserData, amount = 100, Left(()))
      mockInternalServerError(InternalServerError)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(s"$amount" -> "100")
      val result = underTest.submit(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to next Page on successful amount update and journey not completed" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)
      mockUpdateAmount(aStateBenefitsUserData, amount = 100, Right(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaid = None)))))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(s"$amount" -> "100")

      await(underTest.submit(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(request)) shouldBe
        Redirect(TaxPaidQuestionController.show(taxYearEOY, JobSeekersAllowance, sessionDataId))
    }

    "redirect to ReviewClaim Page on successful amount update and when journey is completed" in {
      mockEndOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId, aStateBenefitsUserData)
      mockUpdateAmount(aStateBenefitsUserData, amount = 100, Right(aStateBenefitsUserData))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(s"$amount" -> "100")

      await(underTest.submit(taxYearEOY, JobSeekersAllowance, sessionDataId).apply(request)) shouldBe
        Redirect(ReviewClaimController.show(taxYearEOY, JobSeekersAllowance, sessionDataId))
    }
  }
}
