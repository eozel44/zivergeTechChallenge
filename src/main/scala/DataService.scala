import zio.console.putStrLn
import zio.process.Command
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser
import java.time.Instant
import java.time.temporal.ChronoUnit

object DataService {

  case class Data(event_type: String, data: String, timestamp: Long)
  implicit val decodeData: Decoder[Data] = deriveDecoder[Data]

  case class WindowState(counts: Map[String, Long], currentWindow: Instant) {

    def addData(data: Data) = {
      val dataWindow = Instant.ofEpochSecond(data.timestamp).truncatedTo(ChronoUnit.MINUTES)
      if (dataWindow == currentWindow) {
        val currentCount = counts.get(data.event_type).getOrElse(0L)
        WindowState(counts + (data.event_type -> (currentCount + 1L)), dataWindow)
      } else {
        WindowState(Map(data.event_type -> 1L), dataWindow)
      }
    }
  }

  val myApp = for {
    _ <- Command("/home/eren/blackbox.amd64").linesStream
           //.tap(x => putStrLn(s"before mapping: $x"))
           .map(line => parser.decode[Data](line))
           .collect { case Right(value) => value }
           .scan(WindowState(Map.empty,Instant.now().truncatedTo(ChronoUnit.MINUTES))){
             case (window,data) => window.addData(data)
           }
           .foreach(k => putStrLn(k.toString))
  } yield ()

  def main(args: Array[String]): Unit = {

    val runtime = zio.Runtime.default
    runtime.unsafeRunSync(myApp)

  }

}
