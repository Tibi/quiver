name := "Quiver"
 
scalaVersion := "2.9.1"
 
seq(webSettings: _*)

// If using JRebel with 0.1.0 of the sbt web plugin
//jettyScanDirs := Nil
// using 0.2.4+ of the sbt web plugin
scanDirectories in Compile := Nil

//resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

// you can also add multiple repositories at the same time
resolvers ++= Seq(
  "Scala Tools Snapshot" at "http://scala-tools.org/repo-releases/",
  "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
)

// if you have issues pulling dependencies from the scala-tools repositories (checksums don't match), you can disable checksums
//checksums := Nil

libraryDependencies ++= {
  val liftVersion = "2.4-M4" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-widgets" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default"
  )
}

libraryDependencies += "net.sf.opencsv" % "opencsv" % "2.3"

    
// when using the sbt web app plugin 0.2.4+, use "container" instead of "jetty" for the context
// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container", // For Jetty 8
  //"org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "container", // For Jetty 7
  //"org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty,test", // For Jetty 6, add scope test to make jetty avl. for tests
  "org.scala-tools.testing" % "specs_2.9.0" % "1.6.8" % "test", // For specs.org tests
  "junit" % "junit" % "4.8" % "test->default", // For JUnit 4 testing
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "com.h2database" % "h2" % "1.2.138", // In-process database, useful for development systems
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default" // Logging
)


 //val jettyPlus = "org.mortbay.jetty" % "jetty-plus" % jettyVersion % "test"
//  val jettyNaming = "org.mortbay.jetty" % "jetty-naming" % jettyVersion % "test"
  
//  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
//  val h2 = "com.h2database" % "h2" % "1.1.118"
//  val csv = "net.sf.opencsv" % "opencsv" % "2.0"
  // Testing
//  val junit = "junit" % "junit" % "4.5" % "test"
//  val specs = "org.scala-tools.testing" % "specs" % "1.6.0" % "test"
//  val scalaCheck = "org.scalacheck" % "scalacheck" % "1.5" % "test"
  
  // required because Ivy doesn't pull repositories from poms
//  val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"
 
//  val mysql = "mysql" % "mysql-connector-java" % "5.1.10"
  
