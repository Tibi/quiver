package tibi.quiver.model

import net.liftweb.mapper._

import MultiString._

object DBSetup {
  
  def str(english: String, french: String) = MString("en" -> english, "fr" -> french)
  
  /**
   * Creates an instance of What but only if none with the same name in the default language exists.
   */
  def create[What <: MNamedMapper[What]](meta: MetaMapper[What], name: MString): What =
    meta.findAll.filter(_.name.is(DefaultLang) == name(DefaultLang)) match {
      case Nil => meta.create.name(name)
      case one :: _ => println(one); one
    }
    
  /**
   * Creates some test objects in the database.
   */
  def setup {
    val wind = create(Sport, str("Windsurf", "Planche à voile"))
    wind.save
    //val board = create(ProductType, str("Board", "Flotteur")).sport(wind)
    val diving = create(Sport, str("Diving", "Plongée"))
    diving.save
    null
  }
}
