name := "escli"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators"          % "1.0.4",
  "com.typesafe.akka"      %% "akka-actor"                        % "2.4.2",
  "com.typesafe.akka"      %% "akka-stream"                       % "2.4.2",
  "com.typesafe.akka"      %% "akka-http-experimental"            % "2.4.2",
  "com.typesafe.akka"      %% "akka-http-spray-json-experimental" % "2.4.2",
  "io.spray"               %% "spray-json"                        % "1.3.2",
  "jline"                  %  "jline"                             % "2.14.1",
  "org.scalatest"          %% "scalatest"                         % "2.2.4"   % "test")



  
