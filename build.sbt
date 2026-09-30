ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "3.4.1"

ThisBuild / scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-source:future", // https://github.com/oleg-py/better-monadic-for
  "-Yexplicit-nulls", // https://docs.scala-lang.org/scala3/reference/other-new-features/explicit-nulls.html
  "-Ysafe-init", // https://docs.scala-lang.org/scala3/reference/other-new-features/safe-initialization.html
  "-Wnonunit-statement",
  "-Wunused:all",
)

Global / onChangedBuildSource := ReloadOnSourceChanges
Test / fork := false

ThisBuild / semanticdbEnabled := true
ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")

ThisBuild / evictionErrorLevel := Level.Info

addCommandAlias(
  "check",
  "; scalafixAll; scalafmtSbtCheck; scalafmtCheckAll",
)

lazy val commons = project
  .in(file("libs/commons"))
  .enablePlugins(ScalaJSPlugin)
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.5.0" % Optional,
      "is.cir" %%% "ciris" % "3.5.0" % Optional,
      "org.log4s" %%% "log4s" % "1.10.0" % Optional,
      "org.typelevel" %%% "cats-effect" % "3.5.4" % Optional,
      "org.typelevel" %%% "log4cats-core" % "2.6.0" % Optional,
    ),
  )

lazy val notifications = project
  .in(file("apps/notifications"))
  .enablePlugins(LambdaJSPlugin)
  .configure(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.comcast" %%% "ip4s-core" % "3.5.0",
      "io.circe" %%% "circe-parser" % "0.14.7",
      "is.cir" %%% "ciris" % "3.5.0",
      "org.http4s" %%% "http4s-circe" % "0.23.38",
      "org.http4s" %%% "http4s-dsl" % "0.23.38",
      "org.http4s" %%% "http4s-ember-client" % "0.23.38",
      "org.typelevel" %%% "feral-lambda-http4s" % "0.2.4",
    ),
  )
  .dependsOn(`commons` % "test->test;compile->compile")

lazy val operations = project
  .in(file("apps/operations"))
  .enablePlugins(LambdaJSPlugin, ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)
  .configure(baseSettings, nodeJsSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.comcast" %%% "ip4s-core" % "3.5.0",
      "com.softwaremill.sttp.client3" %%% "cats" % "3.9.5" % Test,
      "io.circe" %%% "circe-parser" % "0.14.7" % Test,
      "is.cir" %%% "ciris" % "3.5.0",
      "org.gnieh" %%% "fs2-data-xml" % "1.11.0",
      "org.http4s" %%% "http4s-circe" % "0.23.38",
      "org.http4s" %%% "http4s-dsl" % "0.23.38",
      "org.http4s" %%% "http4s-ember-client" % "0.23.38",
      "org.typelevel" %%% "case-insensitive" % "1.4.0",
      "org.typelevel" %%% "feral-lambda-http4s" % "0.2.4",
    ),
    Compile / npmDependencies ++= Seq(
      "mysql" -> "2.18.1",
      "promise-mysql" -> "5.2.0",
    ),
    // Waiting for Yarn version 2 to enable: useYarn := true
  )
  .dependsOn(
    `commons` % "test->test;compile->compile",
    `smithy4s-aws-clients` % "test->test;compile->compile",
  )

lazy val `smithy4s-aws-clients` = project
  .in(file("libs/smithy4s-aws-clients"))
  .enablePlugins(ScalaJSPlugin, Smithy4sCodegenPlugin)
  .configure(baseSettings, nodeJsSettings)
  .settings(
    idePackagePrefix := None,
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %%% "smithy4s-aws-http4s" % smithy4sVersion.value,
    ),
  )

lazy val MUnitFramework = new TestFramework("munit.Framework")

lazy val baseSettings: Project => Project = _.settings(
  idePackagePrefix := Some("es.eriktorr.lambda4s"),
  Global / excludeLintKeys += idePackagePrefix,
  libraryDependencies ++= Seq(
    "com.47deg" %%% "scalacheck-toolbox-datetime" % "0.7.0" % Test,
    "io.chrisdavenport" %%% "cats-scalacheck" % "0.3.2" % Test,
    "org.scalameta" %%% "munit" % "0.7.29" % Test,
    "org.scalameta" %%% "munit-scalacheck" % "0.7.29" % Test,
    "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test,
    "org.typelevel" %%% "scalacheck-effect" % "1.0.4" % Test,
    "org.typelevel" %%% "scalacheck-effect-munit" % "1.0.4" % Test,
  ),
  Test / testFrameworks += MUnitFramework,
  Test / testOptions += Tests.Argument(MUnitFramework, "--exclude-tags=online"),
)

lazy val nodeJsSettings: Project => Project =
  _.settings(
    Global / stQuiet := true,
    scalaJSLinkerConfig ~= (_
      /* disabled because it somehow triggers many warnings */
      .withSourceMap(false)
      .withModuleKind(ModuleKind.CommonJSModule)),
  )

lazy val root = project
  .in(file("."))
  .aggregate(commons, notifications, operations, `smithy4s-aws-clients`)
  .settings(
    name := "lambda4s-demo",
    Compile / doc / sources := Seq(),
    publish := {},
    publishLocal := {},
  )
