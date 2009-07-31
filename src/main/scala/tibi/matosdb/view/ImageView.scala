package tibi.matosdb.view

class ImageView extends LiftView {
  override def dispatch = {
    case "image" => getImage _
  }
case "enumerate" => doEnumerate _
}
def doEnumerate () : NodeSeq = {