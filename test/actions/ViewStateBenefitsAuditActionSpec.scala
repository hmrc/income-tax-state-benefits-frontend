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

import models.BenefitType.JobSeekersAllowance
import models.audit.ViewStateBenefitsAudit
import support.UnitTest
import support.builders.requests.UserPriorDataRequestBuilder.aUserPriorDataRequest
import support.mocks.MockAuditService
import support.providers.TaxYearProvider

import scala.concurrent.ExecutionContext

class ViewStateBenefitsAuditActionSpec extends UnitTest
  with TaxYearProvider
  with MockAuditService {

  private val executionContext = ExecutionContext.global
  private val benefitType = JobSeekersAllowance

  private val underTest = ViewStateBenefitsAuditAction(taxYearEOY, benefitType, mockAuditService)(executionContext)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".filter" should {
    "send an audit event and return None" in {
      val auditEvent = ViewStateBenefitsAudit(taxYearEOY, aUserPriorDataRequest.user, benefitType, aUserPriorDataRequest.incomeTaxUserData)

      mockSendAudit(auditEvent.toAuditModel)

      await(underTest.filter(aUserPriorDataRequest)) shouldBe None
    }
  }
}
