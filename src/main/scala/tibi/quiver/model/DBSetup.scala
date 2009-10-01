package tibi.quiver.model

import net.liftweb.mapper._
import net.liftweb.util._

import MultiString._

object DBSetup {
  
  def str(english: String, french: String) = MString("en" -> english, "fr" -> french)
  
  /**
   * Creates an instance of What but only if none with the same name in the default language exists.
   */
  def create[What <: MNamedMapper[What]](meta: MetaMapper[What], name: MString): What = {
    val res = meta.findAll.filter(_.name.is(DefaultLang) == name(DefaultLang)) match {
      case Nil => meta.create.name(name)
      case one :: _ => one
    }
    if (res.id == -1) println("created "+res)
    res
  }
  
  def associateProps2PT(pt: ProductType, props: Seq[Property]) {
    var i = 0
    for (prop <- props) {
      ProductTypeProperty.create.property(prop).productType(pt).order(i).save
      i += 1
    }
  }
    
  /**
   * Creates some test objects in the database.
   */
  def setup {
    val wind = create(Sport, str("Windsurf", "Planche à voile"))
    if (wind.id != -1) return
    wind.save
    val board = create(ProductType, str("Board", "Flotteur")).sport(wind)
    val sail = create(ProductType, str("Sail", "Voile")).sport(wind)
    val mast = create(ProductType, str("Mast", "Mât")).sport(wind)
    val boom = create(ProductType, str("Boom", "Wishbone")).sport(wind)
    List(mast, boom) foreach { _.save } // to remove

  // Board properties
  val prog = create(Property, str("Program", "Programme")).dataType(PropertyType.String)
  val width = create(Property, str("Width", "Largeur")).unit("cm").dataType(PropertyType.Decimal)
  val length = create(Property, str("Length", "Longueur")).unit("cm").dataType(PropertyType.Int)
  val vol = create(Property, str("Volume", "Volume")).unit("l").dataType(PropertyType.Int)
  val weight = create(Property, str("Weight", "Poid")).unit("kg").dataType(PropertyType.Decimal)
  val boardProps = List(prog, width, length, vol, weight)
  boardProps foreach { _.save }

  // Sail properties
  val surface = create(Property, str("Surface", "Surface")).unit("m²").dataType(PropertyType.Decimal)
  val mastLength = create(Property, str("Mast Length", "Longueur du mat")).unit("cm").dataType(PropertyType.Int)
  val luffLength = create(Property, str("Luff Length", "Longueur du guindant")).unit("cm").dataType(PropertyType.Int)
  val boomLength = create(Property, str("Boom Length", "Longueur au wish")).unit("cm").dataType(PropertyType.Int)
  val numBatten = create(Property, str("Number of battens", "Nombre de lattes")).unit("").dataType(PropertyType.Int)
  val numCams = create(Property, str("Number of cambers", "Nombre de cambers")).unit("").dataType(PropertyType.Int)
  val sailProps = List(surface, mastLength, luffLength, boomLength, numBatten, numCams, weight)
  sailProps foreach { _.save }

    if (board.id == -1) { // TODO allow adding new props
      board.save
      associateProps2PT(board, boardProps)
    }
    
    if (sail.id == -1) {
      sail.save
      associateProps2PT(sail, sailProps)
    }
    null
  }
}
