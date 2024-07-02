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

package models.pages

import models.pages.elements.BenefitDataRow
import models.{BenefitType, IncomeTaxUserData}
import play.api.data.Form

case class ClaimsPage(taxYear: Int,
                      benefitType: BenefitType,
                      isInYear: Boolean,
                      benefitDataRows: Seq[BenefitDataRow],
                      ignoredBenefitDataRows: Seq[BenefitDataRow],
                      form: Form[Boolean])

object ClaimsPage {

  def apply(taxYear: Int,
            benefitType: BenefitType,
            isInYear: Boolean,
            incomeTaxUserData: IncomeTaxUserData,
            form: Form[Boolean]): ClaimsPage = {
    val customerData = incomeTaxUserData.customerAllowancesFor(benefitType)
      .map(BenefitDataRow.mapFrom(taxYear, _)).toSeq

    val hmrcData = incomeTaxUserData.hmrcAllowancesFor(benefitType)
      .map(BenefitDataRow.mapFrom(taxYear, _))
      .filter(hmrcBenefit => !customerData.exists(_.benefitId == hmrcBenefit.benefitId)).toSeq

    val (ignoredBenefitDataRows, benefitDataRows) = (hmrcData ++ customerData)
      .sortWith((it1, it2) => it1.startDate.isBefore(it2.startDate))
      .partition(_.isIgnored)

    ClaimsPage(taxYear, benefitType, isInYear, benefitDataRows, ignoredBenefitDataRows, form)
  }
}
