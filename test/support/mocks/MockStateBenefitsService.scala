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

import models.errors.HttpParserError
import models.{BenefitType, IncomeTaxUserData, StateBenefitsUserData, User}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.StateBenefitsService
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.Future

trait MockStateBenefitsService extends MockitoSugar {

  protected val mockStateBenefitsService: StateBenefitsService = mock[StateBenefitsService]

  def mockGetPriorData(user: User,
                       taxYear: Int,
                       result: Either[HttpParserError, IncomeTaxUserData]): Unit =
    when(mockStateBenefitsService.getPriorData(eqTo(user), eqTo(taxYear))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockGetUserSessionData(user: User,
                             sessionDataId: UUID,
                             result: Either[HttpParserError, StateBenefitsUserData]): Unit =
    when(mockStateBenefitsService.getUserSessionData(eqTo(user), eqTo(sessionDataId))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockCreateSessionData(stateBenefitsUserData: StateBenefitsUserData,
                            result: Either[HttpParserError, UUID]): Unit =
    when(mockStateBenefitsService.createSessionData(eqTo(stateBenefitsUserData))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUpdateSessionData(stateBenefitsUserData: StateBenefitsUserData,
                            result: Either[HttpParserError, Unit]): Unit =
    when(mockStateBenefitsService.updateSessionData(eqTo(stateBenefitsUserData))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockSaveClaim(user: User,
                    benefitType: BenefitType,
                    stateBenefitsUserData: StateBenefitsUserData,
                    result: Either[HttpParserError, Unit]): Unit =
    when(mockStateBenefitsService.saveClaim(eqTo(user), eqTo(benefitType), eqTo(stateBenefitsUserData))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockRemoveClaim(sessionDataId: UUID,
                      user: User,
                      stateBenefitsUserData: StateBenefitsUserData,
                      result: Either[HttpParserError, Unit]): Unit =
    when(mockStateBenefitsService.removeClaim(eqTo(sessionDataId), eqTo(user), eqTo(stateBenefitsUserData))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockRestoreClaim(user: User,
                       sessionDataId: UUID,
                       stateBenefitsUserData: StateBenefitsUserData,
                       result: Either[HttpParserError, Unit]): Unit =
    when(mockStateBenefitsService.restoreClaim(eqTo(sessionDataId), eqTo(user), eqTo(stateBenefitsUserData))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))
}
