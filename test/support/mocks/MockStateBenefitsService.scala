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
import org.scalamock.handlers.{CallHandler2, CallHandler3, CallHandler4}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.StateBenefitsService
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.Future

trait MockStateBenefitsService extends MockFactory { _: TestSuite =>

  protected val mockStateBenefitsService: StateBenefitsService = mock[StateBenefitsService]

  def mockGetPriorData(user: User,
                       taxYear: Int,
                       result: Either[HttpParserError, IncomeTaxUserData]): CallHandler3[User, Int, HeaderCarrier, Future[Either[HttpParserError, IncomeTaxUserData]]] = {
    (mockStateBenefitsService.getPriorData(_: User, _: Int)(_: HeaderCarrier))
      .expects(user, taxYear, *)
      .returning(Future.successful(result))
  }

  def mockGetUserSessionData(user: User,
                             sessionDataId: UUID,
                             result: Either[HttpParserError, StateBenefitsUserData]): CallHandler3[User, UUID, HeaderCarrier, Future[Either[HttpParserError, StateBenefitsUserData]]] = {
    (mockStateBenefitsService.getUserSessionData(_: User, _: UUID)(_: HeaderCarrier))
      .expects(user, sessionDataId, *)
      .returning(Future.successful(result))
  }

  def mockCreateSessionData(stateBenefitsUserData: StateBenefitsUserData,
                            result: Either[HttpParserError, UUID]): CallHandler2[StateBenefitsUserData, HeaderCarrier, Future[Either[HttpParserError, UUID]]] = {
    (mockStateBenefitsService.createSessionData(_: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }

  def mockUpdateSessionData(stateBenefitsUserData: StateBenefitsUserData,
                            result: Either[HttpParserError, Unit]): CallHandler2[StateBenefitsUserData, HeaderCarrier, Future[Either[HttpParserError, Unit]]] = {
    (mockStateBenefitsService.updateSessionData(_: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }

  def mockSaveClaim(user: User,
                    benefitType: BenefitType,
                    stateBenefitsUserData: StateBenefitsUserData,
                    result: Either[HttpParserError, Unit]): CallHandler4[User, BenefitType, StateBenefitsUserData, HeaderCarrier, Future[Either[HttpParserError, Unit]]] = {
    (mockStateBenefitsService.saveClaim(_: User, _: BenefitType, _: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(user, benefitType, stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }

  def mockRemoveClaim(sessionDataId: UUID,
                      user: User,
                      stateBenefitsUserData: StateBenefitsUserData,
                      result: Either[HttpParserError, Unit]): CallHandler4[UUID, User, StateBenefitsUserData, HeaderCarrier, Future[Either[HttpParserError, Unit]]] = {
    (mockStateBenefitsService.removeClaim(_: UUID, _: User, _: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(sessionDataId, user, stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }

  def mockRestoreClaim(user: User,
                       sessionDataId: UUID,
                       stateBenefitsUserData: StateBenefitsUserData,
                       result: Either[HttpParserError, Unit]): CallHandler4[UUID, User, StateBenefitsUserData, HeaderCarrier, Future[Either[HttpParserError, Unit]]] = {
    (mockStateBenefitsService.restoreClaim(_: UUID, _: User, _: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(sessionDataId, user, stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }
}
