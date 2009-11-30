package tibi.quiver.snippet

import scala.xml._

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._  
import net.liftweb.mapper.By
import net.liftweb.http._

import model._
import MultiString._
import view.ImageServer

import LangImplicits._

//object currentCategory extends SessionVar[Box[Category]](Empty)

object currentProductType extends SessionVar[Box[ProductType]](Empty)

class Cat {
  // The current category: take it from the "catId" or "catName" param 
  lazy val cat: Box[Category] = S.param("catName") match {
    case Full(catName) => Category.findByName(catName, lang)
    case _ => S.param("catId") match {
      case Full(catId) => Category.find(catId)
      case _ => Empty
    }
  }
  
  /*
  def listRoot(xhtml: NodeSeq): NodeSeq = {
    currentCategory(Empty)
    list(xhtml)
  }
  */
  
  def name(xhtml: NodeSeq): NodeSeq = cat match {
    case Full(category) => bind("category", xhtml,
      "name" -> Text(category.name))
    case _ => Nil
  }
    
  def listSubCategories(xhtml: NodeSeq): NodeSeq = {
    Category.findAll(By(Category.parent, cat)).flatMap(
      category => bind("category", xhtml, "name" -> category.name,
                  //TODO use sitemap to get links
                  "name_and_link" -> SHtml.link("/cat/" + category.name.is(lang),
                                                () => null,  //() => currentCategory(Full(category)),
                                                Text(category.name),
                                                ("class", "category"))
    )
  )}
  
  def new_one(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processNew(): Any = Category.create.name(MString(lang.is -> name)).parent(cat).save
    bind("category", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("New Category", processNew))
  }
  

  def listPTs(xhtml: NodeSeq): NodeSeq = cat match {
    case Full(category) => category.productTypes.flatMap(
	    productType => bind("product_type", xhtml, "name" -> productType.name,
	                  "name_and_link" -> SHtml.link("/product_type.html",
	                                                () => currentProductType(Full(productType)),
	                                                Text(productType.name),
	                  								("class", "product_type"))))
    case _ => Nil
  }

  def newPT(xhtml: NodeSeq): NodeSeq = {
    var name = ""
    def processNew(): Any = ProductType.create.name(MString(lang.is -> name)).category(cat).save
    bind("product_type", xhtml, "name" -> SHtml.text(name, name = _),
      "submit" -> SHtml.submit("New Product Type", processNew))
  }
}
