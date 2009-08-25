import sbt._

class MyProject(info: ProjectInfo) extends DefaultWebProject(info)
{
  override def useMavenConfigurations = true

  //val lift = "net.liftweb" % "lift-core" % "1.0" % "compile->default"
  val lift = "net.liftweb" % "lift-core" % "1.1-SNAPSHOT" % "compile->default"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.14" % "test->default"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"
  val junit = "junit" % "junit" % "3.8.1" % "test->default"
  val h2 = "com.h2database" % "h2" % "1.1.117" % "runtime->default"  // "1.+"

  // required because Ivy doesn't pull repositories from poms
  val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"
  
  // to get latest scala and lift snapshots
  val scalaToolsRepo = "scala tools snapshot" at "http://scala-tools.org/repo-snapshots"
}