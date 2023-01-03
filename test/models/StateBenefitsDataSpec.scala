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

package models

import play.api.libs.json.{JsValue, Json}
import support.UnitTest
import support.builders.StateBenefitBuilder.aStateBenefit
import support.builders.StateBenefitsDataBuilder.{aStateBenefitsData, aStateBenefitsDataJsValue}

import java.time.{Instant, LocalDate}
import java.util.UUID

class StateBenefitsDataSpec extends UnitTest {

  private val stateBenefit = aStateBenefit
    .copy(startDate = LocalDate.parse("2019-04-23"))
    .copy(endDate = Some(LocalDate.parse("2020-08-13")))
    .copy(dateIgnored = Some(Instant.parse("2019-07-08T05:23:00Z")))
    .copy(submittedOn = Some(Instant.parse("2020-09-11T17:23:00Z")))

  private val stateBenefitsData = aStateBenefitsData
    .copy(incapacityBenefits = Some(Set(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c934")))))
    .copy(statePension = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c935"))))
    .copy(statePensionLumpSum = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c936"))))
    .copy(employmentSupportAllowances = Some(Set(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c937")))))
    .copy(jobSeekersAllowances = Some(Set(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c938")))))
    .copy(bereavementAllowance = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c939"))))
    .copy(other = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c940"))))

  "stateBenefitsDataWrites" should {
    "convert StateBenefitsData to correct JsValue when full object" in {
      Json.toJson(stateBenefitsData) shouldBe aStateBenefitsDataJsValue
    }

    "convert StateBenefitsData to correct JsValue when empty object" in {
      val jsValue: JsValue = Json.parse(
        """
          |{
          |}
          |""".stripMargin)

      Json.toJson(StateBenefitsData()) shouldBe jsValue
    }
  }

  "stateBenefitsDataReads" should {
    "convert JsValue to StateBenefitsData when full object" in {
      Json.fromJson[StateBenefitsData](aStateBenefitsDataJsValue).get shouldBe stateBenefitsData
    }

    "convert JsValue to StateBenefitsData when empty object" in {
      val jsValue: JsValue = Json.parse(
        """
          |{
          |}
          |""".stripMargin)

      Json.fromJson[StateBenefitsData](jsValue).get shouldBe StateBenefitsData()
    }
  }
}
