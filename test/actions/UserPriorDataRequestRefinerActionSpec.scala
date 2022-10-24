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

package actions

import models.errors.HttpParserError
import models.requests.UserPriorDataRequest
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results.InternalServerError
import support.UnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.requests.AuthorisationRequestBuilder.anAuthorisationRequest
import support.mocks.{MockErrorHandler, MockStateBenefitsService}

import scala.concurrent.ExecutionContext

class UserPriorDataRequestRefinerActionSpec extends UnitTest
  with MockStateBenefitsService
  with MockErrorHandler {

  private val anyTaxYear = 2022
  private val executionContext = ExecutionContext.global

  private val underTest = UserPriorDataRequestRefinerAction(anyTaxYear, mockStateBenefitsService, mockErrorHandler)(executionContext)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".refine" should {
    "handle InternalServerError when when getting prior data result in an error" in {
      mockGetPriorData(anAuthorisationRequest.user, anyTaxYear, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      await(underTest.refine(anAuthorisationRequest)) shouldBe Left(InternalServerError)
    }

    "return IncomeTaxUserData when the service returns data" in {
      mockGetPriorData(anAuthorisationRequest.user, anyTaxYear, Right(anIncomeTaxUserData))

      await(underTest.refine(anAuthorisationRequest)) shouldBe Right(UserPriorDataRequest(anIncomeTaxUserData, anAuthorisationRequest.user, anAuthorisationRequest.request))
    }
  }
}
