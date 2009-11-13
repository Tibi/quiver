import sbt._

class MyProject(info: ProjectInfo) extends DefaultWebProject(info) //with AutoCompilerPlugins
{
  override def useMavenConfigurations = true

  val liftVersion = "1.1-M6" // M7 has no sources for now
  val lift = "net.liftweb" % "lift-core" % liftVersion withSources()
  
  // The following are needed only to get sources
  val liftWeb = "net.liftweb" % "lift-webkit" % liftVersion withSources()
  val liftUtil = "net.liftweb" % "lift-util" % liftVersion withSources()
  val liftMapper = "net.liftweb" % "lift-mapper" % liftVersion withSources()
  
  // Jetty must be in test for sbt to run it
  val jettyVersion = "6.1.14"
  val jetty6 = "org.mortbay.jetty" % "jetty" % jettyVersion % "test"
  val jettyPlus = "org.mortbay.jetty" % "jetty-plus" % jettyVersion % "test"
  val jettyNaming = "org.mortbay.jetty" % "jetty-naming" % jettyVersion % "test"
  
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val h2 = "com.h2database" % "h2" % "1.1.117"
  val csv = "net.sf.opencsv" % "opencsv" % "2.0"

  val mysql = "mysql" % "mysql-connector-java" % "5.1.6"
  
  // Testing
  val junit = "junit" % "junit" % "4.5" % "test"
  val specs = "org.scala-tools.testing" % "specs" % "1.6.0" % "test"
  val scalaCheck = "org.scalacheck" % "scalacheck" % "1.5" % "test"
  
  // required because Ivy doesn't pull repositories from poms
  val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"
  
  // to get latest scala and lift snapshots
  //val scalaToolsRepo = "scala tools snapshot" at "http://scala-tools.org/repo-snapshots"

  /*
  val sxr = compilerPlugin("org.scala-tools.sxr" %% "sxr" % "0.2.1")
  override def compileOptions =
    CompileOption("-P:sxr:base-directory:" + mainScalaSourcePath.asFile.getAbsolutePath) ::
    super.compileOptions.toList
  */
}