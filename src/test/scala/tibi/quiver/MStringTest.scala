package tibi.quiver

import org.specs._

import model.MultiString._

object MStringTest extends Specification {
  "stored == retrieved" in {
    val en = "in english"
    val fr = "en français"
    val ms = MString("en" -> en, "fr" -> fr)
    ms("en") must_== en
    ms("fr") must_== fr
  }
}
