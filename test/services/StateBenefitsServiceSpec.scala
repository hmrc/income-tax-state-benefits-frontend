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

package services

import connectors.errors.{ApiError, SingleErrorBody}
import models.BenefitDataType.{CustomerOverride, HmrcData}
import models.errors.HttpParserError
import play.api.http.Status.INTERNAL_SERVER_ERROR
import support.UnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.builders.audit.IgnoreStateBenefitAuditBuilder.anIgnoreStateBenefitAudit
import support.builders.audit.UnIgnoreStateBenefitAuditBuilder.anUnIgnoreStateBenefitAudit
import support.mocks.{MockAuditService, MockStateBenefitsConnector}
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class StateBenefitsServiceSpec extends UnitTest
  with MockAuditService
  with MockStateBenefitsConnector
  with TaxYearProvider {

  implicit private val hc: HeaderCarrier = HeaderCarrier()
  private val sessionDataId = UUID.randomUUID()

  private val underTest = new StateBenefitsService(mockAuditService, mockStateBenefitsConnector)

  ".getPriorData(...)" should {
    "return error when fails to get data" in {
      mockGetIncomeTaxUserData(aUser, taxYearEOY, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.getPriorData(aUser, taxYearEOY)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return data" in {
      mockGetIncomeTaxUserData(aUser, taxYearEOY, Right(anIncomeTaxUserData))

      await(underTest.getPriorData(aUser, taxYearEOY)) shouldBe Right(anIncomeTaxUserData)
    }
  }

  ".getUserSessionData(...)" should {
    "return error when fails to create data" in {
      mockGetUserSessionData(aUser, sessionDataId, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return correct result" in {
      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData))

      await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Right(aStateBenefitsUserData)
    }
  }

  ".createSessionData(...)" should {
    "return error when fails to create data" in {
      mockCreateSessionData(aStateBenefitsUserData, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.createSessionData(aStateBenefitsUserData)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "delegate to stateBenefitsConnector.createSessionData() and return the result" in {
      mockCreateSessionData(aStateBenefitsUserData, Right(sessionDataId))

      await(underTest.createSessionData(aStateBenefitsUserData)) shouldBe Right(sessionDataId)
    }
  }

  ".updateSessionData(...)" should {
    "return error when fails to create data" in {
      mockUpdateSessionData(aStateBenefitsUserData, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.updateSessionData(aStateBenefitsUserData)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "create session data with and return its sessionDataId" in {
      mockUpdateSessionData(aStateBenefitsUserData, Right(()))

      await(underTest.updateSessionData(aStateBenefitsUserData)) shouldBe Right(())
    }
  }

  ".saveClaim(...)" should {
    "return error when fails to save data" in {
      mockSaveClaim(aStateBenefitsUserData, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.saveClaim(aStateBenefitsUserData)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return correct result" in {
      mockSaveClaim(aStateBenefitsUserData, Right(()))

      await(underTest.saveClaim(aStateBenefitsUserData)) shouldBe Right(())
    }
  }

  ".removeClaim(...)" should {
    "return error when fails to remove data" in {
      mockRemoveClaim(aUser, sessionDataId, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.removeClaim(sessionDataId, aUser, aStateBenefitsUserData)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "send IgnoreStateBenefitAudit event when HMRC data and return correct result when remove successful" in {
      val stateBenefitsUserData = aStateBenefitsUserData.copy(benefitDataType = HmrcData.name)

      mockRemoveClaim(aUser, sessionDataId, Right(()))
      mockSendAudit(anIgnoreStateBenefitAudit.toAuditModel)

      await(underTest.removeClaim(sessionDataId, aUser, stateBenefitsUserData)) shouldBe Right(())
    }

    "not send audit event when not HMRC data and return correct result when remove successful" in {
      mockRemoveClaim(aUser, sessionDataId, Right(()))

      await(underTest.removeClaim(sessionDataId, aUser, aStateBenefitsUserData.copy(benefitDataType = CustomerOverride.name))) shouldBe Right(())
    }
  }

  ".restoreClaim(...)" should {
    "return error when fails to restore claim" in {
      mockRestoreClaim(aUser, sessionDataId, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.restoreClaim(sessionDataId, aUser, aStateBenefitsUserData)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return correct result when restore claim is successful" in {
      mockRestoreClaim(aUser, sessionDataId, Right(()))
      mockSendAudit(anUnIgnoreStateBenefitAudit.toAuditModel)

      await(underTest.restoreClaim(sessionDataId, aUser, aStateBenefitsUserData)) shouldBe Right(())
    }
  }
}
