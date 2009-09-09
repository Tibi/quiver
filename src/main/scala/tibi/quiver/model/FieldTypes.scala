package tibi.quiver.model

import net.liftweb._ 
import mapper._



class MappedMultiString[T<:Mapper[T]](fieldOwner: T, maxLen: Int) extends MappedString(fieldOwner, maxLen * 4) {

}
