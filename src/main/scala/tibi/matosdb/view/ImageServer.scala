package tibi.matosdb.view

import net.liftweb._ 
import mapper._ 
import http._ 
import util._
import ControlHelpers.tryo

import model.Image

object ImageServer {
  
  object TestImage {
    def unapply(in: String): Option[Image] = try {
        Image.find(By(Image.id, in.toLong))
    } catch { case _:NumberFormatException => None }
  }

  def matcher: LiftRules.DispatchPF = {
    case r @ Req("imageSrv" :: TestImage(img) ::
                 Nil, _, GetRequest) => () => serveImage(img, r)
  }

  def serveImage(img: Image, r: Req): Box[LiftResponse] = {
  /*  if (r.testIfModifiedSince(img.saveTime))
    Full(InMemoryResponse(new Array[Byte](0),
                          List("Last-Modified" ->
                               toInternetDate(img.saveTime.is)), Nil, 304))
    else */ Full(InMemoryResponse(img.data.is,
                               List(/*"Last-Modified" ->
                                    toInternetDate(img.saveTime.is),*/
                                    "Content-Type" -> img.mimeType.is,
                                    "Content-Length" ->
                                    img.data.is.length.toString), Nil, 200))
  }
}
