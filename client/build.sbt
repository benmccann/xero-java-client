// Copyright 2015 Connectifier, Inc. All Rights Reserved.

organization := "com.connectifier.xero"
name := "client"
version := "0.12-SNAPSHOT"

// Java. Not Scala
crossPaths := false
autoScalaLibrary := false

// XJC-plugin settings
sources in (Compile, xjc) += baseDirectory.value / ".." / ".." / "XeroAPI-Schemas" / "v2.00"
xjcCommandLine += "-p"
xjcCommandLine += "com.connectifier.xeroclient.models"
xjcBindings += "src/main/resources/bindings.xjb"
xjcPlugins += "com.connectifier.xero" % "xjc-plugin" % "0.1-SNAPSHOT"
xjcCommandLine += "-Xcustom"

libraryDependencies ++= Seq(
  "com.google.guava" % "guava"           % "18.0",
  "org.bouncycastle" % "bcpkix-jdk15on"  % "1.51",
  "org.scribe"       % "scribe"          % "1.3.5",
  "junit"            % "junit"           % "4.12"  % "test",
  "com.novocode"     % "junit-interface" % "0.11"  % "test"
)
