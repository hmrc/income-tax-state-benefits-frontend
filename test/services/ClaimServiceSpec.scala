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

import models.ClaimCYAModel
import models.errors.HttpParserError
import play.api.http.Status.BAD_REQUEST
import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.mocks.MockStateBenefitsService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class ClaimServiceSpec extends UnitTest
  with MockStateBenefitsService {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private val startDate = LocalDate.now()
  private val endDate = LocalDate.now()

  private val underTest = new ClaimService(mockStateBenefitsService)

  ".updateStartDate" should {
    "create a new claim and update startDate when no claim exists" in {
      val userData = aStateBenefitsUserData.copy(claim = None)
      val expectedClaim = ClaimCYAModel(startDate = startDate)

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateStartDate(userData, startDate)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "update claim with startDate when claim exists" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(startDate = LocalDate.of(2022, 2, 2))))
      val expectedClaim = aClaimCYAModel.copy(startDate = startDate)

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateStartDate(userData, startDate)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "return Left when updateSessionData fails" in {
      mockUpdateSessionData(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(startDate = startDate))), Left(HttpParserError(BAD_REQUEST)))

      await(underTest.updateStartDate(aStateBenefitsUserData, startDate)) shouldBe Left(())
    }
  }

  ".updateEndDateQuestion" should {
    "update claim with updateEndDateQuestion and endDate set to None when updateEndDateQuestion is false" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(endDate = Some(endDate))))
      val expectedClaim = aClaimCYAModel.copy(endDateQuestion = Some(false), endDate = None)

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateEndDateQuestion(userData, question = false)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "update claim with updateEndDateQuestion and endDate unchanged when updateEndDateQuestion is true" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(endDate = Some(endDate))))
      val expectedClaim = aClaimCYAModel.copy(endDateQuestion = Some(true), endDate = Some(endDate))

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateEndDateQuestion(userData, question = true)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "return Left when updateSessionData fails" in {
      mockUpdateSessionData(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(endDateQuestion = Some(true)))), Left(HttpParserError(BAD_REQUEST)))

      await(underTest.updateEndDateQuestion(aStateBenefitsUserData, question = true)) shouldBe Left(())
    }
  }

  ".updateEndDate" should {
    "update claim with endDate when claim exists" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(endDate = Some(LocalDate.of(2022, 2, 2)))))
      val expectedClaim = aClaimCYAModel.copy(endDate = Some(endDate))

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateEndDate(userData, endDate)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "return Left when updateSessionData fails" in {
      mockUpdateSessionData(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(endDate = Some(endDate)))), Left(HttpParserError(BAD_REQUEST)))

      await(underTest.updateEndDate(aStateBenefitsUserData, endDate)) shouldBe Left(())
    }
  }

  ".updateAmount" should {
    "update claim with amount when claim exists" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(amount = Some(100))))
      val expectedClaim = aClaimCYAModel.copy(amount = Some(200))

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateAmount(userData, 200)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "return Left when updateSessionData fails" in {
      mockUpdateSessionData(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(amount = Some(100)))), Left(HttpParserError(BAD_REQUEST)))

      await(underTest.updateAmount(aStateBenefitsUserData, amount = 100)) shouldBe Left(())
    }
  }

  ".updateTaxPaidQuestion" should {
    "update claim with updateTaxPaidQuestion and taxPaid set to None when updateTaxPaidQuestion is false" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaid = Some(100))))
      val expectedClaim = aClaimCYAModel.copy(taxPaidQuestion = Some(false), taxPaid = None)

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateTaxPaidQuestion(userData, question = false)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "update claim with updateTaxPaidQuestion and taxPaid unchanged when updateTaxPaidQuestion is true" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaid = Some(100))))
      val expectedClaim = aClaimCYAModel.copy(taxPaidQuestion = Some(true), taxPaid = Some(100))

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateEndDateQuestion(userData, question = true)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "return Left when updateSessionData fails" in {
      mockUpdateSessionData(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaidQuestion = Some(true)))), Left(HttpParserError(BAD_REQUEST)))

      await(underTest.updateEndDateQuestion(aStateBenefitsUserData, question = true)) shouldBe Left(())
    }
  }

  ".updateTaxPaidAmount" should {
    "update claim with amount when claim exists" in {
      val userData = aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaid = Some(100))))
      val expectedClaim = aClaimCYAModel.copy(taxPaid = Some(200))

      mockUpdateSessionData(userData.copy(claim = Some(expectedClaim)), Right(()))

      await(underTest.updateTaxPaidAmount(userData, 200)) shouldBe Right(aStateBenefitsUserData.copy(claim = Some(expectedClaim)))
    }

    "return Left when updateSessionData fails" in {
      mockUpdateSessionData(aStateBenefitsUserData.copy(claim = Some(aClaimCYAModel.copy(taxPaid = Some(100)))), Left(HttpParserError(BAD_REQUEST)))

      await(underTest.updateTaxPaidAmount(aStateBenefitsUserData, amount = 100)) shouldBe Left(())
    }
  }
}
