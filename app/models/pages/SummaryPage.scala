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

import models.BenefitType.{EmploymentSupportAllowance, JobSeekersAllowance}
import models.pages.elements.TaskListTag
import models.pages.elements.TaskListTag.Completed
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}

case class SummaryPage(taxYear: Int,
                       taskListItems: Seq[TaskListItem])

object SummaryPage {

  def apply(taxYear: Int)(implicit messages: Messages): SummaryPage = {

    val taskListItems: Seq[TaskListItem] = List(
      TaskListItem(
        title = TaskListItemTitle(HtmlContent(messages(s"common.${EmploymentSupportAllowance.typeName}"))),
        status = TaskListTag.itemStatus(Completed),
        href = Some(controllers.routes.ClaimsController.show(taxYear, EmploymentSupportAllowance).url),
        classes = EmploymentSupportAllowance.typeName
      ),
      TaskListItem(
        title = TaskListItemTitle(HtmlContent(messages(s"common.${JobSeekersAllowance.typeName}"))),
        status = TaskListTag.itemStatus(Completed),
        href = Some(controllers.routes.ClaimsController.show(taxYear, JobSeekersAllowance).url),
        classes = JobSeekersAllowance.typeName
      )
    )

    SummaryPage(taxYear, taskListItems)
  }
}
