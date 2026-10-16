ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
// JavaScript
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.16.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")
addSbtPlugin("com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % "0.18.59")
addSbtPlugin("org.typelevel" %% "sbt-feral-lambda" % "0.2.4")
// Development tools
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.2")
addDependencyTreePlugin
