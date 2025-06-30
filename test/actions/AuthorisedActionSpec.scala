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

import models.authorisation.Enrolment.{Agent, Individual, Nino, SupportingAgent}
import models.authorisation.SessionValues
import models.authorisation.SessionValues.{CLIENT_MTDITID, CLIENT_NINO}
import models.errors.MissingAgentClientDetails
import models.requests.AuthorisationRequest
import models.session.UserSessionData
import org.apache.pekko.actor.ActorSystem
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import play.api.http.{HeaderNames, Status => TestStatus}
import play.api.mvc.Results.{InternalServerError, Ok}
import play.api.mvc._
import play.api.test.{FakeRequest, ResultExtractors}
import support.ControllerUnitTest
import support.builders.UserBuilder.aUser
import support.mocks.{MockAppConfig, MockAuthorisationService, MockErrorHandler, MockSessionDataService}
import support.providers.FakeRequestProvider
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedActionSpec extends ControllerUnitTest
  with FakeRequestProvider
  with MockAuthorisationService
  with MockFactory
  with MockSessionDataService
  with MockErrorHandler
  with MockAppConfig
  with TestStatus with HeaderNames with ResultExtractors {

  private implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(aUser.sessionId)))
  private val executionContext = ExecutionContext.global
  implicit val actorSystem: ActorSystem = ActorSystem()

  val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  private val nino = "AA123456A"
  private val mtdItId = "1234567890"
  private val arn: String = "0987654321"

  val sessionData: UserSessionData = UserSessionData(aUser.sessionId, mtdItId, nino)

  private val fakeRequestWithMtditidAndNino: FakeRequest[AnyContentAsEmpty.type] = fakeAgentRequest
    .withHeaders(newHeaders = "X-Session-ID" -> aUser.sessionId)
    .withSession(CLIENT_MTDITID -> mtdItId, CLIENT_NINO -> nino)

  private val enrolments = Enrolments(Set(
    Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, aUser.mtditid)), "Activated"),
    Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, aUser.nino)), "Activated")
  ))

  private val underTest = new AuthorisedAction(mockErrorHandler, mockSessionDataService)(authorisationService, appConfig, mcc)

  def bodyOf(awaitable: Future[Result]): String = {
    val awaited = await(awaitable)
    await(awaited.body.consumeData.map(_.utf8String))
  }
  trait AgentTest {
    val validHeaderCarrier: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionId")))

    val testBlock: AuthorisationRequest[AnyContent] => Future[Result] = user => Future.successful(Ok(s"${user.user.mtditid} ${user.user.arn.get}"))

    def redirectUrl(awaitable: Future[Result]): String = {
      await(awaitable).header.headers.getOrElse("Location", "/")
    }

    def primaryAgentPredicate(mtdId: String): Predicate =
      Enrolment("HMRC-MTD-IT")
        .withIdentifier("MTDITID", mtdId)
        .withDelegatedAuthRule("mtd-it-auth")

    def secondaryAgentPredicate(mtdId: String): Predicate =
      Enrolment("HMRC-MTD-IT-SUPP")
        .withIdentifier("MTDITID", mtdId)
        .withDelegatedAuthRule("mtd-it-auth-supp")


    val primaryAgentEnrolment: Enrolments = Enrolments(Set(
      Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, mtdItId)), "Activated"),
      Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, arn)), "Activated")
    ))

    val supportingAgentEnrolment: Enrolments = Enrolments(Set(
      Enrolment(SupportingAgent.key, Seq(EnrolmentIdentifier(Individual.value, mtdItId)), "Activated"),
      Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, arn)), "Activated")
    ))

    def mockAuthReturnException(exception: Exception,
                                predicate: Predicate): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] =
      (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *, *)
        .returning(Future.failed(exception))

    def mockAuthReturn(enrolments: Enrolments, predicate: Predicate): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] =
      (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *, *)
        .returning(Future.successful(enrolments))

    def testAuth: AuthorisedAction = {
      mockViewAndChangeUrl()
      mockSignInUrl()

      new AuthorisedAction(
        mockErrorHandler,
        mockSessionDataService
      )(authService = authorisationService,
        mockAppConfig,
        cc = stubMessagesControllerComponents())
    }

    lazy val fakeRequestWithMtditidAndNino: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(
      SessionValues.TAX_YEAR -> "2022",
      SessionValues.CLIENT_MTDITID -> mtdItId,
      SessionValues.CLIENT_NINO -> nino
    )
  }

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".parser" should {
    "return default parser from the ControllerComponents" in {
      underTest.parser shouldBe a[BodyParser[_]]
    }
  }

  ".enrolmentGetIdentifierValue" should {
    "return the value for the given identifier" in {
      val returnValue = "anIdentifierValue"
      val returnValueAgent = "anAgentIdentifierValue"
      val enrolments = Enrolments(Set(
        Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, returnValue)), "Activated"),
        Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, returnValueAgent)), "Activated")
      ))

      underTest.enrolmentGetIdentifierValue(Individual.key, Individual.value, enrolments) shouldBe Some(returnValue)
      underTest.enrolmentGetIdentifierValue(Agent.key, Agent.value, enrolments) shouldBe Some(returnValueAgent)
    }

    "return a None" when {
      val key = "someKey"
      val identifierKey = "anIdentifier"
      val returnValue = "anIdentifierValue"
      val enrolments = Enrolments(Set(Enrolment(key, Seq(EnrolmentIdentifier(identifierKey, returnValue)), "someState")))

      "the given identifier cannot be found" in {
        underTest.enrolmentGetIdentifierValue(key, "someOtherIdentifier", enrolments) shouldBe None
      }

      "the given key cannot be found" in {
        underTest.enrolmentGetIdentifierValue("someOtherKey", identifierKey, enrolments) shouldBe None
      }
    }
  }

  ".individualAuthentication" should {
    "perform the block action" when {
      "the correct enrolment exist" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual, aUser.sessionId)(fakeIndividualRequest, headerCarrierWithSession))

        "returns an OK status" in {
          result.header.status shouldBe OK
        }

        "returns a body of the mtditid" in {
          await(result.body.consumeData.map(_.utf8String)) shouldBe aUser.mtditid
        }
      }
    }

    "return a redirect" when {

      "the nino enrolment is missing" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set())

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual, aUser.sessionId)(fakeIndividualRequest, headerCarrierWithSession))

        "returns a forbidden" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the individual enrolment is missing but there is a nino" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set(Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, aUser.nino)), "Activated")))

        lazy val result = {
          mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)
          await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual, aUser.sessionId)(fakeIndividualRequest, headerCarrierWithSession))
        }

        "returns an Unauthorised" in {
          result.header.status shouldBe SEE_OTHER
        }

        "returns a redirect to the correct page" in {
          result.header.headers.getOrElse("Location", "/") shouldBe controllers.errors.routes.IndividualAuthErrorController.show.url
        }
      }
    }

    "return the user to IV Uplift" when {
      "the confidence level is below minimum" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L50)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual, aUser.sessionId)(fakeIndividualRequest, headerCarrierWithSession))

        "has a status of 303" in {
          result.header.status shouldBe SEE_OTHER
        }

        "redirects to the iv url" in {
          result.header.headers("Location") shouldBe "/update-and-submit-income-tax-return/iv-uplift"
        }
      }
    }
  }

  ".agentAuthenticated" when {
    "session data for Client MTDITID and/or NINO is missing" should {
      "return a redirect to View and Change service" in new AgentTest {
        mockGetSessionDataException(aUser.sessionId)(MissingAgentClientDetails("No session data"))

        val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
          request = FakeRequest().withSession(fakeRequest.session.data.toSeq :_*),
          hc = emptyHeaderCarrier
        )

        status(result) shouldBe SEE_OTHER
        redirectUrl(result) shouldBe viewAndChangeUrl
      }
    }

    "session data for Client NINO and MTD IT ID are present" which {
      "results in a NoActiveSession error to be returned from Auth" should {
        "return a redirect to the login page" in new AgentTest {
          mockGetSessionData(aUser.sessionId)(sessionData)
          object AuthException extends NoActiveSession("Some reason")
          mockAuthReturnException(AuthException, primaryAgentPredicate(mtdItId))

          val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/signIn"
        }
      }

      "results in an Exception other than an AuthException error being returned for Primary Agent check" should {
        "render an ISE page" in new AgentTest {

          mockGetSessionData(aUser.sessionId)(sessionData)
          mockAuthReturnException(new Exception("bang"), primaryAgentPredicate(mtdItId))
          mockInternalServerError(InternalServerError("An unexpected error occurred"))

          val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq: _*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe INTERNAL_SERVER_ERROR
          bodyOf(result) shouldBe "An unexpected error occurred"
        }
      }

      "results in an AuthorisationException error being returned from Auth" should {

        "render an ISE page when secondary agent auth call also fails with non-Auth exception" in new AgentTest {

          mockGetSessionData(aUser.sessionId)(sessionData)
          mockAuthReturnException(InsufficientEnrolments(), primaryAgentPredicate(mtdItId))
          mockAuthReturnException(new Exception("bang"), secondaryAgentPredicate(mtdItId))
          mockInternalServerError(InternalServerError("An unexpected error occurred"))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq: _*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe INTERNAL_SERVER_ERROR
          bodyOf(result) shouldBe "An unexpected error occurred"
        }

        "return a redirect to the agent error page when secondary agent auth call also fails due to insufficient enrolments" in new AgentTest {

          mockGetSessionData(aUser.sessionId)(sessionData)
          mockAuthReturnException(InsufficientEnrolments(), primaryAgentPredicate(mtdItId))
          mockAuthReturnException(InsufficientEnrolments(), secondaryAgentPredicate(mtdItId))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq: _*),
            hc = emptyHeaderCarrier
          )
          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/error/you-need-client-authorisation"
        }

        "return a redirect to the agent error page when secondary agent auth call also fails" in new AgentTest {

          mockGetSessionData(aUser.sessionId)(sessionData)
          object AuthException extends AuthorisationException("Some reason")
          mockAuthReturnException(AuthException, primaryAgentPredicate(mtdItId))
          mockAuthReturnException(AuthException, secondaryAgentPredicate(mtdItId))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = emptyHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/error/you-need-client-authorisation"
        }

        "return a redirect to the secondary agent not authorised page when a supporting agent has correct credentials" in new AgentTest {

          mockGetSessionData(aUser.sessionId)(sessionData)
          object AuthException extends AuthorisationException("Some reason")
          mockAuthReturnException(AuthException, primaryAgentPredicate(mtdItId))
          mockAuthReturn(supportingAgentEnrolment, secondaryAgentPredicate(mtdItId))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = validHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/error/supporting-agent-not-authorised"
        }
      }

      "results in successful authorisation for a primary agent" should {
        "return a redirect to You Need Agent Services page when an ARN cannot be found" in new AgentTest {

          mockGetSessionData(aUser.sessionId)(sessionData)
          val primaryAgentEnrolmentNoArn: Enrolments = Enrolments(Set(
            Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, mtdItId)), "Activated"),
            Enrolment(Agent.key, Seq.empty, "Activated")
          ))

          mockAuthReturn(primaryAgentEnrolmentNoArn, primaryAgentPredicate(mtdItId))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = validHeaderCarrier
          )

          status(result) shouldBe SEE_OTHER
          redirectUrl(result) shouldBe s"$baseUrl/error/you-need-agent-services-account"
        }

        "invoke block when the user is properly authenticated" in new AgentTest {
          mockGetSessionData(aUser.sessionId)(sessionData)
          mockAuthReturn(primaryAgentEnrolment, primaryAgentPredicate(mtdItId))

          lazy val result: Future[Result] = testAuth.agentAuthentication(testBlock, aUser.sessionId)(
            request = FakeRequest().withSession(fakeRequestWithMtditidAndNino.session.data.toSeq :_*),
            hc = validHeaderCarrier
          )

          status(result) shouldBe OK
          contentAsString(result) shouldBe s"$mtdItId $arn"
        }
      }
    }
  }

  ".invokeBlock" should {
    lazy val block: AuthorisationRequest[AnyContent] => Future[Result] = request =>
      Future.successful(Ok(s"mtditid: ${request.user.mtditid}${request.user.arn.fold("")(arn => " arn: " + arn)}"))

    "perform the block action" when {
      "the user is successfully verified as an agent" which {
        lazy val result = {
          mockGetSessionData(aUser.sessionId)(sessionData)
          mockAuthAsAgent()
          await(underTest.invokeBlock(fakeRequestWithMtditidAndNino, block))
        }

        "should return an OK(200) status" in {
          result.header.status shouldBe OK
          await(result.body.consumeData.map(_.utf8String)) shouldBe "mtditid: 1234567890 arn: 0987654321"
        }
      }

      "the user is successfully verified as an individual" in {
        lazy val result = {
          mockAuth(Some(nino))
          await(underTest.invokeBlock(fakeIndividualRequest, block))
        }

        result.header.status shouldBe OK
        await(result.body.consumeData.map(_.utf8String)) shouldBe "mtditid: 1234567890"
      }
    }

    "return a redirect" when {
      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(AuthException))

          underTest.invokeBlock(fakeAgentRequest, block)
        }

        status(result) shouldBe SEE_OTHER
      }

      "render ISE" when {
        "an unexpected exception is caught that is not related to Authorisation" in {

          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(new Exception("bang")))

          mockInternalServerError()
          val result = underTest.invokeBlock(fakeAgentRequest, block)

          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "there is no MTDITID value in session" in {
        val fakeRequestWithNino = fakeIndividualRequest.withSession(CLIENT_NINO -> nino)
        lazy val result = {
          mockGetSessionDataException(aUser.sessionId)(MissingAgentClientDetails("No session data"))

          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.affinityGroup, *, *)
            .returning(Future.successful(Some(AffinityGroup.Agent)))

          underTest.invokeBlock(fakeRequestWithNino, block)
        }

        status(result) shouldBe SEE_OTHER
        await(result).header.headers.getOrElse("Location", "/") shouldBe "/report-quarterly/income-and-expenses/view/agents/client-utr"
      }
    }

    "redirect to the sign in page" when {
      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(NoActiveSession))
          underTest.invokeBlock(fakeIndividualRequest, block)
        }

        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
