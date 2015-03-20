// Copyright 2015 Connectifier, Inc. All Rights Reserved.

organization := "com.connectifier.xero"
name := "client"
version := "0.12-SNAPSHOT"

// Java. Not Scala
crossPaths := false
autoScalaLibrary := false

// XJC-plugin settings
sources in (Compile, xjc) += baseDirectory.value / ".." / ".." / "XeroAPI-Schemas" / "v2.00"
// Can remove sourceManaged line after using future version of plugin
// https://github.com/sbt/sbt-xjc/commit/226ff93ddcd0374aae0383b0a2a4e0282f7b7374
sourceManaged in (Compile, xjc) <<= sourceManaged / "main"
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

// Eclipse
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
EclipseKeys.createSrc := EclipseCreateSrc.All
EclipseKeys.withSource := true
