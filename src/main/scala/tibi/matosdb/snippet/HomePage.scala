package tibi.matosdb.snippet

import scala.xml._
import net.liftweb._

import net.liftweb.mapper.By
import http._
import js._
import util._
import S._
import SHtml._
import scala.xml._
import Helpers._  

import tibi.matosdb.model._

object currentSport extends SessionVar[Box[Sport]](Empty)
object currentProductType extends SessionVar[Box[ProductType]](Empty)
object currentBrand extends SessionVar[Box[Brand]](Empty)

class Sports {
  
  def list(xhtml: NodeSeq): NodeSeq = Sport.findAll.flatMap(
    sport => bind("sport", xhtml, "name" -> sport.name.is,
                  "name_and_link" -> SHtml.link("/product_types.html",
                                                () => currentSport(Full(sport)),
                                                Text(sport.name.is))))

  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = Sport.create.name(name).save
    bind("sport", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("Add", processAdd))
  }
}


class ProductTypes {
  
  val forSport = currentSport.is match {
    case Full(sport) => sport
    case _ => throw new RuntimeException("called without a sport")
  }

  def header(xhtml: NodeSeq): NodeSeq = bind("sport", xhtml,
    "name" -> Text(forSport.name))

  def list(xhtml: NodeSeq): NodeSeq = ProductType.findAll(By(ProductType.sport, forSport)).flatMap(
    productType => bind("product_type", xhtml, "name" -> productType.name.is,
                  "name_and_link" -> SHtml.link("/brands.html",
                                                () => currentProductType(Full(productType)),
                                                Text(productType.name.is))))

  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = ProductType.create.name(name).sport(currentSport.is).save
    bind("product_type", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("Add", processAdd))
  }
}


class Brands {
  def header(xhtml: NodeSeq): NodeSeq = bind("product_type", xhtml,
    "name" -> (currentProductType.is match {
      case Full(productType) => Text(productType.name)
      case _ => Text("no product type")
  }))
  def list(xhtml: NodeSeq): NodeSeq = Brand.findAll.flatMap(
    brand => bind("brand", xhtml, "name" -> brand.name.is,
                  "name_and_link" -> SHtml.link("/models.html",
                                                () => currentBrand(Full(brand)),
                                                Text(brand.name.is))))

  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = Brand.create.name(name).save
    bind("brand", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("Add", processAdd))
  }
}
