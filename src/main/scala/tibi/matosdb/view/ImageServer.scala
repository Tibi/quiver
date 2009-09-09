package tibi.matosdb.view

import java.util.Date

import net.liftweb._ 
import mapper._ 
import http._ 
import util._
import ControlHelpers.tryo
import TimeHelpers.{toInternetDate, parseInternetDate, now}

import model.Image

// Taken from http://www.mail-archive.com/liftweb@googlegroups.com/msg08344.html
object ImageServer {
  
  object cache extends KeyedCache[Long, Image](100, Full(0.75f), 
                        (id:Long) => Image.find(By(Image.id, id)))
  
  object TestImage {
    def unapply(in: String): Option[Image] = try {
        cache(in.toLong)
    } catch { case _:NumberFormatException => None }
  }

  def matcher: LiftRules.DispatchPF = {
    case r @ Req("imageSrv" :: TestImage(img) ::
                 Nil, _, GetRequest) => () => serveImage(img, r)
  }

  def isModifiedSince(req: Req, since: Long): Boolean = {
    // TODO test, I’ve just copied it from the internet
    req.request.header("if-modified-since") match {
      case Full(mod) => ((since / 1000L) * 1000L) <= parseInternetDate(mod).getTime 
      case _ => false
    }
    // if (mod != null && ((since / 1000L) * 1000L) <= parseInternetDate(mod).getTime) InMemoryResponse(new Array[Byte](0), Nil, Nil, 304) 
  }
  
  def serveImage(img: Image, r: Req): Box[LiftResponse] = {
    if (! isModifiedSince(r, img.saveTime.getDate)) {
      println(304)
      Full(InMemoryResponse(new Array[Byte](0),
                          List("Last-Modified" ->
                               toInternetDate(img.saveTime.is)), Nil, 304))
    }
    else Full(InMemoryResponse(img.data.is,
                               List("Last-Modified" ->
                               			toInternetDate(img.saveTime.is),
                                    "Content-Type" -> img.mimeType.is,
                                    "Content-Length" ->
                                    	img.data.is.length.toString), Nil, 200))
  }
  
  def save(image: Image) {
    image.saveTime(now).save
    cache.update(image.id, image)
  }
}
