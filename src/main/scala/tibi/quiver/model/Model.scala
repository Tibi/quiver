package tibi.quiver.model

import java.math.MathContext

import scala.xml.NodeSeq

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.mapper._ 
import net.liftweb.http.SHtml._ 

import MultiString._


class Image extends MyMapper[Image] { 
  object fileName extends MappedString(this, 100)
  object mimeType extends MappedString(this, 50)
  object saveTime extends MappedDateTime(this)
  object data extends MappedBinary(this)
  def getSingleton = Image
}
object Image extends Image with MyMetaMapper[Image] 


class Category extends MNamedMapper[Category]  with OneToMany[Long, Category] { 
  object parent extends MappedLongForeignKey(this, Category)
  object subCategories extends MappedOneToMany(Category, Category.parent) 
                       with Owned[Category] with Cascade[Category]
  def productTypes = ProductType.findAll(By(ProductType.category, this))
  def getSingleton = Category
}
object Category extends Category with MNamedMetaMapper[Category]


class ProductType extends MNamedMapper[ProductType] {
  object category extends MappedLongForeignKey(this, Category)
  def categoryName = category.obj.open_!.name
  lazy val properties = fetchProperties
  def fetchProperties = ProductTypeProperty.findAll(
                          By(ProductTypeProperty.productType, this.id),
                          OrderBy(ProductTypeProperty.order, Ascending)).map(_.property.obj.open_!)
  def getSingleton = ProductType
}
object ProductType extends ProductType with MNamedMetaMapper[ProductType] {
  override def fieldOrder = List(name, category)
}


class Brand extends NamedMapper[Brand] { 
  object mainProductType extends MappedLongForeignKey(this, ProductType) {
    override def _toForm = Full(select(ProductType.findAll.map(pt => (pt.id.toString, pt.name.toString)), //TODO get name in the right language
                                       Full(is.toString), f => this(f.toInt)))
  }
  object logo extends MappedLongForeignKey(this, Image)
  def getSingleton = Brand
}
object Brand extends Brand with NamedMetaMapper[Brand] with CRUDify[Long, Brand] {
  override def fieldOrder = List(name, mainProductType, logo)
}


class Model extends NamedMapper[Model] {
  object brand extends MappedLongForeignKey(this, Brand)
  object productType extends MappedLongForeignKey(this, ProductType)
  def sizes = Size.findAll(By(Size.model, this.id))
  def getSingleton = Model
}
object Model extends Model with LongKeyedMetaMapper[Model] with CRUDify[Long, Model] {
  override def fieldOrder = List(name, brand, productType)
}


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