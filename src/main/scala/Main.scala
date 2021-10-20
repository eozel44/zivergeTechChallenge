import io.circe.{Decoder, Encoder}
import zio._
import zio.console._
import zio.interop.catz._
import zio.interop.catz.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder


object Main extends App {
   private val dsl = Http4sDsl[Task]
  import dsl._

   private val dataService = HttpRoutes
    .of[Task] {
      case GET -> Root / "hello" => Ok("Hello, Joe")
    }
    .orNotFound

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    ZIO
      .runtime[ZEnv]
      .flatMap { implicit runtime =>
        BlazeServerBuilder[Task](runtime.platform.executor.asEC)
          .bindHttp(8080, "localhost")
          .withHttpApp(dataService)
          .resource
          .toManagedZIO
          .useForever
          .foldCauseM(
            err => ZIO.succeed(ExitCode.failure),
            _ => ZIO.succeed(ExitCode.success)
          )
      }

}