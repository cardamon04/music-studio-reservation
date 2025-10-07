import mill._
import $ivy.`com.lihaoyi::mill-contrib-playlib:`,  mill.playlib._

object studiobackend extends RootModule with PlayModule {

  // Upgrading project Scala version to 3.x; ensure libraries and Play version are compatible.
  def scalaVersion = "3.3.2"
  def playVersion = "3.0.9"
  def twirlVersion = "2.0.9"

  object test extends PlayTests
}
