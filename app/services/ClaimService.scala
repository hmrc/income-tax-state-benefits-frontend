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

import models.{ClaimCYAModel, StateBenefitsUserData}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClaimService @Inject()(stateBenefitsService: StateBenefitsService)
                            (implicit ec: ExecutionContext) {

  def updateStartDate(stateBenefitsUserData: StateBenefitsUserData,
                      startDate: LocalDate)
                     (implicit hc: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val updatedClaim = stateBenefitsUserData.claim.fold(ClaimCYAModel(startDate = startDate))(_.copy(startDate = startDate))
    val updatedUserData = stateBenefitsUserData.copy(claim = Some(updatedClaim))

    stateBenefitsService.createOrUpdate(updatedUserData).map {
      case Left(_) => Left(())
      case Right(uuid) => Right(uuid)
    }
  }

  def updateEndDate(stateBenefitsUserData: StateBenefitsUserData,
                    endDate: LocalDate)
                   (implicit hc: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(endDate = Some(endDate)))
    val updatedUserData = stateBenefitsUserData.copy(claim = updatedClaim)

    stateBenefitsService.createOrUpdate(updatedUserData).map {
      case Left(_) => Left(())
      case Right(uuid) => Right(uuid)
    }
  }
}
