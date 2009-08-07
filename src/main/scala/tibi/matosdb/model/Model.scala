package tibi.matosdb.model

import scala.xml.NodeSeq

import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._

class Image extends LongKeyedMapper[Image] with IdPK { 
  def getSingleton = Image
  object fileName extends MappedString(this, 100)
  object mimeType extends MappedString(this, 50)
  object data extends MappedBinary(this)
}

object Image extends Image with LongKeyedMetaMapper[Image] 

class Sport extends LongKeyedMapper[Sport] with IdPK { 
  def getSingleton = Sport
  object name extends MappedString(this, 100)
  def productTypes = ProductType.findAll(By(ProductType.sport, this))
}

object Sport extends Sport with LongKeyedMetaMapper[Sport] {
  override def fieldOrder = List(name)
}


class ProductType extends LongKeyedMapper[ProductType] with IdPK {
  def getSingleton = ProductType

  object name extends MappedString(this, 100)
  object sport extends MappedLongForeignKey(this, Sport)
  def sportName = sport.obj.map(_.name.is) openOr "Unknown"
  //TODO object properties extends  Set[Property])
}

object ProductType extends ProductType with LongKeyedMetaMapper[ProductType] {
  override def fieldOrder = List(name, sport)
}


class Brand extends LongKeyedMapper[Brand] with IdPK { 
  def getSingleton = Brand
  object name extends MappedString(this, 100)
  object mainProductType extends MappedLongForeignKey(this, ProductType) {
    override def _toForm = Full(select(ProductType.findAll.map(pt => (pt.id.toString, pt.name)),
                                       Full(is.toString), f => this(f.toInt)))
  }
  object logo extends MappedLongForeignKey(this, Image)
}

object Brand extends Brand with LongKeyedMetaMapper[Brand] with CRUDify[Long, Brand] {
  override def fieldOrder = List(name, mainProductType, logo)
}


class Model extends LongKeyedMapper[Model] with IdPK { 
  def getSingleton = Model
  object name extends MappedString(this, 100)
  object brand extends MappedLongForeignKey(this, Brand)
  object productType extends MappedLongForeignKey(this, ProductType)
}

object Model extends Model with LongKeyedMetaMapper[Model] with CRUDify[Long, Model] {
  override def fieldOrder = List(name, brand, productType)
}


class Size extends LongKeyedMapper[Size] with IdPK { //TODO extends CanHaveProperties
  def getSingleton = Size
  object name extends MappedString(this, 100)
  object model extends MappedLongForeignKey(this, Model)
  //TODO override def isAllowed(prop: Property) = model.prod_type.properties contains prop
}

object Size extends Size with LongKeyedMetaMapper[Size] {
  override def fieldOrder = List(name, model)
}

/*
case class Property(name: String)

trait CanHaveProperties {
  private val vals = Map[Property, String]()
  def setProperty(prop: Property, value: String) {
    if (isAllowed(prop))
      vals.put(prop, value)
  }
  def isAllowed(prop: Property): Boolean
}
*/