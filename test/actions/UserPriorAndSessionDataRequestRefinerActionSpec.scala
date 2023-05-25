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

package actions

import models.BenefitDataType.CustomerOverride
import models.BenefitType
import models.BenefitType.JobSeekersAllowance
import models.errors.HttpParserError
import models.requests.UserPriorAndSessionDataRequest
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results.InternalServerError
import support.UnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.requests.UserSessionDataRequestBuilder.aUserSessionDataRequest
import support.mocks.{MockErrorHandler, MockStateBenefitsService}
import support.providers.TaxYearProvider

import scala.concurrent.ExecutionContext

class UserPriorAndSessionDataRequestRefinerActionSpec extends UnitTest
  with MockStateBenefitsService
  with MockErrorHandler
  with TaxYearProvider {

  private val executionContext = ExecutionContext.global

  private def createAction(benefitType: BenefitType) = UserPriorAndSessionDataRequestRefinerAction(
    taxYear,
    mockStateBenefitsService,
    benefitType,
    mockErrorHandler
  )(executionContext)

  private val underTest = createAction(JobSeekersAllowance)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".refine" should {

    "do not call other services if it is not customer override" in {
      await(underTest.refine(aUserSessionDataRequest)) shouldBe Right(UserPriorAndSessionDataRequest(aStateBenefitsUserData, None, aUserSessionDataRequest.user, aUserSessionDataRequest.request))
    }

    "handle InternalServerError when when getting prior data result in an error" in {
      val userSessionDataRequest = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(benefitDataType = CustomerOverride.name))
      mockGetPriorData(userSessionDataRequest.user, taxYear, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      await(underTest.refine(userSessionDataRequest)) shouldBe Left(InternalServerError)
    }

    "return IncomeTaxUserData when the service returns data" in {
      val userSessionDataRequest = aUserSessionDataRequest.copy(stateBenefitsUserData = aStateBenefitsUserData.copy(benefitDataType = CustomerOverride.name))

      mockGetPriorData(userSessionDataRequest.user, taxYear, Right(anIncomeTaxUserData))

      await(underTest.refine(userSessionDataRequest)) shouldBe
        Right(UserPriorAndSessionDataRequest(userSessionDataRequest.stateBenefitsUserData, Some(aStateBenefit), userSessionDataRequest.user, userSessionDataRequest.request))
    }
  }
}
