package tibi.quiver.model

import scala.xml.NodeSeq

import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._

import MultiString._

class Image extends LongKeyedMapper[Image] with IdPK { 
  object fileName extends MappedString(this, 100)
  object mimeType extends MappedString(this, 50)
  object saveTime extends MappedDateTime(this)
  object data extends MappedBinary(this)
  def getSingleton = Image
}

object Image extends Image with LongKeyedMetaMapper[Image] 

class Sport extends LongKeyedMapper[Sport] with IdPK { 
  object name extends MappedMString(this, 100)
  def productTypes = ProductType.findAll(By(ProductType.sport, this))
  def getSingleton = Sport
}

object Sport extends Sport with LongKeyedMetaMapper[Sport] {
  override def fieldOrder = List(name)
}


class ProductType extends LongKeyedMapper[ProductType] with IdPK {
  object name extends MappedMString(this, 100)
  object sport extends MappedLongForeignKey(this, Sport)
  def sportName = sport.obj.open_!.name
  def properties = for (ptp <- ProductTypeProperty.findAll(By(ProductTypeProperty.productType, this.id)))
    yield ptp.property.obj
  def getSingleton = ProductType
}

object ProductType extends ProductType with LongKeyedMetaMapper[ProductType] {
  override def fieldOrder = List(name, sport)
}

class ProductTypeProperty extends LongKeyedMapper[ProductTypeProperty] with IdPK {
  object productType extends MappedLongForeignKey(this, ProductType)
  object property extends MappedLongForeignKey(this, Property)
  object order extends MappedInt(this)
  def getSingleton = ProductTypeProperty
}

object ProductTypeProperty extends ProductTypeProperty with LongKeyedMetaMapper[ProductTypeProperty] {
  def join(pt: ProductType, prop: Property, order: Int) =
    create.productType(pt).property(prop).order(order).save
}

class Brand extends LongKeyedMapper[Brand] with IdPK { 
  object name extends MappedString(this, 100)
  object mainProductType extends MappedLongForeignKey(this, ProductType) {
    override def _toForm = Full(select(ProductType.findAll.map(pt => (pt.id.toString, pt.name.toString)), //TODO get name in the right language
                                       Full(is.toString), f => this(f.toInt)))
  }
  object logo extends MappedLongForeignKey(this, Image)
  def getSingleton = Brand
}

object Brand extends Brand with LongKeyedMetaMapper[Brand] with CRUDify[Long, Brand] {
  override def fieldOrder = List(name, mainProductType, logo)
}


class Model extends LongKeyedMapper[Model] with IdPK { 
  object name extends MappedString(this, 100)
  object brand extends MappedLongForeignKey(this, Brand)
  object productType extends MappedLongForeignKey(this, ProductType)
  def sizes = Size.findAll(By(Size.model, this.id))
  def getSingleton = Model
}

object Model extends Model with LongKeyedMetaMapper[Model] with CRUDify[Long, Model] {
  override def fieldOrder = List(name, brand, productType)
}


class Size extends LongKeyedMapper[Size] with IdPK {
  object name extends MappedString(this, 100)
  object model extends MappedLongForeignKey(this, Model)
  def propertyValues = PropertyValue.findAll(By(PropertyValue.owner, this.id))
  def getSingleton = Size
}

object Size extends Size with LongKeyedMetaMapper[Size] {
  override def fieldOrder = List(name, model)
}

object PropertyType extends Enumeration {
  type PropertyType = Value
  val String = Value("string")
  val Int = Value("integer")
  val Decimal = Value("decimal")
  val Date =  Value("date")
  val Bool = Value("boolean")
}

class Property extends LongKeyedMapper[Property] with IdPK {
  object name extends MappedMString(this, 100)
  object unit extends MappedString(this, 20)
  object type_ extends MappedEnum(this, PropertyType)
  def getSingleton = Property
}

object Property extends Property with LongKeyedMetaMapper[Property] with CRUDify[Long, Property] {
  override def fieldOrder = List(name, unit, type_)
}

class PropertyValue extends LongKeyedMapper[PropertyValue] with IdPK {
  object property extends MappedLongForeignKey(this, Property)
  object owner extends MappedLongForeignKey(this, Size)
  object valStr extends MappedString(this, 1000)
  object valInt extends MappedLong(this)
  //object valDeci extends MappedDecimal(this)
  def getSingleton = PropertyValue
}

object PropertyValue extends PropertyValue with LongKeyedMetaMapper[PropertyValue] {
  override def fieldOrder = List(property)
}
