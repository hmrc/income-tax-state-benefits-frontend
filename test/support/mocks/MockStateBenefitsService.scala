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

package support.mocks

import models.errors.HttpParserError
import models.{IncomeTaxUserData, User}
import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import services.StateBenefitsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockStateBenefitsService extends MockFactory {

  protected val mockStateBenefitsService: StateBenefitsService = mock[StateBenefitsService]

  def mockGetPriorData(user: User,
                       taxYear: Int,
                       result: Either[HttpParserError, IncomeTaxUserData]): CallHandler3[User, Int, HeaderCarrier, Future[Either[HttpParserError, IncomeTaxUserData]]] = {
    (mockStateBenefitsService.getPriorData(_: User, _: Int)(_: HeaderCarrier))
      .expects(user, taxYear, *)
      .returning(Future.successful(result))
  }
}
