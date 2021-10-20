import zio.console.putStrLn
import zio.process.Command
import zio.stream.ZTransducer

import java.sql.Timestamp
import zio._

object Main {

  case class Data(event_type:String,data:String,timestamp:Long)
  trait ServiceError extends Exception with Product with Serializable
  case class ParsingError(m: String) extends ServiceError

  def toData(line:String):Data={
    import io.circe.Decoder
    import io.circe.generic.semiauto.deriveDecoder
    import io.circe.parser
    implicit val decodeData: Decoder[Data] = deriveDecoder[Data]
    //ZIO.fromEither(parser.decode[Data](line)).mapError(k=>ParsingError(k.getMessage)).
    parser.decode[Data](line).getOrElse(Data("0","0",0))
  }


  val myApp = for {
    _ <- Command("/home/eren/blackbox.amd64").linesStream
      //.tap(x => putStrLn(s"before mapping: $x"))
      .map(k=>toData(k))
      //.tap(x => putStrLn(s"after mapping: $x"))
      .chunkN(10)
      .filterNot(_.timestamp==0)
      .groupByKey(k=>k.event_type) {
        case (word, stream) =>
          stream
            //.tap(x => putStrLn("aggStreamBefore " + x))
            .scan((1, word)){ case (acc, _) => (acc._1 + 1, word)}
            //.tap(x => putStrLn("aggStreamAfter " + x.toString))
      }

      .foreach(k=>putStrLn(s"${k._2} --- ${k._1}"))
  } yield ()


  def main(args: Array[String]): Unit = {

    val runtime = zio.Runtime.default
    runtime.unsafeRunSync(myApp)

  }

}
