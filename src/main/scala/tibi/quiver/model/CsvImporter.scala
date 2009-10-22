package tibi.quiver.model

import java.io.Reader
import scala.collection.mutable.{Map => MutMap}
import scala.collection.mutable.ListBuffer
import collection.jcl.Conversions.convertList

import au.com.bytecode.opencsv.CSVReader
import net.liftweb.mapper.By
import net.liftweb.util._
import MultiString._


//TODO what about years?

class CsvImporter(val reader: Reader, val lang: Lang, val pt: ProductType, val brand: Brand) {
  
  // Infos aboat the size currently being built
  var sizeName: Box[String] = Empty
  var model: Box[Model] = Empty
  val propVals: MutMap[Property, String] = MutMap()
  def resetSizeInfos = { sizeName = Empty; model = Empty; propVals.clear }

  /** Empty if the import was successful, else contains error messages. */
  val errors = new ListBuffer[String]

  // A cache of known models
  val models: MutMap[String, Model]  = MutMap()

  // Starts the import right in the constructor.
  process

  def process {
    val csv = new CSVReader(reader, ';')  //TODO guess the separator?
    var hasName, hasModel = false
    // Reads the header into a list of functions to handle each cell
    val funcs: Seq[(String) => Unit] = for (head <- csv.readNext) yield head match {
      case "Model" => hasModel = true; handleModel _
      case "Name" => hasName = true; (x: String) => sizeName = Full(x)
      case "" | null => doNothing _
      case propName: String => findProperty(propName) match {
        case Full(prop) => propVals(prop) = _
        case other => errors += ("Unknown property or column: " + other); doNothing _ 
      }
    }
    if (!hasName) errors += "CSV file has no name column."
    if (!hasModel) errors += "CSV file has no model column."
    if (!hasName || !hasModel) return
    
    // Read the other lines and apply the functions for each.
    for (line <- convertList(csv.readAll)) {  // can’t iterate over java lists???
      resetSizeInfos
      zipApply(funcs.toList, line.toList)
      for (sizeNam <- sizeName; modl <- model) {
        // Creates the size only if a size name and a model are defined.
        val size = Size.create.name(sizeNam).model(modl)
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
  
  def findProperty(propName: String): Box[Property] = {
    Property.findByName(propName, lang) match {
      case List(prop) => Full(prop)
      //case prop :: Nil => Full(prop)
      case _ => errors += "Property not found " + propName; Empty
    }
  }
}
