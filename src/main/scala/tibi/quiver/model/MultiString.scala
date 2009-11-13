package tibi.quiver.model

import scala.collection.immutable.EmptyMap
import scala.xml._
import java.lang.reflect.Method
import java.util.Date
import net.liftweb._
import util._
import mapper._
import http.SHtml


object MultiString {

  /** The type used to specify the language. */
  type Lang = String
  val English: Lang = "en"
  val DefaultLang = English

  /**
   * A multilingual immutable string.
   * 
   * val mstr = MString("en" -> "in english")
   * mstr("en")  // returns "in english"
   * mstr + ("de" -> "auf deutsch")  // returns a new MString with both languages set
   */
  case class MString (map: Map[Lang, String]) {
    def apply(lang: Lang): String = if (map contains lang) map(lang) else ""
    def + (pair: Pair[Lang, String]) = MString(map + pair)
  }
  
  object MString {
    def apply(pairs: Pair[Lang, String]*) = new MString(Map(pairs: _*))
  }

  /**
   * A Lift mapper field holding a MString.
   * It can’t store the ¦ character because it is used as a delimiter.
   */
  class MappedMString[T <: Mapper[T]] (val fieldOwner: T, maxLen: Int)
  	  extends MappedField[MString, T] {

    def this(fieldOwner: T, value: MString, maxLen: Int) {
      this(fieldOwner, maxLen)
      setAll(value)
    }

    // "enIn English¦deauf Deutsch¦" <--> MString("en" -> "in English", "de" -> "auf Deutsch")
    def toStr = (for ((lang, str) <- is.map) yield
      if (str contains '¦') throw new IllegalArgumentException("Can’t save MString because it contains ¦ in language "
                                                               + lang + ": " + str)
      else lang.toString + str + "¦") reduceLeft (_+_)
    def fromStr(str: String): MString = {
      val seq = for (one <- str split '¦')
    	  yield (one take 2).toString -> (one drop 2).toString
      MString(seq :_*)
    }

    /**
     * A criterion to use in findAll to match a string in a given language.
     */  //def apply[O <: Mapper[O]](field: MappedField[String, O], value: String) =
    //def criterion(str: String, lang: Lang) =
  //Cmp[T, String](this, OprEnum.Like, Full("%"+lang.toString+str+"¦%"), Empty, Empty) // Like(this, "%"+lang.toString+str+"¦%")
    
    // The rest is mostly boilerplate copied from MappedString

    def defaultValue = new MString(new EmptyMap[Lang, String])
    def dbFieldClass = classOf[MString]
    
    private var data: MString = defaultValue
    private var origData = defaultValue
    
    def st(in: MString) {
      data = in
      origData = in
    }
    
    protected def i_is_! = data
    protected def i_was_! = origData
    override def doneWithSave { origData = data }
    override def readPermission_? = true
    override def writePermission_? = true
    protected def i_obscure_!(in: MString) = defaultValue
    
    protected def real_i_set_!(value: MString): MString = {
      if (value != data) {
        data = value
        dirty_?(true)
      }
      data
    }
    
    def setFromAny(in: Any) = in match {
      case mstr: MString => setAll(mstr)
      case n :: _ => setFromString(n.toString)
      case Some(n) => setFromString(n.toString)
      case Full(n) => setFromString(n.toString)
      case None | Empty | Failure(_, _, _) | null => setFromString("")
      case n => setFromString(n.toString)
    }
    
    def setFromString(in: String): MString = setAll(fromStr(in))

    //TODO what should we do here?
    def setAll(in: MString) = set(in)
    
    def targetSQLType = java.sql.Types.VARCHAR
    def jdbcFriendly(field: String) = toStr
    def real_convertToJDBCFriendly(value: MString): Object = value.toString
    
    def buildSetActualValue(accessor: Method, inst: AnyRef, columnName: String): (T, AnyRef) => Unit =
      (inst, v) => doField(inst, accessor, {
        case f: MappedMString[_] => f.st(fromStr(v.toString))
      })

    def buildSetLongValue(accessor: Method, columnName: String): (T, Long, Boolean) => Unit =
      (inst, v, isNull) => null

    def buildSetStringValue(accessor: Method, columnName: String): (T, String) => Unit =
      (inst, v) => doField(inst, accessor, {
        case f: MappedMString[_] => f.st(fromStr(v))
      })

    def buildSetDateValue(accessor: Method, columnName: String): (T, Date) => Unit =
      (inst, v) => null

    def buildSetBooleanValue(accessor: Method, columnName: String): (T, Boolean, Boolean) => Unit =
      (inst, v, isNull) => null
    
    def fieldCreatorString(dbType: DriverType, colName: String): String = colName+" VARCHAR("+maxLen*4+")"
     
    import http.js._
    def asJsExp: JsExp = JE.Str(is.toString) //TODO also convert to string?
      
    override def _toForm: Box[NodeSeq] = Full(SHtml.text(data(DefaultLang), this.setFromAny(_)))
//      S.fmapFunc({s: List[String] => this.setFromAny(s)}) {
//      funcName => Full(<input type='text' id={fieldId} name={funcName} value={}/>)
//    }
  }
}
