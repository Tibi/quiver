package tibi.quiver.model

import java.math.MathContext

import scala.xml.NodeSeq

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.mapper._ 
import net.liftweb.http.SHtml._ 

import MultiString._


class Size extends NamedMapper[Size] with OneToMany[Long, Size]{
  object model extends MappedLongForeignKey(this, Model)
  object propertyValues extends MappedOneToMany(PropertyValue, PropertyValue.owner) //TODO OrderBy
                        with Owned[PropertyValue] with Cascade[PropertyValue]
  object year extends MappedInt(this)

  def propertyValue(prop: Property): Box[PropertyValue] =
    Box(propertyValues.filter(_.property == prop).toList)

  def propertyValueFormatted(prop: Property): Box[String] =
    propertyValue(prop).map(_.format)
                                           
  def setPropertyValue(prop: Property, value: String): Unit = {
    propertyValue(prop) match {
      case Full(pv) => if (value == "") pv.delete_! else pv.setValue(value).save
      case Empty => if (value != "")
        propertyValues += PropertyValue.create.owner(this).property(prop).setValue(value)
      case _ => error("shit happened while setting property value " + prop + " to " + value)
    }
  }
  def getSingleton = Size
}
object Size extends Size with NamedMetaMapper[Size] {
  override def fieldOrder = List(name, model)
}


object PropertyType extends Enumeration {
  type PropertyType = Value
  val String = Value("string")
  val Int = Value("integer")
  val Decimal = Value("decimal")
  val Bool = Value("boolean")
}

class Property extends MNamedMapper[Property] {
  object unit extends MappedString(this, 20)
  object dataType extends MappedEnum(this, PropertyType)
  def operators = dataType.is match {
    case PropertyType.Int => List("=", "<", ">")
    case PropertyType.Decimal => List("=", "<", ">")
    case PropertyType.String => List()
    case PropertyType.Bool => List()
  }
  def getSingleton = Property
}
object Property extends Property with MNamedMetaMapper[Property] with CRUDify[Long, Property] {
  override def fieldOrder = List(name, unit, dataType)
}


class PropertyValue extends MyMapper[PropertyValue] {
  object property extends MappedLongForeignKey(this, Property)
  object owner extends MappedLongForeignKey(this, Size)
  // The value fields, only one is filled.
  object valStr extends MappedString(this, 1000)
  object valInt extends MappedLong(this)
  object valDeci extends MappedDecimal(this, MathContext.DECIMAL32, 2)
  object valBool extends MappedBoolean(this)
  
  def dataType = property.obj.open_!.dataType.is
  
  def format: String = dataType match {
    case PropertyType.String => valStr.is
    case PropertyType.Int => valInt.is.toString
    case PropertyType.Decimal => valDeci.is.toString
    case PropertyType.Bool => if (valBool.is) "yes" else "no"
  }
  
  def setValue(value: String) = dataType match {
    case PropertyType.String => valStr(value)
    case PropertyType.Int => valInt(value.toInt)
    case PropertyType.Decimal => valDeci(BigDecimal(value))
    case PropertyType.Bool => valBool(value startsWith "y")
  }
     
  def getSingleton = PropertyValue
}
object PropertyValue extends PropertyValue with MyMetaMapper[PropertyValue] {
  override def fieldOrder = List(property)
}


class ProductTypeProperty extends MyMapper[ProductTypeProperty] {
  object productType extends MappedLongForeignKey(this, ProductType)
  object property extends MappedLongForeignKey(this, Property)
  object order extends MappedInt(this)
  def getSingleton = ProductTypeProperty
}
object ProductTypeProperty extends ProductTypeProperty with MyMetaMapper[ProductTypeProperty] {
  def join(pt: ProductType, prop: Property, order: Int) =
    create.productType(pt).property(prop).order(order).save
}