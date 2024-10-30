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

package controllers

import config.AppConfig
import forms.YesNoForm
import models.BenefitType.JobSeekersAllowance
import models.authorisation.SessionValues.TAX_YEAR
import models.mongo.{JourneyAnswers, JourneyStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, contentAsString, defaultAwaitTimeout, redirectLocation, route, running, status, writeableOf_AnyContentAsEmpty}
import services.SectionCompletedService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, ConfidenceLevel, Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.inject.bind
import play.api.libs.json.Json
import views.html.pages.SectionCompletedStateView

import java.time.Instant
import scala.concurrent.Future

class SectionCompletedStateControllerSpec extends AnyFreeSpec with MockitoSugar with Matchers {

  private val mtdItId: String = "1234567890"
  private val activated: String = "Activated"

  private val enrolments: Enrolments = Enrolments(Set(
    Enrolment(
      "HMRC-MTD-IT",
      Seq(EnrolmentIdentifier("MTDITID", mtdItId)),
      activated
    ),
    Enrolment(
      "HMRC-NI",
      Seq(EnrolmentIdentifier("NINO", "nino")),
      activated
    )
  ))

  private val authResponse: Enrolments ~ ConfidenceLevel =
    new~(
      enrolments,
      ConfidenceLevel.L250
    )

  private val sectionCompletedStateControllerRoute: String = "/update-and-submit-income-tax-return/state-benefits" + controllers.routes.SectionCompletedStateController.show(taxYear, JobSeekersAllowance).url
//    private val sectionCompletedStateControllerRoute: String = controllers.routes.SectionCompletedStateController.show(taxYear, JobSeekersAllowance ).url


  def form(): Form[Boolean] = YesNoForm.yesNoForm("sectionCompletedState.error.required")
  val mockAppConfig = mock[AppConfig]
  val mockAuthConnector = mock[AuthConnector]

  implicit val hc = new HeaderCarrier()

  when(mockAuthConnector.authorise[Option[AffinityGroup]](any(), eqTo(affinityGroup))(any(), any()))
    .thenReturn(Future.successful(Some(AffinityGroup.Individual)))

  when(mockAuthConnector.authorise[Enrolments ~ ConfidenceLevel](any(), eqTo(allEnrolments and confidenceLevel))(any(), any()))
    .thenReturn(Future.successful(authResponse))

  val mockService: SectionCompletedService = mock[SectionCompletedService]

  val endOfTaxYearRange: Int = taxYear
  val startOfTaxYearRange: Int = endOfTaxYearRange - 5
  val taxYear: Int = taxYear
  val taxYears: Seq[Int] = (startOfTaxYearRange to  endOfTaxYearRange).toList
  val validTaxYears: (String, String) = "validTaxYears" -> taxYears.mkString(",")

  def messages(application: Application): Messages = application.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val application = new GuiceApplicationBuilder()
    .overrides(
      bind[AppConfig].toInstance(mockAppConfig),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[SectionCompletedService].toInstance(mockService)
    )
    .build()

  "SectionCompletedStateController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[AppConfig].toInstance(mockAppConfig),
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[SectionCompletedService].toInstance(mockService)
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, sectionCompletedStateControllerRoute).withSession(validTaxYears)
          .withSession(TAX_YEAR -> taxYear.toString)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SectionCompletedStateView]

        when(mockService.get(any(), any(), any())(any())).thenReturn(Future.successful(None))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, JobSeekersAllowance)(mockAppConfig, messages(application), request).toString
      }
    }

    "must return OK and the correct view for a GET when the status is Completed" in {

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[AppConfig].toInstance(mockAppConfig),
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[SectionCompletedService].toInstance(mockService)
        )
        .build()

      running(application) {

        val journeyAnswers = JourneyAnswers(
          mtdItId = mtdItId,
          taxYear = taxYear,
          JobSeekersAllowance.typeName,
          data = Json.obj("status" -> JourneyStatus.Completed.toString),
          lastUpdated = Instant.now()
        )

        val request = FakeRequest(GET, sectionCompletedStateControllerRoute).withSession(validTaxYears)
          .withSession(TAX_YEAR -> taxYear.toString)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SectionCompletedStateView]

        when(mockService.get(any(), any(), any())(any())).thenReturn(Future.successful(Some(journeyAnswers)))

        status(result) mustEqual OK
         contentAsString(result) mustEqual view(form().fill(true), taxYear, JobSeekersAllowance)(mockAppConfig, messages(application), request).toString
      }

    }

    "must return OK and the correct view for a GET when the status is InProgress" in {

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[AppConfig].toInstance(mockAppConfig),
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[SectionCompletedService].toInstance(mockService)
        )
        .build()

      running(application) {

        val journeyAnswers = JourneyAnswers(
          mtdItId = mtdItId,
          taxYear = taxYear,
          JobSeekersAllowance.typeName,
          data = Json.obj("status" -> JourneyStatus.InProgress.toString),
          lastUpdated = Instant.now()
        )

        val request = FakeRequest(GET, sectionCompletedStateControllerRoute).withSession(validTaxYears)
          .withSession(TAX_YEAR -> taxYear.toString)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SectionCompletedStateView]

        when(mockService.get(any(), any(), any())(any())).thenReturn(Future.successful(Some(journeyAnswers)))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form().fill(false), taxYear, JobSeekersAllowance)(mockAppConfig, messages(application), request).toString

      }
    }

    "must return OK and the correct view for a GET when there is no status" in {

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[AppConfig].toInstance(mockAppConfig),
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[SectionCompletedService].toInstance(mockService)
        )
        .build()

      running(application) {

        val journeyAnswers = JourneyAnswers(
          mtdItId = mtdItId,
          taxYear = taxYear,
          JobSeekersAllowance.typeName,
          data = Json.obj("status" -> None),
          lastUpdated = Instant.now()
        )

        val request = FakeRequest(GET, sectionCompletedStateControllerRoute).withSession(validTaxYears)
          .withSession(TAX_YEAR -> taxYear.toString)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SectionCompletedStateView]

        when(mockService.get(any(), any(), any())(any())).thenReturn(Future.successful(Some(journeyAnswers)))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form(), taxYear, JobSeekersAllowance)(mockAppConfig, messages(application), request).toString

      }
    }

//    "must return BAD_REQUEST when form validation fails during submit" in {
//
//      running(application) {
//
//        val request =
//          FakeRequest(POST, sectionCompletedStateControllerRoute)
//            .withFormUrlEncodedBody(("value", "invalid value"))
//            .withSession(validTaxYears)
//
//        val view = application.injector.instanceOf[SectionCompletedStateView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
////          contentAsString(result) mustEqual view(form, taxYear, JobSeekersAllowance)(mockAppConfig, messages(application), request).toString
//      }
//
//    }

//    "must redirect on successful form submission" in {
//
//      val application = new GuiceApplicationBuilder()
//        .overrides(
//          bind[AppConfig].toInstance(mockAppConfig),
//          bind[AuthConnector].toInstance(mockAuthConnector),
//          bind[SectionCompletedService].toInstance(mockService),
//        )
//        .build()
//
//        running(application) {
//
//          val request = FakeRequest(GET, sectionCompletedStateControllerRoute).withSession(validTaxYears)
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual mockAppConfig.incomeTaxSubmissionOverviewUrl(taxYear)
//        }
//      }

    }
}
