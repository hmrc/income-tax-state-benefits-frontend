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

package support.mocks

import models.StateBenefitsUserData
import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.ClaimService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

trait MockClaimService extends MockFactory { _: TestSuite =>

  protected val mockClaimService: ClaimService = mock[ClaimService]

  def mockUpdateStartDate(stateBenefitsUserData: StateBenefitsUserData,
                          startDate: LocalDate,
                          result: Either[Unit, StateBenefitsUserData]): CallHandler3[StateBenefitsUserData, LocalDate, HeaderCarrier, Future[Either[Unit, StateBenefitsUserData]]] = {
    (mockClaimService.updateStartDate(_: StateBenefitsUserData, _: LocalDate)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, startDate, *)
      .returning(Future.successful(result))
  }

  def mockUpdateEndDateQuestion(stateBenefitsUserData: StateBenefitsUserData,
                                question: Boolean,
                                result: Either[Unit, StateBenefitsUserData]): CallHandler3[StateBenefitsUserData, Boolean, HeaderCarrier, Future[Either[Unit, StateBenefitsUserData]]] = {
    (mockClaimService.updateEndDateQuestion(_: StateBenefitsUserData, _: Boolean)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, question, *)
      .returning(Future.successful(result))
  }

  def mockUpdateEndDate(stateBenefitsUserData: StateBenefitsUserData,
                        endDate: LocalDate,
                        result: Either[Unit, StateBenefitsUserData]): CallHandler3[StateBenefitsUserData, LocalDate, HeaderCarrier, Future[Either[Unit, StateBenefitsUserData]]] = {
    (mockClaimService.updateEndDate(_: StateBenefitsUserData, _: LocalDate)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, endDate, *)
      .returning(Future.successful(result))
  }

  def mockUpdateAmount(stateBenefitsUserData: StateBenefitsUserData,
                       amount: BigDecimal,
                       result: Either[Unit, StateBenefitsUserData]): CallHandler3[StateBenefitsUserData, BigDecimal, HeaderCarrier, Future[Either[Unit, StateBenefitsUserData]]] = {
    (mockClaimService.updateAmount(_: StateBenefitsUserData, _: BigDecimal)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, amount, *)
      .returning(Future.successful(result))
  }

  def mockUpdateTaxPaidQuestion(stateBenefitsUserData: StateBenefitsUserData,
                                question: Boolean,
                                result: Either[Unit, StateBenefitsUserData]): CallHandler3[StateBenefitsUserData, Boolean, HeaderCarrier, Future[Either[Unit, StateBenefitsUserData]]] = {
    (mockClaimService.updateTaxPaidQuestion(_: StateBenefitsUserData, _: Boolean)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, question, *)
      .returning(Future.successful(result))
  }

  def mockUpdateTaxPaidAmount(stateBenefitsUserData: StateBenefitsUserData,
                              amount: BigDecimal,
                              result: Either[Unit, StateBenefitsUserData]): CallHandler3[StateBenefitsUserData, BigDecimal, HeaderCarrier, Future[Either[Unit, StateBenefitsUserData]]] = {
    (mockClaimService.updateTaxPaidAmount(_: StateBenefitsUserData, _: BigDecimal)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, amount, *)
      .returning(Future.successful(result))
  }
}
