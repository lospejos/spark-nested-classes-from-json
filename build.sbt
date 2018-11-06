name := "spark-nested-classes-from-json"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.3.2" // because Azure have Spark 2.3.1

libraryDependencies ++= Seq (
  //"org.apache.spark" %% "spark-core"                 % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql"                  % sparkVersion % "provided",
  //"org.apache.spark" %% "spark-streaming"            % sparkVersion % "provided"

  "org.apache.spark" %% "spark-sql-kafka-0-10"       % sparkVersion % "provided" excludeAll ExclusionRule(organization = "net.jpountz.lz4", name = "lz4"),
  //"org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion % "provided" excludeAll ExclusionRule(organization = "net.jpountz.lz4", name = "lz4"),

  "org.postgresql" % "postgresql" % "42.2.5"

  //"com.github.scopt" %% "scopt" % "3.7.0"  // command line options/arguments parser library

  //"org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % "test"
)

// To prevent errors when assembly
// https://stackoverflow.com/a/39058507/1828296
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}