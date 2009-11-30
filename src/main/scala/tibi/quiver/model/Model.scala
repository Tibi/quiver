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
object Model extends Model with NamedMetaMapper[Model] with CRUDify[Long, Model] {
  override def fieldOrder = List(name, brand, productType)
}

