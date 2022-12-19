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

import controllers.jobseekers.routes.{AmountController, EndDateController, ReviewClaimController}
import forms.YesNoForm
import forms.jobseekers.FormsProvider
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.test.Helpers.{contentAsString, contentType, status}
import sttp.model.Method.POST
import support.ControllerUnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.mocks.{MockActionsProvider, MockClaimService, MockErrorHandler}
import views.html.pages.jobseekers.EndDateQuestionPageView

class EndDateQuestionControllerSpec extends ControllerUnitTest
  with MockActionsProvider
  with MockClaimService
  with MockErrorHandler {

  private val pageView = inject[EndDateQuestionPageView]
  private val sessionDataId = aStateBenefitsUserData.sessionDataId.get

  private val underTest = new EndDateQuestionController(
    actionsProvider = mockActionsProvider,
    formsProvider = new FormsProvider(),
    pageView = pageView,
    claimService = mockClaimService,
    errorHandler = mockErrorHandler
  )

  ".show" should {
    "return a successful response" in {
      mockEndOfYearSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)

      val result = underTest.show(taxYearEOY, sessionDataId).apply(fakeIndividualRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }

  ".submit" should {
    "render page with error when validation of form fails" in {
      mockEndOfYearSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "")
      val result = underTest.submit(taxYearEOY, sessionDataId).apply(request)

      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-error-summary").isEmpty shouldBe false
    }

    "redirect to ReviewClaim page when Yes is submitted and isFinished" in {
      mockEndOfYearSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = true, Right(aStateBenefitsUserData))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")

      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe Redirect(ReviewClaimController.show(taxYearEOY, sessionDataId))
    }

    "redirect to ReviewClaim page when No is submitted and isFinished" in {
      mockEndOfYearSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = false, Right(aStateBenefitsUserData))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "false")

      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe Redirect(ReviewClaimController.show(taxYearEOY, sessionDataId))
    }

    "redirect to End Date page when Yes is submitted and not finished" in {
      mockEndOfYearSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = true, Right(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaid = None)))))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")

      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe Redirect(EndDateController.show(taxYearEOY, sessionDataId))
    }

    "redirect To Amount page when No is submitted and not finished" in {
      mockEndOfYearSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = false, Right(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaid = None)))))

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "false")

      await(underTest.submit(taxYearEOY, sessionDataId)(request)) shouldBe Redirect(AmountController.show(taxYearEOY, sessionDataId))
    }

    "handle internal server error when updating end date question fails" in {
      mockEndOfYearSessionDataFor(taxYearEOY, sessionDataId, aStateBenefitsUserData)
      mockUpdateEndDateQuestion(aStateBenefitsUserData, question = true, Left(()))
      mockInternalServerError(InternalServerError)

      val request = fakeIndividualRequest.withMethod(POST.method).withFormUrlEncodedBody(YesNoForm.yesNo -> "true")
      val result = underTest.submit(taxYearEOY, sessionDataId).apply(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
