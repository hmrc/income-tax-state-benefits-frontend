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

import models.authorisation.Enrolment.{Agent, Individual, Nino}
import org.scalamock.handlers.CallHandler4
import org.scalatest.TestSuite
import services.AuthorisationService
import support.builders.UserBuilder.{aUser, anAgentUser}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthorisationService extends MockAuthConnector { _: TestSuite =>

  protected val authorisationService: AuthorisationService = new AuthorisationService(mockAuthConnector)

  def mockAuthAsAgent(): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    val agentRetrievals: Some[AffinityGroup] = Some(AffinityGroup.Agent)
    val enrolments: Enrolments = Enrolments(Set(
      Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, aUser.mtditid)), "Activated"),
      Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, anAgentUser.arn.get)), "Activated")
    ))

    mockAuthorise(Retrievals.affinityGroup, agentRetrievals)
    mockAuthorise(Retrievals.allEnrolments, enrolments)
  }

  def mockAuth(nino: Option[String]): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    val enrolments = Enrolments(Set(
      Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, "1234567890")), "Activated"),
      Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, "0987654321")), "Activated")
    ) ++ nino.fold(Seq.empty[Enrolment])(unwrappedNino =>
      Seq(Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, unwrappedNino)), "Activated"))
    ))

    mockAuthorise(Retrievals.affinityGroup, Some(AffinityGroup.Individual))
    mockAuthorise(Retrievals.allEnrolments and Retrievals.confidenceLevel, enrolments and ConfidenceLevel.L250)
  }
}
