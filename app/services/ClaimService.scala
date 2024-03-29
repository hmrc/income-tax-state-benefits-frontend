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

import models.{ClaimCYAModel, StateBenefitsUserData}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClaimService @Inject()(stateBenefitsService: StateBenefitsService)
                            (implicit ec: ExecutionContext) {

  def updateStartDate(stateBenefitsUserData: StateBenefitsUserData, startDate: LocalDate)
                     (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, StateBenefitsUserData]] = {
    val updatedClaim = stateBenefitsUserData.claim.fold(ClaimCYAModel(startDate = startDate))(_.copy(startDate = startDate))
    updateClaim(stateBenefitsUserData, Some(updatedClaim))
  }

  def updateEndDateQuestion(stateBenefitsUserData: StateBenefitsUserData, question: Boolean)
                           (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, StateBenefitsUserData]] = {
    val endDate = if (question) stateBenefitsUserData.claim.flatMap(_.endDate) else None
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(endDateQuestion = Some(question), endDate = endDate))
    updateClaim(stateBenefitsUserData, updatedClaim)
  }

  def updateEndDate(stateBenefitsUserData: StateBenefitsUserData,
                    endDate: LocalDate)
                   (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, StateBenefitsUserData]] = {
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(endDate = Some(endDate)))
    updateClaim(stateBenefitsUserData, updatedClaim)
  }

  def updateAmount(stateBenefitsUserData: StateBenefitsUserData,
                   amount: BigDecimal)
                  (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, StateBenefitsUserData]] = {
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(amount = Some(amount)))
    updateClaim(stateBenefitsUserData, updatedClaim)
  }

  def updateTaxPaidQuestion(stateBenefitsUserData: StateBenefitsUserData, question: Boolean)
                           (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, StateBenefitsUserData]] = {
    val taxPaid = if (question) stateBenefitsUserData.claim.flatMap(_.taxPaid) else None
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(taxPaidQuestion = Some(question), taxPaid = taxPaid))
    updateClaim(stateBenefitsUserData, updatedClaim)
  }

  def updateTaxPaidAmount(stateBenefitsUserData: StateBenefitsUserData,
                          amount: BigDecimal)
                         (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, StateBenefitsUserData]] = {
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(taxPaid = Some(amount)))
    updateClaim(stateBenefitsUserData, updatedClaim)
  }

  private def updateClaim(originalUserData: StateBenefitsUserData, withClaim: Option[ClaimCYAModel])
                         (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, StateBenefitsUserData]] = {
    val updatedUserData = originalUserData.copy(claim = withClaim)
    stateBenefitsService.updateSessionData(updatedUserData).map {
      case Left(_) => Left(())
      case Right(_) => Right(updatedUserData)
    }
  }
}
