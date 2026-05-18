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

import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

trait MockAuditConnector extends MockitoSugar {

  protected val mockAuditConnector: AuditConnector = mock[AuditConnector]

  def mockSendExtendedEvent(applicationName: String,
                            auditType: String,
                            eventTags: Map[String, String],
                            detail: JsValue,
                            result: AuditResult): Unit =
    when(mockAuditConnector.sendExtendedEvent(
      argThat[ExtendedDataEvent](e =>
        e.auditSource == applicationName &&
          e.auditType == auditType &&
          e.detail == detail &&
          e.tags == eventTags
      )
    )(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(result))
}
