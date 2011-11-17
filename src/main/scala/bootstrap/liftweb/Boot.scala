package bootstrap.liftweb

import java.sql.{Connection, DriverManager}

import net.liftweb.common._
import net.liftweb.util.NamedPF

import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.mapper._
import net.liftweb.widgets.tablesorter.TableSorter

import tibi.quiver.model._
import tibi.quiver.view.ImageServer

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot extends Logger {
  def boot {
    
    // Defines the database to use.
    debug("this is a debug message")    
    DefaultConnectionIdentifier.jndiName = "jdbc/quiver"
    if (!DB.jndiJdbcConnAvailable_?) {
      warn("No JNDI, using h2")
      //DefaultConnectionIdentifier.jndiName = null
      //exit
      DB.defineConnectionManager(DefaultConnectionIdentifier, DbVendor)
    }
    
    // package to search for snippets, models and views
    LiftRules.addToPackages("tibi.quiver")
    
    // Tells Mapper to log all queries
    /* not working on stax
    DB.addLogFunc {
      case (query, time) => {
        Log.info("All queries took " + time + "ms: ")
        query.allEntries.foreach({ case DBLogEntry(stmt, duration) =>
          Log.info(stmt + " took " + duration + "ms")})
        Log.info("End queries")
      }
    }
    */
    
    // Model classes to map to the database
    Schemifier.schemify(true, Schemifier.infoF _, User, Image,
        Category, ProductType, Brand, Model, Size,
        Property, PropertyValue, ProductTypeProperty)

    // Adds some basic data to our database: product types and properties
    DbSetup.setup

    // Builds the menu (SiteMap)
    val entries = Menu(Loc("Home", List("index"), "home"))::
                  Menu(Loc("Search", List("search"), "search"))::
                  Menu(Loc("Brand", List("brand_models"), "Brand"), Brand.menus:_*)::
                  Menu(Loc("Edit Brand", List("brand_edit"), "edit brand"))::
                  Menu(Loc("Product Type", List("product_type"), "Product Type"))::
                  Property.menus:::
                  Menu(Loc("Model", List("model"), "Model"))::
                  Menu(Loc("Model edit", List("model_edit"), "Edit Model"))::
                  Menu(Loc("Size edit", List("size_edit"), "Edit Size"))::
                  User.sitemap:::
                  Nil
    LiftRules.setSiteMap(SiteMap(entries:_*))
    
    // Rewrite some URLs
    LiftRules.rewrite.prepend(NamedPF("category rewrite") {
      case RewriteRequest(ParsePath("cat" :: category :: Nil, _, _,_), _, _) => 
        RewriteResponse("index" :: Nil, Map("catName" -> category))
    })
    
    // Initializes the jquery lift widget
    TableSorter.init
    
    // Adds our image server to the request processing chain.
    LiftRules.dispatch.append(ImageServer.matcher)
    
    // Shows the spinny image when an Ajax call starts.
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Makes the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8")) 
    
    // We want all DB calls fired during an http request in a single transaction.
    S.addAround(DB.buildLoanWrapper)
  }

}

/** Used only when no JNDI connection is available. */
object DbVendor extends ConnectionManager {

  Class.forName("org.h2.Driver")
  //Class.forName("com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource")

  def newConnection(name: ConnectionIdentifier): Box[Connection] = try {
    Full(DriverManager.getConnection("jdbc:h2:db/quiver;AUTO_SERVER=TRUE"))
    //Full(DriverManager.getConnection("jdbc:mysql://localhost:3306/quiver", "quiver", "quiver"))
  } catch {
    case e: Exception => e.printStackTrace; Empty
  }
  
  def releaseConnection(conn: Connection) {
    conn.close
  }
}