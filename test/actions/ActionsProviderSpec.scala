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

import controllers.errors.routes.UnauthorisedUserErrorController
import controllers.routes.ClaimsController
import models.BenefitDataType.CustomerAdded
import models.BenefitType.JobSeekersAllowance
import models.IncomeTaxUserData
import models.authorisation.SessionValues.{TAX_YEAR, VALID_TAX_YEARS}
import models.errors.HttpParserError
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Results.{InternalServerError, Ok, Redirect}
import play.api.mvc.{AnyContent, Request}
import play.api.test.Helpers.status
import support.ControllerUnitTest
import support.builders.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.mocks.{MockAuthorisedAction, MockErrorHandler, MockStateBenefitsService}

import java.util.UUID

class ActionsProviderSpec extends ControllerUnitTest
  with MockAuthorisedAction
  with MockStateBenefitsService
  with MockErrorHandler {

  private val anyBlock = (_: Request[AnyContent]) => Ok("any-result")
  private val validTaxYears = validTaxYearList.mkString(",")
  private val sessionDataId = UUID.randomUUID()

  private val actionsProvider = new ActionsProvider(
    mockAuthorisedAction,
    mockStateBenefitsService,
    mockErrorHandler,
    appConfig
  )

  ".priorDataFor(taxYear)" should {
    "redirect to UnauthorisedUserErrorController when authentication fails" in {
      mockFailToAuthenticate()

      val underTest = actionsProvider.priorDataFor(taxYearEOY)(block = anyBlock)

      await(underTest(fakeIndividualRequest)) shouldBe Redirect(UnauthorisedUserErrorController.show)
    }

    "handle internal server error when getPriorData result in error" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetPriorData(aUser, taxYearEOY, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      val underTest = actionsProvider.priorDataFor(taxYearEOY)(block = anyBlock)

      await(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe InternalServerError
    }

    "return successful response when end of year" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetPriorData(aUser, taxYearEOY, Right(IncomeTaxUserData(stateBenefits = Some(anAllStateBenefitsData))))

      val underTest = actionsProvider.priorDataFor(taxYearEOY)(block = anyBlock)

      status(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe OK
    }

    "return successful response when in year" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetPriorData(aUser, taxYear, Right(IncomeTaxUserData(stateBenefits = Some(anAllStateBenefitsData))))

      val underTest = actionsProvider.priorDataFor(taxYear)(block = anyBlock)

      status(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYear.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe OK
    }
  }

  ".endOfYearSessionDataFor(taxYear)" should {
    "redirect to UnauthorisedUserErrorController when authentication fails" in {
      mockFailToAuthenticate()

      val underTest = actionsProvider.endOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      await(underTest(fakeIndividualRequest)) shouldBe Redirect(UnauthorisedUserErrorController.show)
    }

    "redirect to Income Tax Submission Overview when in year" in {
      mockAuthAsIndividual(Some(aUser.nino))

      val underTest = actionsProvider.endOfYearSessionDataFor(taxYear, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      await(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYear.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe
        Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))
    }

    "handle internal server error when getUserSessionData result in error" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetUserSessionData(aUser, sessionDataId, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      val underTest = actionsProvider.endOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      await(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe InternalServerError
    }

    "return successful response when end of year" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData))

      val underTest = actionsProvider.endOfYearSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      status(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe OK
    }
  }

  ".reviewClaimSessionDataFor(...)" should {
    "redirect to UnauthorisedUserErrorController when authentication fails" in {
      mockFailToAuthenticate()

      val underTest = actionsProvider.reviewClaimSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      await(underTest(fakeIndividualRequest)) shouldBe Redirect(UnauthorisedUserErrorController.show)
    }

    "handle internal server error when getUserSessionData result in error" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetUserSessionData(aUser, sessionDataId, Left(HttpParserError(INTERNAL_SERVER_ERROR)))
      mockHandleError(INTERNAL_SERVER_ERROR, InternalServerError)

      val underTest = actionsProvider.reviewClaimSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      await(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe InternalServerError
    }

    "redirect to ClaimsController when ReviewClaimFilterAction returns Redirect" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData.copy(benefitDataType = CustomerAdded.name, claim = None)))

      val underTest = actionsProvider.reviewClaimSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      await(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe
        Redirect(ClaimsController.show(taxYearEOY, JobSeekersAllowance))
    }

    "return successful UserSessionDataRequest when user session data exists" in {
      mockAuthAsIndividual(Some(aUser.nino))
      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData))

      val underTest = actionsProvider.reviewClaimSessionDataFor(taxYearEOY, JobSeekersAllowance, sessionDataId)(block = anyBlock)

      status(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe OK
    }
  }

  ".endOfYear(taxYear)" should {
    "redirect to UnauthorisedUserErrorController when authentication fails" in {
      mockFailToAuthenticate()

      val underTest = actionsProvider.endOfYear(taxYearEOY)(block = anyBlock)

      await(underTest(fakeIndividualRequest)) shouldBe Redirect(UnauthorisedUserErrorController.show)
    }

    "redirect to Income Tax Submission Overview when in year" in {
      mockAuthAsIndividual(Some(aUser.nino))

      val underTest = actionsProvider.endOfYear(taxYear)(block = anyBlock)

      await(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYear.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe
        Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))
    }

    "return successful response when end of year" in {
      mockAuthAsIndividual(Some(aUser.nino))

      val underTest = actionsProvider.endOfYear(taxYearEOY)(block = anyBlock)

      status(underTest(fakeIndividualRequest.withSession(TAX_YEAR -> taxYearEOY.toString, VALID_TAX_YEARS -> validTaxYears))) shouldBe OK
    }
  }
}
