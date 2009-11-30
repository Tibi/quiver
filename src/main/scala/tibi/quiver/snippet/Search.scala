package tibi.quiver.snippet

import scala.xml._
import scala.collection.mutable.{Map => MutMap}

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._  
import net.liftweb.http._

import model._
import LangImplicits._

class SimpleSearch {
  
  object term extends RequestVar[Box[String]](Empty)
  
  def form(html: NodeSeq): NodeSeq = {
    bind("form", html, "field" -> SHtml.text(term openOr "", t => term(Full(t))))
  }
  
  def results(html: NodeSeq): NodeSeq = term.is match {
    case Full(termStr) => Model.findByName(termStr) flatMap (model => <ul>{model.name}</ul>)
    case _ => Nil
  }
}


class AdvancedSearch {
  
  val operators = MutMap[Property, String]()
  val values = MutMap[Property, String]()
  
  def form(html: NodeSeq): NodeSeq = currentProductType.is match {
    case Full(pt) => pt.properties.flatMap(prop =>
      bind("prop", html,
           "name" -> Text(prop.name),
           "operator" -> SHtml.select(prop.operators.map(s => (s, s)),
                                            Empty,
                                            op => operators(prop) = op),
           "field" -> field(prop))
    )
    case _ => Nil
  }
  
  private def field(prop: Property) = {
    val value = Box(values get prop)
    prop.dataType match {
      case PropertyType.String | PropertyType.Int | PropertyType.Decimal =>
        SHtml.text(value.openOr(""), values(prop) = _)
      case PropertyType.Bool => SHtml.checkbox(value === "y",
        (v: Boolean) => values(prop) = if (v) "y" else "n")
    }
  }
    
    
}
