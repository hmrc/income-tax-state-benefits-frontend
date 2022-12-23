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

package controllers.session

import controllers.jobseekers.routes.{ClaimsController, ReviewClaimController, StartDateController}
import models.BenefitType.JobSeekersAllowance
import models.StateBenefitsUserData
import models.errors.HttpParserError
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.test.Helpers.status
import support.ControllerUnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.UserBuilder.aUser
import support.mocks.{MockActionsProvider, MockErrorHandler, MockStateBenefitsService}

import java.util.UUID

class UserSessionDataControllerSpec extends ControllerUnitTest
  with MockActionsProvider
  with MockStateBenefitsService
  with MockErrorHandler {

  private val benefitId = UUID.randomUUID()
  private val sessionDataId = UUID.randomUUID()

  private val underTest = new UserSessionDataController(mockActionsProvider, mockStateBenefitsService, mockErrorHandler)

  ".create" should {
    "return error when stateBenefitsService.createOrUpdate(...) returns Left" in {
      mockEndOfYear(taxYearEOY)
      mockCreateOrUpdate(StateBenefitsUserData(taxYearEOY, aUser), Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockInternalServerError(InternalServerError)

      val result = underTest.create(taxYearEOY, JobSeekersAllowance).apply(fakeIndividualRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to StartDateController when stateBenefitsService.createOrUpdate(...) returns value" in {
      val sessionDataId = UUID.randomUUID()

      mockEndOfYear(taxYearEOY)
      mockCreateOrUpdate(StateBenefitsUserData(taxYearEOY, aUser), Right(sessionDataId))

      val result = await(underTest.create(taxYearEOY, JobSeekersAllowance).apply(fakeIndividualRequest))

      result shouldBe Redirect(StartDateController.show(taxYearEOY, JobSeekersAllowance, sessionDataId))
    }
  }

  ".loadToSession" should {
    "redirect to JobSeekersAllowance page when benefitId not found" in {
      mockPriorDataFor(taxYearEOY, anIncomeTaxUserData)

      val result = await(underTest.loadToSession(taxYearEOY, JobSeekersAllowance, benefitId).apply(fakeIndividualRequest))

      result shouldBe Redirect(ClaimsController.show(taxYearEOY, JobSeekersAllowance))
    }

    "return error when stateBenefitsService.createOrUpdate(...) returns Left" in {
      mockPriorDataFor(taxYearEOY, anIncomeTaxUserData)
      mockCreateOrUpdate(StateBenefitsUserData(taxYearEOY, aUser, aStateBenefit.benefitId, anIncomeTaxUserData).get, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockInternalServerError(InternalServerError)

      val result = underTest.loadToSession(taxYearEOY, JobSeekersAllowance, aStateBenefit.benefitId).apply(fakeIndividualRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to ReviewClaimController when stateBenefitsService.createOrUpdate(...) returns Right" in {
      mockPriorDataFor(taxYearEOY, anIncomeTaxUserData)
      mockCreateOrUpdate(StateBenefitsUserData(taxYearEOY, aUser, aStateBenefit.benefitId, anIncomeTaxUserData).get, Right(sessionDataId))

      val result = await(underTest.loadToSession(taxYearEOY, JobSeekersAllowance, aStateBenefit.benefitId).apply(fakeIndividualRequest))

      result shouldBe Redirect(ReviewClaimController.show(taxYearEOY, JobSeekersAllowance, sessionDataId))
    }
  }
}
