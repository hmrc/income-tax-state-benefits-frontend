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

import models.audit.AuditModel
import play.api.libs.json.Json
import support.UnitTest
import support.mocks.{MockAuditConnector, MockConfiguration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends UnitTest
  with MockAuditConnector
  with MockConfiguration {

  private val auditType = "some-audit-type"
  private val transactionName = "some-transaction-name"
  private val eventDetails = "some-event-details"
  private val appName = "some-app-name"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val underTest = new AuditService(mockAuditConnector, mockConfiguration)

  ".sendAudit" should {
    "extendEvent and send to auditConnector" in {
      val event = AuditModel(auditType, transactionName, eventDetails)
      val eventTags = AuditExtensions.auditHeaderCarrier(headerCarrier).toAuditTags() + ("transactionName" -> event.transactionName)

      mockGet(appName)
      mockSendExtendedEvent(appName, auditType, eventTags, Json.toJson(event.detail), AuditResult.Success)

      await(underTest.sendAudit(event)) shouldBe AuditResult.Success
    }
  }
}
