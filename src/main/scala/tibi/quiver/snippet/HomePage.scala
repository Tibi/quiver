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
  //implicit def String2MString(str: String): MString = MString(lang.is -> str)
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
    def processAdd(): Any = Sport.create.name(MString(lang.is -> name)).save
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
    def processAdd(): Any = ProductType.create.name(MString(lang.is -> name)).sport(currentSport.is).save
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
                  "name_and_link" -> SHtml.link("/model.html",
                                                () => currentModel(Full(model)),
                                                Text(model.name.is))))
  def add(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processAdd(): Any = Model.create.name(name).brand(forBrand).productType(forProductType).save
    bind("model", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("Add", processAdd))
  }
}

object currentSize extends RequestVar[Box[Size]](Empty)

class ModelSnip {
  val model = currentModel.is.open_!
  
  def header(xhtml: NodeSeq): NodeSeq = bind("model", xhtml,
        "brand" -> model.brand.obj.open_!.name,
        "name" -> model.name)
  
  def properties(xhtml: NodeSeq): NodeSeq =
    currentProductType.is.open_!.properties.flatMap(
    		prop => bind("prop", xhtml, "name" -> Text(prop.name)))
  
  def sizes(xhtml: NodeSeq): NodeSeq =
    model.sizes.flatMap(size => bind("size", xhtml,
        "name" -> size.name,
        "prop_values" -> currentProductType.is.open_!.properties.flatMap(prop => {
          val propVal = size.propertyValue(prop)
          bind("prop", chooseTemplate("size", "prop_values", xhtml),
               "val" -> propVal.openOr(""))}),
        "edit_link" -> link("size_edit", () => currentSize(Full(size)), Text("Edit")),
        "delete_link" -> link("#", () => deleteSize(size), Text("Delete"))))
  
  def add_link(xhtml: NodeSeq): NodeSeq = link("size_edit", () => currentSize(Empty), Text("Add"))
  
  private def deleteSize(size: Size) {
    if (size.delete_!) S.notice("size "+size+" deleted")
    else S.error("impossible to delete "+size)
  }
}


class SizeSnip {
  if (!currentSize.is.isDefined) currentSize(Full(Size.create.model(currentModel.is)))
  val size = currentSize.is.open_!
  
  def edit_form(xhtml: NodeSeq): NodeSeq = bind("form", xhtml,
      "name" -> SHtml.text(size.name, size.name(_)),
      "property_values" -> currentModel.is.open_!.productType.obj.open_!.properties.flatMap(prop => {
        val propName = prop.name.is(lang)
        <div><label for="{propName}">{propName}</label>{
          SHtml.text(size.propertyValue(prop).openOr(""), size.setPropertyValue(prop, _))
        }</div>
      }),
      "submit" -> SHtml.submit("Save", () => { size.save; S.redirectTo("model") }))
}
