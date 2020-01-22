name := "sclera-sqltests-parser"

description := "SQL Tests Parser"

version := "4.0-SNAPSHOT"

homepage := Some(url("https://github.com/scleradb/sclera-sqltests-parser"))

organization := "com.scleradb"

organizationName := "Sclera, Inc."

organizationHomepage := Some(url("https://www.scleradb.com"))

startYear := Some(2012)

scalaVersion := "2.13.1"

licenses := Seq("Apache License version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
)

scalacOptions ++= Seq(
    "-Werror", "-feature", "-deprecation", "-unchecked"
)

exportJars := true
