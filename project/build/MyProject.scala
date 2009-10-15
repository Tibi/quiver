import sbt._

class MyProject(info: ProjectInfo) extends DefaultWebProject(info) //with AutoCompilerPlugins
{
  override def useMavenConfigurations = true

  val lift = "net.liftweb" % "lift-core" % "1.1-M6" withSources()
  // The following are needed only to get sources
  val liftWeb = "net.liftweb" % "lift-webkit" % "1.1-M6" withSources()
  val liftUtil = "net.liftweb" % "lift-util" % "1.1-M6" withSources()
  val liftMapper = "net.liftweb" % "lift-mapper" % "1.1-M6" withSources()
  
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.14" % "test"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val junit = "junit" % "junit" % "3.8.1" % "test"
  val h2 = "com.h2database" % "h2" % "1.1.117" % "test"

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