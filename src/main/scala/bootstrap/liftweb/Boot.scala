package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, ConnectionIdentifier}
import _root_.java.sql.{Connection, DriverManager}
import _root_.tibi.matosdb.model._
import _root_.javax.servlet.http.{HttpServletRequest}

import tibi.matosdb.view.ImageServer

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    // where to search snippet
    LiftRules.addToPackages("tibi.matosdb")
    Schemifier.schemify(true, Log.infoF _, User, Image, Sport, ProductType, Brand, Model)

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home"))::
                  Menu(Loc("Brands", List("brands"), "Brands"))::
                  Menu(Loc("Edit Brand", List("brand_edit"), "edit brand"))::
                  Brand.menus:::
                  Menu(Loc("Product Types", List("product_types"), "Product Types"))::
                  Menu(Loc("Models", List("models"), "Models"))::
                  User.sitemap:::
                  Nil
    LiftRules.setSiteMap(SiteMap(entries:_*))
    
    LiftRules.dispatch.append(ImageServer.matcher)
    
    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HttpServletRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}

/**
* Database connection calculation
*/
object DBVendor extends ConnectionManager {

  Class.forName("org.h2.Driver")

  def newConnection(name: ConnectionIdentifier): Box[Connection] = try {
    Full(DriverManager.getConnection("jdbc:h2:db/matosdb;AUTO_SERVER=TRUE"))
  } catch {
    case e: Exception => e.printStackTrace; Empty
  }
  
  def releaseConnection(conn: Connection) {
    conn.close
  }
}