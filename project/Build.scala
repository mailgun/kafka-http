/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import Keys._
import Process._

import scala.xml.{Node, Elem}
import scala.xml.transform.{RewriteRule, RuleTransformer}

object KafkaHttpBuild extends Build {
  val buildNumber = SettingKey[String]("build-number", "Build number defaults to $BUILD_NUMBER environment variable")
  val releaseName = SettingKey[String]("release-name", "the full name of this release")
  val commonSettings = Seq(
    organization := "com.rackspace",
    pomExtra :=
<parent>
  <groupId>com.rackpace</groupId>
  <artifactId>rackspace</artifactId>
  <version>10</version>
</parent>
<licenses>
  <license>
    <name>Apache 2</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    <distribution>repo</distribution>
  </license>
</licenses>,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-g:none"),
    crossScalaVersions := Seq("2.9.1", "2.9.2"),
    scalaVersion := "2.9.2",
    version := "0.0.1",
    publishTo := Some("Apache Maven Repo" at "https://repository.apache.org/service/local/staging/deploy/maven2"),
    credentials += Credentials(Path.userHome / ".m2" / ".credentials"),
    buildNumber := System.getProperty("build.number", ""),
    version <<= (buildNumber, version)  { (build, version)  => if (build == "") version else version + "+" + build},
    releaseName <<= (name, version, scalaVersion) {(name, version, scalaVersion) => name + "_" + scalaVersion + "-" + version},
    javacOptions ++= Seq("-Xlint:unchecked", "-source", "1.5"),
    parallelExecution in Test := false, // Prevent tests from overrunning each other

    libraryDependencies ++= Seq(
      "log4j"                 % "log4j"        % "1.2.15",
      "net.sf.jopt-simple"     % "jopt-simple"  % "3.2",
      "org.slf4j"             % "slf4j-simple" % "1.6.4",
      "org.eclipse.jetty"      % "jetty-server" % "8.1.14.v20131031",
      "org.eclipse.jetty"      % "jetty-servlet" % "8.1.14.v20131031",
      "org.mongodb"            % "mongo-java-driver" % "2.11.3",
      "org.apache.kafka"       % "kafka_2.9.2" % "0.8.0",
      "org.slf4j"             % "slf4j-api" % "1.6.4",
      "org.slf4j"             % "slf4j-simple" % "1.6.4"
    ),
    // The issue is going from log4j 1.2.14 to 1.2.15, the developers added some features which required
    // some dependencies on various sun and javax packages.
    ivyXML := <dependencies>
        <exclude module="javax"/>
        <exclude module="jmxri"/>
        <exclude module="jmxtools"/>
        <exclude module="mail"/>
        <exclude module="jms"/>
        <dependency org="org.apache.kafka" name="kafka_2.9.2" rev="0.8.0"></dependency>
        <dependency org="org.apache.zookeeper" name="zookeeper" rev="3.3.4">
          <exclude org="log4j" module="log4j"/>
          <exclude org="jline" module="jline"/>
        </dependency>
         <dependency org="com.timgroup" name="java-statsd-client" rev="2.0.0"/>
         <dependency org="org.eclipse.jetty" name="jetty-server" rev="8.1.14.v20131031">
         </dependency>
         <dependency org="org.eclipse.jetty" name="jetty-servlet" rev="8.1.14.v20131031">
         </dependency>
         <dependency org="org.mongodb" name="mongo-java-driver" rev="2.11.3">
         </dependency>
      </dependencies>
  )
  libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

  val release = TaskKey[Unit]("release", "Creates a deployable release directory file with dependencies, config, and scripts.")
  val releaseTask = release <<= ( packageBin in Compile, dependencyClasspath in Runtime, exportedProducts in Compile,
    target, releaseName) map { (packageBin, deps, products, target, releaseName) =>
      // NOTE: explicitly exclude sbt-launch.jar dep here, because it can use different scala version and if copied to jars folder will break the build
      // Found the hard way :-(
      val jarFiles = deps.files.filter(f => !products.files.contains(f) && f.getName.endsWith(".jar") && f.getName != "sbt-launch.jar")
      val destination = target / "RELEASE" / releaseName
      IO.copyFile(packageBin, destination / packageBin.getName)
      IO.copy(jarFiles.map { f => (f, destination / "libs" / f.getName) })
      IO.copyDirectory(file("config"), destination / "config")
      IO.copyDirectory(file("bin"), destination / "bin")
      for {file <- (destination / "bin").listFiles} { file.setExecutable(true, true) }
  }
  lazy val kafkaHttp    = Project(id = "KafkaHttp", base = file(".")).settings((commonSettings ++ releaseTask): _*)
}
