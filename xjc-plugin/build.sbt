// Copyright 2015 Connectifier, Inc. All Rights Reserved.

organization := "com.connectifier.xero"
name := "xjc-plugin"
version := "0.1-SNAPSHOT"

// Java. Not Scala
crossPaths := false
autoScalaLibrary := false

// Eclipse
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java
EclipseKeys.withSource := true

libraryDependencies ++= Seq(
  "org.glassfish.jaxb" % "jaxb-xjc"        % "2.2.11",
  "junit"              % "junit"           % "4.12"  % "test",
  "com.novocode"       % "junit-interface" % "0.11"  % "test"
)
