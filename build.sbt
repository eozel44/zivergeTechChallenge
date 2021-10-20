name := "zivergeTechChallenge"

version := "0.1"

scalaVersion := "2.12.11"

val Http4sVersion  = "1.0.0-M4"
val ZIOVersion  = "1.0.11"
val CirceVersion  = "0.14.1"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-dsl"          % Http4sVersion,
  "dev.zio"    %% "zio"                 % ZIOVersion,
  "dev.zio"    %% "zio-process"         % "0.5.0",
  "dev.zio"    %% "zio-interop-cats"    % "2.5.1.0",
  "io.circe"   %% "circe-core"          % CirceVersion,
  "io.circe"   %% "circe-parser"        % CirceVersion,
  "io.circe"   %% "circe-generic"       % CirceVersion
)