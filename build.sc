import mill._, mill.scalalib._, mill.scalalib.publish._, mill.scalajslib._
import mill.scalalib.scalafmt._
import coursier.maven.MavenRepository
import ammonite.ops._

val thisScalaVersion = "2.12.12"
val thisScalaJSVersion = "0.6.33"
val thisPublishVersion = "0.3.0-SNAPSHOT"

val macroParadiseVersion = "2.1.1"
val kindProjectorVersion = "0.9.4"

// cats libs -- maintain version agreement or whatever
val jjmVersion = "0.1.0"
val qasrlVersion = "0.2.0"

trait CommonModule extends ScalaModule with ScalafmtModule with PublishModule {

  def platformSegment: String

  def sources = T.sources (
    millSourcePath / s"src",
    millSourcePath / s"src-$platformSegment"
  )

  def scalaVersion = thisScalaVersion

  def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:higherKinds",
    "-Ypartial-unification"
  )
  def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
    ivy"org.scalamacros:::paradise:$macroParadiseVersion",
    ivy"org.spire-math::kind-projector:$kindProjectorVersion"
  )

  def publishVersion = thisPublishVersion

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "org.julianmichael",
    url = "https://github.com/julianmichael/qasrl-bank-scala",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("julianmichael", "qasrl-bank-scala"),
    developers = Seq(
      Developer("julianmichael", "Julian Michael", "https://github.com/julianmichael")
    )
  )
}

trait JvmPlatform extends CommonModule {
  def platformSegment = "jvm"
}

trait JsPlatform extends CommonModule with ScalaJSModule {
  def scalaJSVersion = T(thisScalaJSVersion)
  def platformSegment = "js"
}

trait QasrlBankModule extends CommonModule {

  def artifactName = "qasrl-bank"

  def millSourcePath = build.millSourcePath / "qasrl-bank"

  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.julianmichael::qasrl::$qasrlVersion",
  )
}

object `qasrl-bank` extends Module {
  object jvm extends QasrlBankModule with JvmPlatform
  object js extends QasrlBankModule with JsPlatform
}

trait QasrlBankServiceModule extends CommonModule {

  def artifactName = "qasrl-bank-service"

  def millSourcePath = build.millSourcePath / "qasrl-bank-service"

  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.julianmichael::jjm-io::$jjmVersion"
  )
}

object `qasrl-bank-service` extends Module {
  object jvm extends QasrlBankServiceModule with JvmPlatform {

    def moduleDeps = Seq(`qasrl-bank`.jvm)
  }
  object js extends QasrlBankServiceModule with JsPlatform {

    def moduleDeps = Seq(`qasrl-bank`.js)
  }
}
