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

import connectors.StateBenefitsConnector
import connectors.errors.ApiError
import models._
import models.errors.HttpParserError
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StateBenefitsService @Inject()(stateBenefitsConnector: StateBenefitsConnector)
                                    (implicit val ec: ExecutionContext) extends Logging {

  def getPriorData(user: User, taxYear: Int)
                  (implicit hc: HeaderCarrier): Future[Either[HttpParserError, IncomeTaxUserData]] = {
    stateBenefitsConnector.getIncomeTaxUserData(user = user, taxYear = taxYear).map {
      case Left(error: ApiError) => Left(HttpParserError(error.status))
      case Right(incomeTaxUserData) => Right(incomeTaxUserData)
    }
  }

  def getUserSessionData(user: User, sessionDataId: UUID)
                        (implicit hc: HeaderCarrier): Future[Either[HttpParserError, StateBenefitsUserData]] = {
    stateBenefitsConnector.getUserSessionData(user, sessionDataId).map {
      case Left(error: ApiError) => Left(HttpParserError(error.status))
      case Right(stateBenefitsUserData) => Right(stateBenefitsUserData)
    }
  }

  def createOrUpdate(stateBenefitsUserData: StateBenefitsUserData)
                    (implicit hc: HeaderCarrier): Future[Either[HttpParserError, UUID]] = {
    stateBenefitsConnector.createOrUpdate(stateBenefitsUserData).map {
      case Left(error: ApiError) => Left(HttpParserError(error.status))
      case Right(uuid) => Right(uuid)
    }
  }

  def saveStateBenefit(stateBenefitsUserData: StateBenefitsUserData)
                      (implicit hc: HeaderCarrier): Future[Either[HttpParserError, Unit]] = {
    stateBenefitsConnector.saveStateBenefit(stateBenefitsUserData).map {
      case Left(error: ApiError) => Left(HttpParserError(error.status))
      case Right(_) => Right(())
    }
  }

  def removeClaim(user: User, sessionDataId: UUID)
                 (implicit hc: HeaderCarrier): Future[Either[HttpParserError, Unit]] = {
    stateBenefitsConnector.removeClaim(user, sessionDataId).map {
      case Left(error: ApiError) => Left(HttpParserError(error.status))
      case Right(_) => Right(())
    }
  }
}
