package tibi.quiver.snippet

import java.util.Date

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

import model._
import MultiString._

import view.ImageServer

object currentSport extends SessionVar[Box[Sport]](Empty)

object lang extends SessionVar[Lang](English)
object implicits {
  implicit def MMString2String(mmstr: MappedMString[_]): String = mmstr.is(lang.is)
  implicit def String2MString(str: String): MString = MString(lang.is -> str)
}
import implicits._


class Sports {
  def list(xhtml: NodeSeq): NodeSeq = Sport.findAll.flatMap(
    sport => bind("sport", xhtml, "name" -> sport.name,
                  //TODO use sitemap to get links
                  "name_and_link" -> SHtml.link("/product_types.html",
                                                () => currentSport(Full(sport)),
                                                Text(sport.name)))
    )
  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = Sport.create.name(name).save
    bind("sport", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("Add", processAdd))
  }
}


object currentProductType extends SessionVar[Box[ProductType]](Empty)

class ProductTypes {
  
  val forSport = currentSport.is match {
    case Full(sport) => sport
    case _ => throw new RuntimeException("called without a sport")
  }

  def header(xhtml: NodeSeq): NodeSeq = bind("sport", xhtml,
    "name" -> Text(forSport.name))

  def list(xhtml: NodeSeq): NodeSeq = forSport.productTypes.flatMap(
    productType => bind("product_type", xhtml, "name" -> productType.name,
                  "name_and_link" -> SHtml.link("/brands.html",
                                                () => currentProductType(Full(productType)),
                                                Text(productType.name))))

  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = ProductType.create.name(name).sport(currentSport.is).save
    bind("product_type", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("Add", processAdd))
  }
}


object currentBrand extends SessionVar[Box[Brand]](Empty)

class Brands {
  def header(xhtml: NodeSeq): NodeSeq = currentProductType.is match {
    case Full(productType) => bind("product_type", xhtml,
                                   "sport" -> Text(productType.sportName),
                                   "name"  -> Text(productType.name))
    case _ => Text("")
  }
  
  def list(xhtml: NodeSeq): NodeSeq = Brand.findAll(
    By(Brand.mainProductType, currentProductType.is)).flatMap(
	    brand => bind("brand", xhtml,
                   "link" -> SHtml.link("/models.html", () => currentBrand(Full(brand)),
                                        brand.logo.obj match {
                     case Full(img) => <img src={"imageSrv/"+img.id.is}/>
                     case _ => Text(brand.name.is)
  })))
  
  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = Brand.create.name(name).mainProductType(currentProductType.is).save
    bind("brand", xhtml, "name" -> SHtml.text(name, name = _),
                         "submit" -> SHtml.submit("Add", processAdd))
  }
  
  private object uploaded extends RequestVar[Box[FileParamHolder]](Empty) 
  
  def edit(xhtml: NodeSeq): NodeSeq = {
    val brand = currentBrand.is match {
      case Full(b) => b
      case _ => throw new RuntimeException("editing no brand")
    }
    def save(): Any = {
      val image = brand.logo.obj openOr Image.create
      uploaded.is.map(fileParam => image.data(fileParam.file))
      ImageServer.save(image)
      brand.logo(image).save
    }
    bind("form", xhtml, "name" -> SHtml.text(brand.name, brand.name(_)),
                        "file_upload" -> fileUpload(upl => uploaded(Full(upl))),
                        "mainPT" -> brand.mainProductType._toForm,
                        "submit" -> SHtml.submit("Save", save))
  }
}


object currentModel extends SessionVar[Box[Model]](Empty)

class Models {
  def header(xhtml: NodeSeq): NodeSeq = bind("head", xhtml,
    "brand" -> Text(forBrand.name),
    "prod_type" -> Text(forProductType.name))
  
   val forBrand = currentBrand.is match {
    case Full(brand) => brand
    case _ => throw new RuntimeException("called without a brand")
  }
   
   val forProductType = currentProductType.is match {
    case Full(pt) => pt
    case _ => throw new RuntimeException("called without a product type")
  }
   
  def list(xhtml: NodeSeq): NodeSeq = Model.findAll(By(Model.brand, forBrand),
                                                    By(Model.productType, forProductType)).flatMap(
    model => bind("model", xhtml, "name" -> model.name.is,
                  "name_and_link" -> SHtml.link("/model_detail.html",
                                                () => currentModel(Full(model)),
                                                Text(model.name.is))))
  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = Model.create.name(name).brand(forBrand).productType(forProductType).save
    bind("model", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("Add", processAdd))
  }
}
