import zio.console.putStrLn
import zio.process.Command
import zio._
import zio.duration._

object DataService {



  trait ServiceError extends Exception with Product with Serializable
  case class ParsingError(m: String) extends ServiceError
  case class Data(event_type: String, data: String, timestamp: Long)
  def toData(line: String): Data = {
    import io.circe.Decoder
    import io.circe.generic.semiauto.deriveDecoder
    import io.circe.parser
    implicit val decodeData: Decoder[Data] = deriveDecoder[Data]
    //ZIO.fromEither(parser.decode[Data](line)).mapError(k=>ParsingError(k.getMessage)).
    parser.decode[Data](line).getOrElse(Data("0", "0", 0))
  }


  val myApp = for {
    _ <- Command("/home/eren/blackbox.amd64").linesStream
      //.tap(x => putStrLn(s"before mapping: $x"))
      .map(k => toData(k))
      //.tap(x => putStrLn(s"after mapping: $x"))
      .filterNot(_.timestamp == 0)
      .schedule(Schedule.spaced(5.second))
      .groupByKey(k => k.event_type) {
        case (word, stream) =>
          stream
            //.tap(x => putStrLn("aggStreamBefore " + x))
            .scan((1, word)) { case (acc, _) => (acc._1 + 1, word) }
        //.tap(x => putStrLn("aggStreamAfter " + x.toString))
      }

      .foreach(k => putStrLn(s"${k._2} --- ${k._1}"))
  } yield ()


  def main(args: Array[String]): Unit = {

    val runtime = zio.Runtime.default
    runtime.unsafeRunSync(myApp)

  }

}
