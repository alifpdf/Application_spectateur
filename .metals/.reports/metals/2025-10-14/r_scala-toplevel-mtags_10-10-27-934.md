error id: file://<WORKSPACE>/build.mill:[901..904) in Input.VirtualFile("file://<WORKSPACE>/build.mill", "import mill.*, scalalib.*, scalajslib.*
import mill.scalajslib.api.ModuleKind
import mill.api.Task.Simple
import os.Path

object versions:
  def scala = "3.7.3"
  def scalaJS = "1.20.1"

object common extends ScalaModule:
  def scalaVersion = versions.scala

  object js extends ScalaJSModule:
    def scalaVersion = versions.scala
    def scalaJSVersion = versions.scalaJS

    def moduleDir: Path = common.moduleDir
    def mvnDeps = common.mvnDeps

object server extends ScalaModule:
  def scalaVersion = versions.scala

  def moduleDeps = Seq(common)

  def mvnDeps = Seq(
    mvn"com.lihaoyi::cask:0.10.2",
    mvn"com.lihaoyi::os-lib:0.11.5"
  )

  def resources = Task(
    super.resources()
    ++ clientSpec.resources()
    :+ clientSpec.fullLinkJS().dest
  )

trait ClientModule extends ScalaJSModule:
  def scalaVersion   = versions.scala
  def scalaJSVersion = versions.scalaJS

  def 

  def moduleDeps = Seq(common.js)

  def mvnDeps = Seq(
    mvn"io.indigoengine::tyrian::0.14.0",
    mvn"io.indigoengine::tyrian-io::0.14.0"
  )

  def moduleKind = ModuleKind.ESModule

  def packageClient: Task[PathRef] = Task:
    os.copy(fullLinkJS().dest, Task.dest / "")

object clientSpec extends ClientModule

object clientCoach extends ClientModule")
file://<WORKSPACE>/file:<WORKSPACE>/build.mill
file://<WORKSPACE>/build.mill:42: error: expected identifier; obtained def
  def moduleDeps = Seq(common.js)
  ^
#### Short summary: 

expected identifier; obtained def