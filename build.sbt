import AssemblyKeys._

assemblySettings


name := "PredicitiveAnalatics"

version := "1.0"

scalaVersion := "2.10.4"

lazy val root = project.in(file("."))

classpathTypes += "maven-plugin"

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4",
  "com.google.guava" % "guava" % "19.0",
  "jfree" % "jfreechart" % "1.0.13",
  "org.nd4j" % "canova-nd4j-codec" % "0.0.0.15",
  ("org.deeplearning4j" % "deeplearning4j-core" % "0.5.0").exclude("org.slf4j", "slf4j-log4j12"),
  "org.jblas" % "jblas" % "1.2.4",
  "org.nd4j" % "nd4j-native" % "0.5.0" classifier "" classifier "linux-x86_64",
  "org.deeplearning4j" % "dl4j-spark" % "0.4-rc3.6",
  "org.apache.spark" %% "spark-hive" % "1.6.1",
  ("org.deeplearning4j" % "arbiter-deeplearning4j" % "0.5.0"))

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case x if x.startsWith("META-INF/maven/com.google.guava") => MergeStrategy.last
    case x if x.startsWith("META-INF/maven/com.twitter") => MergeStrategy.last
     case x if x.startsWith("META-INF/maven/org.apache.avro") => MergeStrategy.last
     case x if x.startsWith("META-INF/maven/org.bytedeco.javacpp-presets") => MergeStrategy.last
     case x if x.startsWith("META-INF/maven/org.eclipse.jetty") => MergeStrategy.last
     case x if x.startsWith("META-INF/maven/org.slf4j") => MergeStrategy.last
     case x if x.startsWith("assets/dl4j-ui.js") => MergeStrategy.last
     case x if x.startsWith("com/esotericsoftware/minlog") => MergeStrategy.last
     case x if x.startsWith("com/google/common/base") => MergeStrategy.last
     case x if x.startsWith("dropwizard.yml") => MergeStrategy.last
     case x if x.startsWith("javax/annotation") => MergeStrategy.last
     case x if x.startsWith("javax/inject") => MergeStrategy.last
     case x if x.startsWith("javax/servlet") => MergeStrategy.last
     case x if x.startsWith("overview.html") => MergeStrategy.last
     case x if x.startsWith("parquet.thrift") => MergeStrategy.last
     case x if x.startsWith("parquet") => MergeStrategy.last
     
     case x if x.startsWith("plugin.xml") => MergeStrategy.last
     
     case PathList("org", "aopalliance", xs@_ *) => MergeStrategy.discard 
     case PathList("com", "sun","research", "ws"  , "wadl", xs@_ *) => MergeStrategy.discard 
     case PathList("org", "apache","commons" , xs@_ *) => MergeStrategy.discard 
     case PathList("org", "apache","hadoop" , "yarn" , xs@_ *) => MergeStrategy.discard 
     case PathList("org", "apache","log4j"  , xs@_ *) => MergeStrategy.discard 
     case PathList("org", "apache","spark" , "unused" , xs@_ *) => MergeStrategy.discard 
     case PathList("org", "slf4j"  , xs@_ *) => MergeStrategy.discard 
     case PathList("org", "slf4j"  , xs@_ *) => MergeStrategy.discard 
     case PathList("org", "apache" ,"hadoop"  , xs@_ *) => MergeStrategy.first 
     case PathList("org", "apache" ,"hive"  , xs@_ *) => MergeStrategy.first 
     case PathList("javax", "ws" ,"rs"  , xs@_ *) => MergeStrategy.first 
     case PathList("org", "objenesis"  , xs@_ *) => MergeStrategy.first 
     case PathList("javax", "xml"  , xs@_ *) => MergeStrategy.first 
     case PathList("javax", "el"  , xs@_ *) => MergeStrategy.first 
          
     case x => old(x)
  }
 }

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype release Repository" at "http://oss.sonatype.org/service/local/staging/deploy/maven2/"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository/"

