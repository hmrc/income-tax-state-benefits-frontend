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

import sbt.*

object AppDependencies {

  private val bootstrapPlay30Version = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30" % bootstrapPlay30Version,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30" % bootstrapPlay30Version,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.17.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapPlay30Version  % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.18"                % Test,
    "org.jsoup"               %  "jsoup"                      % "1.17.2"                % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "7.0.1"                 % Test,
    "com.github.tomakehurst"  % "wiremock-jre8-standalone"    % "3.0.1"                % Test,
    "org.scalamock"           %% "scalamock"                  % "5.2.0"                 % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.64.8"                % Test
  )
}
