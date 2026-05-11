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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.ClaimService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

trait MockClaimService extends MockitoSugar {

  protected val mockClaimService: ClaimService = mock[ClaimService]

  def mockUpdateStartDate(stateBenefitsUserData: StateBenefitsUserData,
                          startDate: LocalDate,
                          result: Either[Unit, StateBenefitsUserData]): Unit =
    when(mockClaimService.updateStartDate(eqTo(stateBenefitsUserData), eqTo(startDate))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUpdateEndDateQuestion(stateBenefitsUserData: StateBenefitsUserData,
                                question: Boolean,
                                result: Either[Unit, StateBenefitsUserData]): Unit =
    when(mockClaimService.updateEndDateQuestion(eqTo(stateBenefitsUserData), eqTo(question))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUpdateEndDate(stateBenefitsUserData: StateBenefitsUserData,
                        endDate: LocalDate,
                        result: Either[Unit, StateBenefitsUserData]): Unit =
    when(mockClaimService.updateEndDate(eqTo(stateBenefitsUserData), eqTo(endDate))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUpdateAmount(stateBenefitsUserData: StateBenefitsUserData,
                       amount: BigDecimal,
                       result: Either[Unit, StateBenefitsUserData]): Unit =
    when(mockClaimService.updateAmount(eqTo(stateBenefitsUserData), eqTo(amount))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUpdateTaxPaidQuestion(stateBenefitsUserData: StateBenefitsUserData,
                                question: Boolean,
                                result: Either[Unit, StateBenefitsUserData]): Unit =
    when(mockClaimService.updateTaxPaidQuestion(eqTo(stateBenefitsUserData), eqTo(question))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUpdateTaxPaidAmount(stateBenefitsUserData: StateBenefitsUserData,
                              amount: BigDecimal,
                              result: Either[Unit, StateBenefitsUserData]): Unit =
    when(mockClaimService.updateTaxPaidAmount(eqTo(stateBenefitsUserData), eqTo(amount))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))
}
