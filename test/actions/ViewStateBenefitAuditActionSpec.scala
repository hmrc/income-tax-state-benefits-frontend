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

package actions

import models.audit.ViewStateBenefitAudit
import support.UnitTest
import support.builders.StateBenefitsUserDataBuilder.aStateBenefitsUserData
import support.builders.UserBuilder.aUser
import support.builders.requests.UserSessionDataRequestBuilder.aUserSessionDataRequest
import support.mocks.MockAuditService
import support.providers.TaxYearProvider

import scala.concurrent.ExecutionContext

class ViewStateBenefitAuditActionSpec extends UnitTest
  with TaxYearProvider
  with MockAuditService {

  private val executionContext = ExecutionContext.global

  private val underTest = ViewStateBenefitAuditAction(mockAuditService)(executionContext)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".filter" should {
    "send an audit event and return None" in {
      val auditEvent = ViewStateBenefitAudit(aUser.affinityGroup, aStateBenefitsUserData)

      mockSendAudit(auditEvent.toAuditModel)

      await(underTest.filter(aUserSessionDataRequest)) shouldBe None
    }
  }
}
