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

import actions.AuthorisedAction
import models.authorisation.Enrolment.{Agent, Individual, Nino}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.stubMessagesControllerComponents
import services.AuthorisationService
import support.builders.UserBuilder.aUser
import support.providers.AppConfigStubProvider
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthorisedAction extends AppConfigStubProvider
  with MockitoSugar with MockErrorHandler with MockSessionDataService {

  private val mockAuthConnector = mock[AuthConnector]
  private val mockAuthService = new AuthorisationService(mockAuthConnector)

  protected val mockAuthorisedAction: AuthorisedAction = new AuthorisedAction(
    mockErrorHandler,
    mockSessionDataService
  )(mockAuthService, appConfigStub, stubMessagesControllerComponents())

  protected def mockAuthAsAgent(): Unit = {
    val enrolments: Enrolments = Enrolments(Set(
      Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, aUser.mtditid)), "Activated"),
      Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, "0987654321")), "Activated")
    ))

    val agentRetrievals: Some[AffinityGroup] = Some(AffinityGroup.Agent)

    when(mockAuthConnector.authorise(any[Predicate](), eqTo(Retrievals.affinityGroup))(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(agentRetrievals))

    when(mockAuthConnector.authorise(any[Predicate](), eqTo(Retrievals.allEnrolments))(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(enrolments))
  }

  protected def mockAuthAsIndividual(nino: Option[String]): Unit = {
    val enrolments = Enrolments(Set(
      Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, aUser.mtditid)), "Activated"),
      Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, "0987654321")), "Activated")
    ) ++ nino.fold(Seq.empty[Enrolment])(unwrappedNino =>
      Seq(Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, unwrappedNino)), "Activated"))
    ))

    when(mockAuthConnector.authorise(any[Predicate](), eqTo(Retrievals.affinityGroup))(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(Some(AffinityGroup.Individual)))

    when(mockAuthConnector.authorise(any[Predicate](), eqTo(Retrievals.allEnrolments and Retrievals.confidenceLevel))(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(enrolments and ConfidenceLevel.L250))
  }

  protected def mockFailToAuthenticate(): Unit =
    when(mockAuthConnector.authorise(any[Predicate](), any[Retrieval[_]]())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.failed(InsufficientConfidenceLevel()))
}
