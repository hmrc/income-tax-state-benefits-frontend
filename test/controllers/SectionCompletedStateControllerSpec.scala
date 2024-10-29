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
import models.Journey.{EmploymentSupportAllowance, JobSeekersAllowance}
import models.StateBenefitsUserData
import models.errors.HttpParserError
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatestplus.mockito.MockitoSugar
import play.api.{Application, inject}
import play.api.data.Form
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import support.builders.UserBuilder.aUser
import support.utils.TaxYearUtils.taxYear
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, ConfidenceLevel, Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.pages.SectionCompletedStateView

import scala.concurrent.ExecutionContext.Implicits.global
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

  val SectionCompletedStateControllerRoute = "http://localhost:9376/update-and-submit-income-tax-return/state-benefits" + controllers.routes.SectionCompletedStateController.show(2025, EmploymentSupportAllowance.entryName).url

  def form(): Form[Boolean] = YesNoForm.yesNoForm("sectionCompletedState.error.required")
  val mockAppConfig = mock[AppConfig]
  val mockAuthConnector = mock[AuthConnector]

  implicit val hc = new HeaderCarrier()

  when(mockAuthConnector.authorise[Option[AffinityGroup]](any(), eqTo(affinityGroup))(any(), any()))
    .thenReturn(Future.successful(Some(AffinityGroup.Individual)))

  when(mockAuthConnector.authorise[Enrolments ~ ConfidenceLevel](any(), eqTo(allEnrolments and confidenceLevel))(any(), any()))
    .thenReturn(Future.successful(authResponse))

  val endOfTaxYearRange: Int = 2025
  val startOfTaxYearRange: Int = endOfTaxYearRange - 5
  val taxYear: Int = 2025
  val taxYears: Seq[Int] = (startOfTaxYearRange to  endOfTaxYearRange).toList
  val validTaxYears: (String, String) = "validTaxYears" -> taxYears.mkString(",")


  "SectionCompletedStateController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =  new GuiceApplicationBuilder()
        .overrides(inject.bind[AppConfig].toInstance(mockAppConfig),inject.bind[AuthConnector].toInstance(mockAuthConnector)).build()

      def messages(app: Application): Messages = application.injector.instanceOf[MessagesApi].preferred(FakeRequest())


      running(application) {
        val request = FakeRequest(GET, SectionCompletedStateControllerRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SectionCompletedStateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxYear, JobSeekersAllowance.entryName)(mockAppConfig, messages(application), request).toString
      }
    }
  }
}
