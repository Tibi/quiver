package tibi.quiver.model

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
}