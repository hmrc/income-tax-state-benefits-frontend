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

import models.errors.HttpParserError
import models.{ClaimCYAModel, StateBenefitsUserData, User}
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
                     (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val updatedClaim = stateBenefitsUserData.claim.fold(ClaimCYAModel(startDate = startDate, isHmrcData = false))(_.copy(startDate = startDate))
    createOrUpdateClaim(stateBenefitsUserData, Some(updatedClaim))
  }

  def updateEndDateQuestion(stateBenefitsUserData: StateBenefitsUserData, question: Boolean)
                           (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val endDate = if (question) stateBenefitsUserData.claim.flatMap(_.endDate) else None
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(endDateQuestion = Some(question), endDate = endDate))
    createOrUpdateClaim(stateBenefitsUserData, updatedClaim)
  }

  def updateEndDate(stateBenefitsUserData: StateBenefitsUserData,
                    endDate: LocalDate)
                   (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(endDate = Some(endDate)))
    createOrUpdateClaim(stateBenefitsUserData, updatedClaim)
  }

  def updateAmount(stateBenefitsUserData: StateBenefitsUserData,
                   amount: BigDecimal)
                  (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(amount = Some(amount)))
    createOrUpdateClaim(stateBenefitsUserData, updatedClaim)
  }

  def updateTaxPaidQuestion(stateBenefitsUserData: StateBenefitsUserData, question: Boolean)
                           (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val taxPaid = if (question) stateBenefitsUserData.claim.flatMap(_.taxPaid) else None
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(taxPaidQuestion = Some(question), taxPaid = taxPaid))
    createOrUpdateClaim(stateBenefitsUserData, updatedClaim)
  }

  def removeClaim(user: User, sessionDataId: UUID)
                 (implicit headerCarrier: HeaderCarrier): Future[Either[HttpParserError, Unit]] = {
    stateBenefitsService.removeClaim(user, sessionDataId).map {
      case Left(apiError) => Left(apiError)
      case Right(_) => Right(())
    }
  }

  def updateTaxPaidAmount(stateBenefitsUserData: StateBenefitsUserData,
                          amount: BigDecimal)
                         (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val updatedClaim = stateBenefitsUserData.claim.map(_.copy(taxPaid = Some(amount)))
    createOrUpdateClaim(stateBenefitsUserData, updatedClaim)
  }

  private def createOrUpdateClaim(originalUserData: StateBenefitsUserData, withClaim: Option[ClaimCYAModel])
                                 (implicit headerCarrier: HeaderCarrier): Future[Either[Unit, UUID]] = {
    val updatedUserData = originalUserData.copy(claim = withClaim)

    stateBenefitsService.createOrUpdate(updatedUserData).map {
      case Left(_) => Left(())
      case Right(uuid) => Right(uuid)
    }
  }
}
