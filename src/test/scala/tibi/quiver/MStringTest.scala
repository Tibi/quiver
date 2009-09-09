package tibi.quiver

import _root_.junit.framework._
import Assert._

import model.MultiString._

class MStringTest extends TestCase("mstring") {

  def testConstruct = {
    val ms = MString("en" -> "in english")
    assert(ms("en") == "in engleish")
  }
}
