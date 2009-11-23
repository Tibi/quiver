package tibi.quiver.model

import java.io.Reader
import scala.collection.mutable.{Map => MutMap}
import scala.collection.mutable.ListBuffer
import collection.jcl.Conversions.convertList

import au.com.bytecode.opencsv.CSVReader

import net.liftweb.common._
import net.liftweb.mapper.By
import net.liftweb.util._

import MultiString._


//TODO what about years?

/**
 * Imports the sizes from reader for the given product type and brand.
 * 
 * Each line represents a size in csv format, the separator must be ;
 * The first line is a header with property names in the given language + columns called
 * "Model", "Name" (of the size) and "Year" must be present.
 * If the header has an error, nothing is imported.
 * Errors during import are collected in the "errors" attribute.
 */
class CsvImporter(val reader: Reader, val lang: Lang, val pt: ProductType, val brand: Brand) {
 
  /** Error messages, empty if the import was successful. */
  val errors = new ListBuffer[String]
  
  // Infos about the size currently being built
  private var sizeName: Box[String] = Empty
  private var model: Box[Model] = Empty
  private var year: Box[Int] = Empty
  private val propVals: MutMap[Property, String] = MutMap()
  private def resetSizeInfos() { sizeName = Empty; model = Empty; propVals.clear }

  // A cache of known models
  private val models: MutMap[String, Model]  = MutMap()

  // Starts the import right in the constructor.
  process()

  def process() {
    val csv = new CSVReader(reader, ';')  //TODO guess the separator?
    var hasName, hasModel, hasYear = false
    // Reads the header into a list of functions to handle each cell
    val funcs: Seq[(String) => Unit] = for (head <- csv.readNext) yield head match {
      case "Model" => hasModel = true; handleModel _
      case "Name" => hasName = true; (x: String) => sizeName = Full(x)
      case "Year" => hasYear = true; (x: String) => year = Full(x.toInt)
      case "" | null => doNothing _
      case propName: String => Property.findByName(propName, lang) match {
        case Full(prop) => propVals(prop) = _
        case _ => errors += ("Unknown property or column: " + propName); doNothing _ 
      }
    }
    if (!hasName)  errors += "CSV file has no name column."
    if (!hasModel) errors += "CSV file has no model column."
    if (!hasYear)  errors += "CSV file has no Year column."
    if (!hasName || !hasModel || !hasYear) return
    
    // Read the other lines and apply the functions for each.
    for (line <- convertList(csv.readAll)) {  // have to convert from java list
      resetSizeInfos()
      zipApply(funcs.toList, line.toList)
      // Edit the size only if a size name, a model and a year are defined.
      for (sizeNam <- sizeName; modl <- model; yer <- year) {
        val size = Size.findAll(By(Size.model, modl), By(Size.name, sizeNam)) match {
          case List(siz) => siz
          case List(siz, _) => errors += "Several sizes named " + sizeNam + " for model " + modl.name; siz
          case Nil => Size.create.name(sizeNam).model(modl)
        }
        size.year(yer)
        for ((prop, valStr) <- propVals) {
          size.setPropertyValue(prop, valStr)    
        }
        size.save
      }
    }
  }


  /**
   * Given a list of functions and a list of elements, returns the functions applied to the elements.
   */
  def zipApply[T, O](funcs: List[(T) => O], elems: List[T]): List[O] =
    for ((func, elem) <- funcs zip elems) yield func(elem)

  // Used for columns without title
  def doNothing(dummy: String) = {}

  // When the model column is encountered.
  def handleModel(modelName: String): Unit = {
    model = Full(models.get(modelName) match {
      case Some(mod) => mod
      case None => {
        // Model is not in the cache, load it. We suppose a model name is unique in a brand…
        val newModel = Model.findAll(By(Model.name, modelName), By(Model.brand, brand)) match {
          case List(mod) => mod
          case _ => Model.create.name(modelName).brand(brand).productType(pt).saveMe
        }
        models += (modelName -> newModel)  // caches the newly loaded model
        newModel
      }
    })
  }
}
