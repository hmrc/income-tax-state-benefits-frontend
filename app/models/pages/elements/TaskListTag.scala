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

package models.pages.elements

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.TaskListItemStatus
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

sealed abstract class TaskListTag(comment: String)

object TaskListTag {
  case object Completed extends TaskListTag(comment = "All fields completed, or marked as completed by the user.")
  case object CustomerOrHmrcAdded extends TaskListTag(comment = "Some data entered by the user from scratch, section may also have had pre-populated data")
  case object HmrcAdded extends TaskListTag(comment = "Pre-populated data only")
  case object InProgress extends TaskListTag(comment = "Section started from scratch by user but not completed")
  case object NotStarted extends TaskListTag(comment = "No user entered data saved and no pre-populated data")

  def itemStatus(tagStatus: TaskListTag)(implicit messages: Messages) : TaskListItemStatus = {
    tagStatus match {
      case TaskListTag.Completed =>
        TaskListItemStatus(content = HtmlContent(messages(s"common.completed")))
      case TaskListTag.CustomerOrHmrcAdded =>
        TaskListItemStatus(Some(Tag(content = HtmlContent(messages(s"common.addedByHmrc")), classes = "govuk-tag--red")))
      case TaskListTag.HmrcAdded =>
        TaskListItemStatus(Some(Tag(content = HtmlContent(messages(s"common.addedByHmrc")), classes = "govuk-tag--red")))
      case TaskListTag.InProgress =>
        TaskListItemStatus(Some(Tag(content = HtmlContent(messages(s"common.inProgress")), classes = "govuk-tag--light-blue")))
      case TaskListTag.NotStarted =>
        TaskListItemStatus(Some(Tag(content = HtmlContent(messages(s"common.notStarted")), classes = "govuk-tag--blue")))
    }
  }
}
