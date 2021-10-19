import zio.console.putStrLn
import zio.process.Command
import java.sql.Timestamp

object Main {

  case class data(event_type:String,data:String,timestamp:Timestamp)
  val myApp = for {
    _ <- Command("/home/eren/blackbox.amd64").linesStream
      .foreach(putStrLn(_))
  } yield ()


  def main(args: Array[String]): Unit = {

    val runtime = zio.Runtime.default
    runtime.unsafeRunSync(myApp)

  }

}
