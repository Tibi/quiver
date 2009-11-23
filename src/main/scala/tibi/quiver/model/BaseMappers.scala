package tibi.quiver.model

import net.liftweb.common._
import net.liftweb.mapper._
import MultiString._

trait MyMapper[T <: MyMapper[T]] extends LongKeyedMapper[T]  with IdPK {
  self: T =>
}
trait MyMetaMapper[T <: MyMapper[T]] extends LongKeyedMetaMapper[T] {
	self: T with LongKeyedMetaMapper[T] =>  
}

trait NamedMapper[T <: NamedMapper[T]] extends MyMapper[T] {
  self: T =>
  object name extends MappedString(this, 200)
  override def toString: String = name
}
trait NamedMetaMapper[T <: NamedMapper[T]] extends MyMetaMapper[T] {
	self: T with MyMetaMapper[T] =>  
}

trait MNamedMapper[T <: MNamedMapper[T]] extends MyMapper[T] {
  self: T =>
  object name extends MappedMString(this, 200)
}
trait MNamedMetaMapper[T <: MNamedMapper[T]] extends MyMetaMapper[T] {
  self: T with MyMetaMapper[T] =>
  
  def findByName(nameIn: String, lang: Lang): Box[T] =
    findAll.filter(_.name.is(lang) == nameIn) match {
      case List(one) => Full(one)
      case List(_, _) => Failure("found several named " + nameIn)
      case _ => Empty
    }
 // override def fieldOrder = List(name)
}
