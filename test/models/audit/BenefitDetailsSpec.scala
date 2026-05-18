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

package models.audit

import play.api.libs.json.Json
import support.UnitTest
import support.builders.ClaimCYAModelBuilder.aClaimCYAModel

import java.time.{Instant, LocalDate}

class BenefitDetailsSpec extends UnitTest {

  ".apply(...)" must {
    "create object from ClaimCYAModel" in {
      BenefitDetails.apply(aClaimCYAModel) shouldBe BenefitDetails(
        startDate = aClaimCYAModel.startDate,
        endDate = aClaimCYAModel.endDate,
        dateIgnored = aClaimCYAModel.dateIgnored,
        submittedOn = aClaimCYAModel.submittedOn,
        amount = aClaimCYAModel.amount,
        taxPaid = aClaimCYAModel.taxPaid
      )
    }
  }

  "writes" must {
    "write a fully populated BenefitDetails to Json" in {
        val underTest = BenefitDetails(
          startDate = LocalDate.parse("2019-04-23"),
          endDate = Some(LocalDate.parse("2020-08-13")),
          dateIgnored = Some(Instant.parse("2019-07-08T05:23:00Z")),
          submittedOn = Some(Instant.parse("2020-03-13T19:23:00Z")),
          amount = Some(300.00),
          taxPaid = Some(50.00)
        )

        Json.toJson(underTest) shouldBe Json.parse(
          """
            |{
            |  "startDate": "2019-04-23",
            |  "endDate": "2020-08-13",
            |  "dateIgnored": "2019-07-08T05:23:00Z",
            |  "submittedOn": "2020-03-13T19:23:00Z",
            |  "amount": 300.0,
            |  "taxPaid": 50.0
            |}
            |""".stripMargin)
    }

    "write a minimal BenefitDetails to Json" in {
        val underTest = BenefitDetails(startDate = LocalDate.parse("2019-04-23"))

        Json.toJson(underTest) shouldBe Json.parse(
          """
            |{
            |  "startDate": "2019-04-23"
            |}
            |""".stripMargin)
    }
  }
}
