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

package services

import connectors.errors.{ApiError, SingleErrorBody}
import models.errors.HttpParserError
import play.api.http.Status.INTERNAL_SERVER_ERROR
import support.UnitTest
import support.builders.IncomeTaxUserDataBuilder.anIncomeTaxUserData
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.mocks.MockStateBenefitsConnector
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class StateBenefitsServiceSpec extends UnitTest
  with MockStateBenefitsConnector
  with TaxYearProvider {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private val underTest = new StateBenefitsService(mockStateBenefitsConnector)

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
    val sessionDataId = UUID.randomUUID()
    "return error when fails to create data" in {
      mockGetUserSessionData(aUser, sessionDataId, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return correct result" in {
      mockGetUserSessionData(aUser, sessionDataId, Right(aStateBenefitsUserData))

      await(underTest.getUserSessionData(aUser, sessionDataId)) shouldBe Right(aStateBenefitsUserData)
    }
  }

  ".createOrUpdate(...)" should {
    "return error when fails to create data" in {
      mockCreateOrUpdate(aStateBenefitsUserData, Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError)))

      await(underTest.createOrUpdate(aStateBenefitsUserData)) shouldBe Left(HttpParserError(INTERNAL_SERVER_ERROR))
    }

    "return correct result" in {
      val sessionDataId = UUID.randomUUID()

      mockCreateOrUpdate(aStateBenefitsUserData, Right(sessionDataId))

      await(underTest.createOrUpdate(aStateBenefitsUserData)) shouldBe Right(sessionDataId)
    }
  }
}