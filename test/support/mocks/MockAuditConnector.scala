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

import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

trait MockAuditConnector extends MockFactory { _: TestSuite =>

  protected val mockAuditConnector: AuditConnector = mock[AuditConnector]

  def mockSendExtendedEvent(applicationName: String,
                            auditType: String,
                            eventTags: Map[String, String],
                            detail: JsValue,
                            result: AuditResult): CallHandler3[ExtendedDataEvent, HeaderCarrier, ExecutionContext, Future[AuditResult]] = {
    (mockAuditConnector.sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
      .expects(
        where {
          (eventArg: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
            eventArg.auditSource == applicationName &&
              eventArg.auditType == auditType &&
              eventArg.detail == detail &&
              eventArg.tags == eventTags
        }
      )
      .returning(Future.successful(result))
  }
}
