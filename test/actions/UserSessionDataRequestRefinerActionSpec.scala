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
import models.requests.UserSessionDataRequest
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results.InternalServerError
import support.UnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.builders.requests.AuthorisationRequestBuilder.anAuthorisationRequest
import support.mocks.{MockErrorHandler, MockStateBenefitsService}

import java.util.UUID
import scala.concurrent.ExecutionContext

class UserSessionDataRequestRefinerActionSpec extends UnitTest
  with MockStateBenefitsService
  with MockErrorHandler {

  private val executionContext = ExecutionContext.global
  private val sessionDataId: UUID = UUID.randomUUID()

  private val underTest = UserSessionDataRequestRefinerAction(sessionDataId, mockStateBenefitsService, mockErrorHandler)(executionContext)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".refine" should {
    "handle InternalServerError when when getting session data result in an error" in {
      mockGetUserSessionData(aUser, sessionDataId, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      await(underTest.refine(anAuthorisationRequest)) shouldBe Left(InternalServerError)
    }

    "return StateBenefitsUserData when the service returns data" in {
      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData))

      await(underTest.refine(anAuthorisationRequest)) shouldBe
        Right(UserSessionDataRequest(aStateBenefitsUserData, anAuthorisationRequest.user, anAuthorisationRequest.request))
    }
  }
}
