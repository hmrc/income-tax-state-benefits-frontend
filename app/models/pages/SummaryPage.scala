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

package models.pages

import controllers.routes.ClaimsController
import models.BenefitType.{EmploymentSupportAllowance, JobSeekersAllowance}
import models.pages.elements.TaskListItem
import models.pages.elements.TaskListTag.Completed

case class SummaryPage(taxYear: Int,
                       taskListItems: Seq[TaskListItem])

object SummaryPage {

  def apply(taxYear: Int): SummaryPage = {
    val taskListItems: Seq[TaskListItem] = Seq(
      TaskListItem(EmploymentSupportAllowance, ClaimsController.show(taxYear, EmploymentSupportAllowance), Completed),
      TaskListItem(JobSeekersAllowance, ClaimsController.show(taxYear, JobSeekersAllowance), Completed)
    )

    SummaryPage(taxYear, taskListItems)
  }
}
