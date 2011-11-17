package tibi.quiver.snippet

import java.util.Date

import scala.xml._

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._  
import net.liftweb.mapper.By
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._

import tibi.quiver.model._
import MultiString._
import tibi.quiver.view.ImageServer


object lang extends SessionVar[Lang](English)
object LangImplicits {
  implicit def MMString2String(mmstr: MappedMString[_]): String = mmstr.is(lang.is)
  //implicit def String2MString(str: String): MString = MString(lang.is -> str)
}
import LangImplicits._


object currentBrand extends SessionVar[Box[Brand]](Empty)

class Brands {
  def header(xhtml: NodeSeq): NodeSeq = currentProductType.is match {
    case Full(productType) => bind("product_type", xhtml,
                                   //"category" -> Text(productType.categoryName),
                                   "name"  -> Text(productType.name))
    case _ => Text("")
  }
  
  def list(xhtml: NodeSeq): NodeSeq = Brand.findAll(By(Brand.mainProductType, currentProductType.is)
  ).flatMap(
	    brand => bind("brand", xhtml,
                   "link" -> SHtml.link("/brand_models.html", () => currentBrand(Full(brand)),
                                        brand.logo.obj match {
					                      case Full(img) => <img src={"imageSrv/"+img.id.is}/>
					                      case _ => Text(brand.name.is)
                   						}, ("class", "brand"))
	    		 )
  )
  
  def new_one(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processNew(): Any = Brand.create.name(name).mainProductType(currentProductType.is).save
    bind("brand", xhtml, "name" -> SHtml.text(name, name = _),
                         "submit" -> SHtml.submit("New", processNew))
  }
  
  private object uploaded extends RequestVar[Box[FileParamHolder]](Empty) 
  
  def edit(xhtml: NodeSeq): NodeSeq = {
    val brand = currentBrand.is match {
      case Full(b) => b
      case _ => throw new RuntimeException("editing no brand")
    }
    def save(): Unit = {
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
    "prod_type" -> Text(forProductType.name)
  )
  
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
                  "name_and_link" -> SHtml.link("/model",
                                                () => currentModel(Full(model)),
                                                Text(model.name.is), ("class", "model")),
                  "delete_link" -> link("brand_models", () => model.delete_!, Icons.delete)))
  def new_one(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processNew(): Any = Model.create.name(name).brand(forBrand).productType(forProductType).save
    bind("model", xhtml, "name" -> SHtml.text(name, name = _),
    					 "submit" -> SHtml.submit("New", processNew))
  }
}

object currentSize extends RequestVar[Box[Size]](Empty)

class ModelSnip {
  val model = currentModel.is.open_!
  val brand = model.brand.obj.open_!
  
  def header(xhtml: NodeSeq): NodeSeq = bind("model", xhtml,
        "brand" -> brand.name,
        "name" -> model.name)
  
  def properties(xhtml: NodeSeq): NodeSeq =
    currentProductType.is.open_!.properties.flatMap(
    		prop => bind("prop", xhtml, "name" -> Text(prop.name), "unit" -> prop.unit))
  
  def sizes(xhtml: NodeSeq): NodeSeq =
    model.sizes.flatMap(size => bind("size", xhtml,
        "name" -> size.name,
        "prop_values" -> currentProductType.is.open_!.properties.flatMap(prop => {
          bind("prop", chooseTemplate("size", "prop_values", xhtml),
               "val" -> size.propertyValueFormatted(prop).openOr(""))}),
        "edit_link" -> link("size_edit", () => currentSize(Full(size)), Text("Edit")),
        "delete_link" -> link("model.html", () => deleteSize(size), Text("Delete"))))
  
  def new_link(xhtml: NodeSeq): NodeSeq =
    link("size_edit", () => currentSize(Empty), Text("New Size"))
  
  def tableSorter(xhtml: NodeSeq): NodeSeq = {
    import net.liftweb.widgets.tablesorter.TableSorter
    TableSorter("#size-table")
  }
  
  private def deleteSize(size: Size) {
    if (size.delete_!) S.notice("size "+size+" deleted")
    else S.error("impossible to delete "+size)
  }
}


class SizeSnip {
  // Creates a new Size if none is set: New was clicked, not Edit
  if (!currentSize.is.isDefined)
    currentSize(Full(Size.create.model(currentModel.is)))
  // FIXME all those open_! are bad!!
  val size = currentSize.is.open_!
  val model = size.model.obj.open_!
  val brand = model.brand.obj.open_!
  val productType = model.productType.obj.open_!
  
  def header(xhtml: NodeSeq): NodeSeq = bind("size", xhtml,
        "brand" -> brand.name,
        "model" -> model.name,
        "size" -> size.name)
  
  private def field(prop: Property, size: Size) = {
    val propVal = size.propertyValue(prop)
    prop.dataType match {
      case PropertyType.String | PropertyType.Int | PropertyType.Decimal =>
        SHtml.text(size.propertyValueFormatted(prop).openOr(""), size.setPropertyValue(prop, _))
      case PropertyType.Bool => SHtml.checkbox(propVal.isDefined && propVal.open_!.valBool,
        (v: Boolean) => size.setPropertyValue(prop, if (v) "y" else "n"))
    }
  }
  
  def edit_form(xhtml: NodeSeq): NodeSeq = bind("form", xhtml,
      "name" -> size.name.toForm,
      "year" -> size.year.toForm,
      "property_values" -> productType.properties.flatMap(prop => bind("pv",
          chooseTemplate("form", "property_values", xhtml),
          "label" -> prop.name.is(lang),
    	  "value" -> field(prop, size),  //TODO use -%>
          "unit" -> prop.unit.is) ),
      "submit" -> SHtml.submit("Save", () => { size.save; S.redirectTo("model") })
  )
}


class CsvImport {

  private object uploaded extends RequestVar[Box[FileParamHolder]](Empty)

  def form(xhtml: NodeSeq): NodeSeq = bind("form", xhtml,
    "file_upload" -> fileUpload(upl => uploaded(Full(upl))),
    "submit" -> SHtml.submit("Import", save))

  def save(): Unit = {
    for (fileParam <- uploaded.is; pt <- currentProductType.is; brand <- currentBrand.is)
      (new CsvImporter(new java.io.InputStreamReader(fileParam.fileStream), lang.is, pt, brand)).errors foreach { S.error(_) }
  }
}