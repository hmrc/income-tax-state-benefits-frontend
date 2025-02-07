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

package support.builders.pages

import models.BenefitType._
import models.pages.SummaryPage
import models.pages.elements.TaskListTag
import models.pages.elements.TaskListTag._
import play.api.i18n.Messages
import support.utils.TaxYearUtils.taxYear
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemTitle}

object SummaryPageBuilder {

  def summaryPage()(implicit messages: Messages): SummaryPage = SummaryPage(
    taxYear = taxYear,
    taskListItems = List(
      TaskListItem(
        title = TaskListItemTitle(HtmlContent(messages(s"common.${JobSeekersAllowance.typeName}"))),
        status = TaskListTag.itemStatus(NotStarted),
        href = Some(controllers.routes.ClaimsController.show(taxYear, JobSeekersAllowance).url)
      )
    )
  )
}
