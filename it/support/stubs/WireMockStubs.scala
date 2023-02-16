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

package support.stubs

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.StateBenefitsUserData
import models.authorisation.Enrolment.Agent
import play.api.http.ContentTypes.JSON
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.CONTENT_TYPE
import support.builders.UserBuilder.aUser
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel}
import uk.gov.hmrc.http.HttpResponse

import java.util.UUID

trait WireMockStubs {

  protected def authoriseAgentOrIndividual(isAgent: Boolean, nino: Boolean = true): StubMapping = if (isAgent) authoriseAgent() else authoriseIndividual(nino)

  protected def authoriseIndividualUnauthorized(): StubMapping = {
    stubPost(authoriseUri, UNAUTHORIZED, Json.prettyPrint(
      successfulAuthResponse(Some(AffinityGroup.Individual), ConfidenceLevel.L250, Seq(mtditEnrolment, ninoEnrolment): _*)
    ))
  }

  protected def authoriseIndividual(withNino: Boolean = true): StubMapping = {
    stubPost(authoriseUri, OK, Json.prettyPrint(successfulAuthResponse(Some(AffinityGroup.Individual), ConfidenceLevel.L250,
      enrolments = Seq(mtditEnrolment) ++ (if (withNino) Seq(ninoEnrolment) else Seq.empty[JsObject]): _*)))
  }

  protected def authoriseAgent(): StubMapping = {
    stubPost(authoriseUri, OK, Json.prettyPrint(
      successfulAuthResponse(Some(AffinityGroup.Agent), ConfidenceLevel.L250, Seq(asAgentEnrolment, mtditEnrolment): _*)
    ))
  }

  protected def authoriseAgentUnauthorized(): StubMapping = {
    stubPost(authoriseUri, UNAUTHORIZED, Json.prettyPrint(
      successfulAuthResponse(Some(AffinityGroup.Agent), ConfidenceLevel.L250, Seq(asAgentEnrolment, mtditEnrolment): _*)
    ))
  }

  protected def stubPost(url: String, status: Int, responseBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(post(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders.willReturn(aResponse().withStatus(status).withBody(responseBody)))
  }

  private def successfulAuthResponse(affinityGroup: Option[AffinityGroup], confidenceLevel: ConfidenceLevel, enrolments: JsObject*): JsObject = {
    affinityGroup match {
      case Some(group) => Json.obj(
        "affinityGroup" -> group,
        "allEnrolments" -> enrolments,
        "confidenceLevel" -> confidenceLevel
      )
      case _ => Json.obj(
        "allEnrolments" -> enrolments,
        "confidenceLevel" -> confidenceLevel
      )
    }
  }


  private val authoriseUri = "/auth/authorise"

  private val mtditEnrolment = Json.obj(
    "key" -> "HMRC-MTD-IT",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "MTDITID",
        "value" -> "1234567890"
      )
    )
  )

  private val ninoEnrolment = Json.obj(
    "key" -> "HMRC-NI",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "NINO",
        "value" -> aUser.nino
      )
    )
  )

  private val asAgentEnrolment = Json.obj(
    "key" -> Agent.key,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> Agent.value,
        "value" -> "XARN1234567"
      )
    )
  )

  protected def stubGetWithHeadersCheck(url: String,
                                        httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, get(urlMatching(url)))
  }

  protected def userPriorDataStub(nino: String,
                                  taxYear: Int,
                                  httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, get(urlMatching(s"/income-tax-state-benefits/prior-data/nino/$nino/tax-year/$taxYear")))
  }

  protected def userSessionDataStub(nino: String,
                                    sessionDataId: UUID,
                                    httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, get(urlMatching(s"/income-tax-state-benefits/session-data/nino/$nino/session/$sessionDataId")))
  }

  protected def createSessionDataStub(stateBenefitsUserData: StateBenefitsUserData,
                                      httpResponse: HttpResponse): StubMapping = {
    createSessionDataStub(s"/income-tax-state-benefits/session-data", stateBenefitsUserData, httpResponse)
  }

  protected def createSessionDataStub(url: String,
                                      stateBenefitsUserData: StateBenefitsUserData,
                                      httpResponse: HttpResponse): StubMapping = {
    val mappingBuilder = post(urlMatching(url)).withRequestBody(equalToJson(Json.toJson(stateBenefitsUserData).toString))
    stubMapping(httpResponse, mappingBuilder)
  }

  protected def updateSessionDataStub(stateBenefitsUserData: StateBenefitsUserData,
                                      httpResponse: HttpResponse): StubMapping = {
    val nino = stateBenefitsUserData.nino
    val sessionDataId = stateBenefitsUserData.sessionDataId.get
    updateSessionDataStub(s"/income-tax-state-benefits/session-data/nino/$nino/session/$sessionDataId", stateBenefitsUserData, httpResponse)
  }

  protected def updateSessionDataStub(url: String,
                                      stateBenefitsUserData: StateBenefitsUserData,
                                      httpResponse: HttpResponse): StubMapping = {
    val mappingBuilder = put(urlMatching(url)).withRequestBody(equalToJson(Json.toJson(stateBenefitsUserData).toString))
    stubMapping(httpResponse, mappingBuilder)
  }

  protected def saveStateBenefitStub(stateBenefitsUserData: StateBenefitsUserData,
                                     httpResponse: HttpResponse): StubMapping = {
    val mappingBuilder = put(urlMatching(s"/income-tax-state-benefits/income-tax"))
      .withRequestBody(equalToJson(Json.toJson(stateBenefitsUserData).toString()))
    stubMapping(httpResponse, mappingBuilder)
  }

  protected def saveStateBenefitStub(url: String,
                                     httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, put(urlMatching(url)))
  }

  protected def removeClaimStub(nino: String,
                                sessionDataId: UUID,
                                httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, delete(urlMatching(s"/income-tax-state-benefits/session-data/nino/$nino/session/$sessionDataId")))
  }

  protected def removeClaimStub(url: String,
                                httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, delete(urlMatching(url)))
  }

  protected def restoreClaimStub(nino: String,
                                 sessionDataId: UUID,
                                 httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, delete(urlMatching(s"/income-tax-state-benefits/session-data/nino/$nino/session/$sessionDataId/ignore")))
  }

  protected def restoreClaimStub(url: String,
                                 httpResponse: HttpResponse): StubMapping = {
    stubMapping(httpResponse, delete(urlMatching(url)))
  }

  private def stubMapping(httpResponse: HttpResponse,
                          mappingBuilder: MappingBuilder,
                          sessionHeader: (String, String) = "X-Session-ID" -> aUser.sessionId,
                          mtditidHeader: (String, String) = "mtditid" -> aUser.mtditid,
                          requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val responseBuilder = aResponse()
      .withStatus(httpResponse.status)
      .withBody(httpResponse.body)
      .withHeader(CONTENT_TYPE, JSON)

    val mappingBuilderWithHeaders: MappingBuilder = requestHeaders
      .foldLeft(mappingBuilder.withHeader(sessionHeader._1, equalTo(sessionHeader._2))
        .withHeader(mtditidHeader._1, equalTo(mtditidHeader._2)))((result, nxt) => result.withHeader(nxt.key(), equalTo(nxt.firstValue())))

    stubFor(mappingBuilderWithHeaders.willReturn(responseBuilder))
  }
}
