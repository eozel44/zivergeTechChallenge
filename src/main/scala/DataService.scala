import zio.console.putStrLn
import zio.process.Command
import io.circe.{ parser, Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import zio._

import java.time.Instant
import java.time.temporal.ChronoUnit
import zhttp.http._
import io.circe.syntax._
import zhttp.service.Server

object DataService extends App {

  case class Data(event_type: String, data: String, timestamp: Long)
  implicit val decodeData: Decoder[Data]          = deriveDecoder[Data]
  implicit val encodeWindow: Encoder[WindowState] = deriveEncoder[WindowState]

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

  def streamCalculator(ref: Ref[WindowState]) =
    for {
      initialState <- ref.get
      _            <- Command("/home/eren/blackbox.amd64").linesStream
                        //.tap(x => putStrLn(s"before mapping: $x"))
                        .map(line => parser.decode[Data](line))
                        .collect { case Right(value) => value }
                        .scan(initialState) { case (window, data) => window.addData(data) }
                        .foreach(k => ref.set(k))
    } yield ()

  def routes(ref: Ref[WindowState]) =
    Http.collectM[Request] { case Method.GET -> Root / "windows" =>
      ref.get.map(w => Response.jsonString(w.asJson.spaces2))
    }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    for {
      ref      <- Ref.make(WindowState(Map.empty, Instant.now().truncatedTo(ChronoUnit.MINUTES)))
      _        <- streamCalculator(ref).fork
      exitCode <- Server.start(9000, routes(ref)).exitCode
    } yield exitCode
}
