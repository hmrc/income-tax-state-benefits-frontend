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

import connectors.StateBenefitsConnector
import connectors.errors.ApiError
import models.{IncomeTaxUserData, StateBenefitsUserData, User}
import org.scalamock.handlers.{CallHandler2, CallHandler3}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.Future

trait MockStateBenefitsConnector extends MockFactory { _: TestSuite =>

  protected val mockStateBenefitsConnector: StateBenefitsConnector = mock[StateBenefitsConnector]

  def mockGetIncomeTaxUserData(user: User,
                               taxYear: Int,
                               result: Either[ApiError, IncomeTaxUserData]): CallHandler3[User, Int, HeaderCarrier, Future[Either[ApiError, IncomeTaxUserData]]] = {
    (mockStateBenefitsConnector.getIncomeTaxUserData(_: User, _: Int)(_: HeaderCarrier))
      .expects(user, taxYear, *)
      .returning(Future.successful(result))
  }

  def mockGetUserSessionData(user: User,
                             sessionDataId: UUID,
                             result: Either[ApiError, StateBenefitsUserData]): CallHandler3[User, UUID, HeaderCarrier, Future[Either[ApiError, StateBenefitsUserData]]] = {
    (mockStateBenefitsConnector.getUserSessionData(_: User, _: UUID)(_: HeaderCarrier))
      .expects(user, sessionDataId, *)
      .returning(Future.successful(result))
  }

  def mockCreateSessionData(stateBenefitsUserData: StateBenefitsUserData,
                            result: Either[ApiError, UUID]): CallHandler2[StateBenefitsUserData, HeaderCarrier, Future[Either[ApiError, UUID]]] = {
    (mockStateBenefitsConnector.createSessionData(_: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }

  def mockUpdateSessionData(stateBenefitsUserData: StateBenefitsUserData,
                            result: Either[ApiError, Unit]): CallHandler2[StateBenefitsUserData, HeaderCarrier, Future[Either[ApiError, Unit]]] = {
    (mockStateBenefitsConnector.updateSessionData(_: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }

  def mockSaveClaim(stateBenefitsUserData: StateBenefitsUserData,
                    result: Either[ApiError, Unit]): CallHandler2[StateBenefitsUserData, HeaderCarrier, Future[Either[ApiError, Unit]]] = {
    (mockStateBenefitsConnector.saveClaim(_: StateBenefitsUserData)(_: HeaderCarrier))
      .expects(stateBenefitsUserData, *)
      .returning(Future.successful(result))
  }

  def mockRemoveClaim(user: User,
                      sessionDataId: UUID,
                      result: Either[ApiError, Unit]): CallHandler3[User, UUID, HeaderCarrier, Future[Either[ApiError, Unit]]] = {
    (mockStateBenefitsConnector.removeClaim(_: User, _: UUID)(_: HeaderCarrier))
      .expects(user, sessionDataId, *)
      .returning(Future.successful(result))
  }

  def mockRestoreClaim(user: User,
                       sessionDataId: UUID,
                       result: Either[ApiError, Unit]): CallHandler3[User, UUID, HeaderCarrier, Future[Either[ApiError, Unit]]] = {
    (mockStateBenefitsConnector.restoreClaim(_: User, _: UUID)(_: HeaderCarrier))
      .expects(user, sessionDataId, *)
      .returning(Future.successful(result))
  }
}
