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

package support.builders.models.pages

import models.BenefitType._
import models.pages.SummaryPage
import models.pages.elements.TaskListItem
import models.pages.elements.TaskListTag._
import support.utils.TaxYearUtils.taxYear

object SummaryPageBuilder {

  val aSummaryPage: SummaryPage = SummaryPage(
    taxYear = taxYear,
    taskListItems = Seq(
      //      TaskListItem(IncapacityBenefit, controllers.routes.SummaryController.show(taxYear), Completed),
      //      TaskListItem(StatePension, controllers.routes.SummaryController.show(taxYear), CustomerOrHmrcAdded),
      //      TaskListItem(StatePensionLumpSum, controllers.routes.SummaryController.show(taxYear), HmrcAdded),
      //      TaskListItem(EmploymentSupportAllowance, controllers.routes.SummaryController.show(taxYear), InProgress),
      TaskListItem(JobSeekersAllowance, controllers.routes.SummaryController.show(taxYear), NotStarted)
      //      TaskListItem(BereavementAllowance, controllers.routes.SummaryController.show(taxYear), Completed),
      //      TaskListItem(OtherStateBenefits, controllers.routes.SummaryController.show(taxYear), Completed)
    )
  )
}
