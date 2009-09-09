package tibi.quiver.model

import net.liftweb._ 
import mapper._

object MultiString {

  /** The type used to specify the language. */
  type Lang = String

  /**
   * A multilingual immutable string.
   * 
   * val mstr = MString("en" -> "in english")
   * mstr("en")  // returns "in english"
   * mstr + ("de" -> "auf deutsch")  // returns a new MString with both languages set
   */
  case class MString (map: Map[Lang, String]) {

    private val data = map
    
    def this(pair: Pair[Lang, String]) = this(Map(pair))
    
    def apply(lang: Lang) = data(lang)
    
    def + (pair: Pair[Lang, String]) = MString(data + pair)
  }
  
  object MString {
    def apply(pair: Pair[Lang, String]) = new MString(pair)
  }
 

  class MappedMultiString[T <: Mapper[T]] (fieldOwner: T, maxLen: Int)
  	extends MappedString(fieldOwner, maxLen * 4) {
	
  }
}
