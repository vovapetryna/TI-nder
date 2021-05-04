import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Coders
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import com.typesafe.scalalogging.LazyLogging
import core._
import models._
import store._
import utils.AkkaHelpers

import scala.concurrent.ExecutionContext
import scala.util.Try

object Main extends App with LazyLogging with AkkaHelpers {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "api")
  implicit val ec: ExecutionContext         = system.executionContext
  val rStore                                = new RawData
  def distanceM: DistMatrix                 = rStore.toDistMatrixM
  def distanceW: DistMatrix                 = rStore.toDistMatrixW
  val vStoreM                               = new RawPreferences
  val vStoreW                               = new RawPreferences

  //-- preset
  val M1 = Model(Place.parse("football"), Hobby.parse("extreme_sport"), Age(20), Height(180))
  val M2 = Model(Place.parse("restaurant"), Hobby.parse("self-development"), Age(22), Height(175))
  val W1 = Model(Place.parse("restaurant"), Hobby.parse("leisure"), Age(19), Height(169))
  val W2 = Model(Place.parse("park"), Hobby.parse("sport"), Age(21), Height(170))
  val W3 = Model(Place.parse("bar"), Hobby.parse("extreme_sport"), Age(18), Height(165))
  val W4 = Model(Place.parse("theatre"), Hobby.parse("meditations"), Age(22), Height(160))
  rStore.putM("Petro", M1)
  rStore.putM("Andrey", M2)
  rStore.putW("Polina", W1)
  rStore.putW("Veronika", W2)
  rStore.putW("Ania", W3)
  rStore.putW("Yana", W4)
  //--

  val rHandler = RejectionHandler
    .newBuilder()
    .handleNotFound {
      import akka.http.scaladsl.model.StatusCodes
      complete((StatusCodes.NotFound, "Something went wrong"))
    }
    .result()

  val route =
    DebuggingDirectives.logRequestResult("API", Logging.InfoLevel)(
      pathPrefix("file") {
        getFromResourceDirectory("src")
      } ~ handleRejections(rHandler) {
        post {
          path("register") {
            decodeRequestWith(Coders.DefaultCoders: _*) {
              entity(implicitly[FromRequestUnmarshaller[dto.RegisterEntry]]) { entry =>
                Try(if (entry.isMen) rStore.putM(entry.name, entry.model) else rStore.putW(entry.name, entry.model)).toOption.nonEmpty.toResp.send
              }
            }
          } ~ path("question") {
            decodeRequestWith(Coders.DefaultCoders: _*) {
              entity(implicitly[FromRequestUnmarshaller[dto.RegisterEntry]]) { entry =>
                Try(if (entry.isMen) distanceM.genTestForUser(entry.name) else distanceW.genTestForUser(entry.name)).toOption.toResp.send
              }
            }
          } ~ path("answer") {
            decodeRequestWith(Coders.DefaultCoders: _*) {
              entity(implicitly[FromRequestUnmarshaller[dto.AnswerEntry]]) { entry =>
                Try(if (entry.isMen) vStoreM.put(entry.name, entry.answer) else vStoreW.put(entry.name, entry.answer)).toOption.nonEmpty.toResp.send
              }
            }
          } ~ path("myResults") {
            decodeRequestWith(Coders.DefaultCoders: _*) {
              entity(implicitly[FromRequestUnmarshaller[dto.Entry]]) { entry =>
                Try {
                  val initialStep = AlgoStep(
                    rStore.storeM,
                    rStore.storeW,
                    vStoreM.buildTotalOrdering(distanceM),
                    vStoreW.buildTotalOrdering(distanceW),
                    rStore.storeW.map { case (k, _) => k -> Set.empty[String] }
                  )
                  logger.info(initialStep.toString)
                  val solution = AlgoStep.solve(initialStep)
                  logger.info(solution.mkString("\n"))
                  val solutionW = solution.map{case (k, v) => k -> v.headOption.getOrElse("")}
                  .map{case (k, m) => k -> (m, rStore.storeM.get(m))}
                  logger.info(solutionW.mkString("\n"))
                  val SolutionM = solutionW.map { case (w, m) => m._1 -> (w, rStore.storeW.get(w).orElse(rStore.storeM.get(w))) }
                  logger.info(SolutionM.mkString("\n"))
                  (if (entry.isMen) SolutionM.get(entry.name) else solutionW.get(entry.name)).get
                }.toOption.toResp.send
              }
            }
          }
        }
      }
    )

  val host    = "0.0.0.0"
  val port    = 9001
  val binding = Http().newServerAt(host, port).bind(route)

  binding.onComplete {
    case scala.util.Success(_) =>
      logger.info(s"=== Server is UP at http://$host:$port/ ===")
    case scala.util.Failure(ex) =>
      logger.info(s"Failed to bind to $host:$port!", ex)
      sys.exit(1)
  }

  sys.addShutdownHook {
    binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }

}
