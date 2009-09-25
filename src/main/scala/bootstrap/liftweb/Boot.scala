package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, ConnectionIdentifier}
import _root_.java.sql.{Connection, DriverManager}
import _root_.tibi.quiver.model._

import tibi.quiver.view.ImageServer

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    
    // Defines the database to use.
    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    
    // package to search for snippets, models and views
    LiftRules.addToPackages("tibi.quiver")
    
    // Model classes to map to the database
    Schemifier.schemify(true, Log.infoF _, User, Image, Sport, ProductType, Brand, Model, Size,
                        Property, PropertyValue, ProductTypeProperty)
    DBSetup.setup

    // Builds the menu (SiteMap)
    val entries = Menu(Loc("Home", List("index"), "Home"))::
                  Menu(Loc("Brands", List("brands"), "Brands"), Brand.menus:_*)::
                  Menu(Loc("Edit Brand", List("brand_edit"), "edit brand"))::
                  Menu(Loc("Product Types", List("product_types"), "Product Types"))::
                  Property.menus:::
                  Menu(Loc("Models", List("models"), "Models"))::
                  Menu(Loc("Model", List("model"), "Model"))::
                  Menu(Loc("Model edit", List("model_edit"), "Edit Model"))::
                  Menu(Loc("Size edit", List("size_edit"), "Edit Size"))::
                  User.sitemap:::
                  Nil
    LiftRules.setSiteMap(SiteMap(entries:_*))
    
    // Adds our image server to the request processing chain.
    LiftRules.dispatch.append(ImageServer.matcher)
    
    // Shows the spinny image when an Ajax call starts.
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Makes the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: provider.HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}


object DBVendor extends ConnectionManager {

  Class.forName("org.h2.Driver")

  def newConnection(name: ConnectionIdentifier): Box[Connection] = try {
    Full(DriverManager.getConnection("jdbc:h2:db/quiver;AUTO_SERVER=TRUE"))
  } catch {
    case e: Exception => e.printStackTrace; Empty
  }
  
  def releaseConnection(conn: Connection) {
    conn.close
  }
}