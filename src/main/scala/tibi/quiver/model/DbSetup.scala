package tibi.quiver.model

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.mapper._

import MultiString._

object DbSetup {
  
  def str(english: String, french: String) = MString("en" -> english, "fr" -> french)
  
  /**
   * Creates an instance of What but only if none with the same name in the default language exists.
   * Returns the created instance or the existing one.
   * Name is updated in other languages.
   */
  def create[What <: MNamedMapper[What]](meta: MNamedMetaMapper[What], name: MString): What = create(meta, name, true)
  def create[What <: MNamedMapper[What]](meta: MNamedMetaMapper[What], name: MString, save: Boolean): What = {
    val res = meta.findByName(name(DefaultLang), DefaultLang) match {
      case Empty => meta.create
      case Full(one) => one
      case Failure(msg, cause, _) => throw new RuntimeException("not found " + name, cause openOr null)
    }
    res.name(name)
    if (save) saveIt(res)
    res
  }

  // like saveMe but logging.  TODO replace with mapper’s logging
  def saveIt[T <: MyMapper[T]](stuff: MyMapper[T]): T = {
    if (stuff.id == -1) println("created " + stuff)
    stuff.saveMe
  }

  /**
   * Creates a property if none with the same name exists.
   * Returns the created or existing property.
   */
  def createProp(name: MString, typ: PropertyType.PropertyType): Property =
    saveIt(create(Property, name, false).dataType(typ))
  def createProp(name: MString, typ: PropertyType.PropertyType, unit: String): Property =
    saveIt(create(Property, name, false).dataType(typ).unit(unit))

  def createPT(name: MString, category: Category): ProductType = 
    saveIt(create(ProductType, name, false).category(category))
  
  def associateProps2PT(pt: ProductType, props: List[Property]) {
    val existingProperties = pt.fetchProperties
    var i = existingProperties.size
    for (prop <- props -- existingProperties) {
      saveIt(ProductTypeProperty.create.property(prop).productType(pt).order(i))
      i += 1
    }
  }

  /**
   * Creates some test objects in the database. 
   */
  def setup {
    val wind = create(Category, str("Windsurf", "Planche à voile"))
    val board = createPT(str("Board", "Flotteur"), wind)
    val sail = createPT(str("Sail", "Voile"), wind)
    val mast = createPT(str("Mast", "Mât"), wind)
    val boom = createPT(str("Boom", "Wishbone"), wind)

    // Board properties
    val prog = createProp(str("Program", "Programme"), PropertyType.String)
    val width = createProp(str("Width", "Largeur"), PropertyType.Decimal, "cm")
    val length = createProp(str("Length", "Longueur"), PropertyType.Int, "cm")
    val vol = createProp(str("Volume", "Volume"), PropertyType.Int, "l")
    val weight = createProp(str("Weight", "Poid"), PropertyType.Decimal, "kg")
    val boardProps = List(prog, width, length, vol, weight)

    // Sail properties
    val surface = createProp(str("Surface", "Surface"), PropertyType.Decimal, "m²")
    val mastLength = createProp(str("Mast", "Mât"), PropertyType.Int, "cm")
    val altMast = createProp(str("Alternative Mast", "Mât alternatif"), PropertyType.Int, "cm")
    val luffLength = createProp(str("Luff Length", "Longueur du guindant"), PropertyType.Int, "cm")    
    //val baseLength = createProp(str("Base Length", "Longueur de rallonge de pied de mât"), "cm", PropertyType.Int)
    //val headLength = createProp(str("Extended Head Length", "Longueur de rallonge en tête"), "cm", PropertyType.Int)
    val headVario_? = createProp(str("Vario Head?", "Têtière reglable?"), PropertyType.Bool)
    val boomLength = createProp(str("Boom Length", "Longueur au wish"), PropertyType.Int, "cm")
    val numBatten = createProp(str("Number of battens", "Nombre de lattes"), PropertyType.Int)
    val numCams = createProp(str("Number of cambers", "Nombre de cambers"), PropertyType.Int)
    val sailProps = List(surface, mastLength, altMast, luffLength, headVario_?, boomLength, numBatten, numCams, weight)

    associateProps2PT(board, boardProps)
    associateProps2PT(sail, sailProps)
  }
}
