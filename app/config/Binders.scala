/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import models.BenefitType
import play.api.mvc.{PathBindable, QueryStringBindable}

import java.net.URLEncoder

object Binders {

  implicit def benefitTypePathBinder(implicit stringBinder: PathBindable[String]): PathBindable[BenefitType] = new PathBindable[BenefitType] {

    override def bind(key: String, value: String): Either[String, BenefitType] =
      stringBinder.bind(key, value)
        .flatMap(mapToBenefitType)

    override def unbind(key: String, value: BenefitType): String = URLEncoder.encode(mapToString(value), "utf-8")
  }

  implicit def benefitTypeQueryBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[BenefitType] =
    new QueryStringBindable[BenefitType] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BenefitType]] =
        stringBinder.bind("benefitType", params)
          .map(_.flatMap(mapToBenefitType))

      override def unbind(key: String, value: BenefitType): String =
        stringBinder.unbind("benefitType", URLEncoder.encode(value.typeName, "utf-8"))
    }

  private def mapToBenefitType(value: String): Either[String, BenefitType] = value match {
    case "state-pension" | "statePension" => Right(BenefitType.StatePension)
    case "state-pension-lump-sum" | "statePensionLumpSum" => Right(BenefitType.StatePensionLumpSum)
    case "employment-support-allowance" | "employmentSupportAllowance" => Right(BenefitType.EmploymentSupportAllowance)
    case "jobseekers-allowance" | "jobSeekersAllowance" => Right(BenefitType.JobSeekersAllowance)
    case "other-state-benefits" | "otherStateBenefits" => Right(BenefitType.OtherStateBenefits)
    case _ => Left(s"Unknown benefit type $value")
  }

  private def mapToString(value: BenefitType) = value match {
    case BenefitType.StatePension => "state-pension"
    case BenefitType.StatePensionLumpSum => "state-pension-lump-sum"
    case BenefitType.EmploymentSupportAllowance => "employment-support-allowance"
    case BenefitType.JobSeekersAllowance => "jobseekers-allowance"
    case BenefitType.OtherStateBenefits => "other-state-benefits"
  }
}
