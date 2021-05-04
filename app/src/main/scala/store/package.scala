import com.typesafe.scalalogging.LazyLogging
import scalaz._

package object store extends LazyLogging {
  class RawData {
    var storeM = Map.empty[String, models.Model]
    var storeW = Map.empty[String, models.Model]
    def putM(key: String, value: models.Model): RawData = {
      storeM = storeM.+((key, value))
      logger.info(storeM.mkString("\n"))
      this
    }
    def putW(key: String, value: models.Model): RawData = {
      storeW = storeW.+((key, value))
      logger.info(storeM.mkString("\n"))
      this
    }
    def toDistMatrixM: DistMatrix =
      DistMatrix(storeM.map { case (k, v) => k -> storeW.map{case (k, v) => k -> v.dist(v)} }, storeM, storeW)
    def toDistMatrixW: DistMatrix =
      DistMatrix(storeW.map { case (k, v) => k -> storeM.map{ case (k, v) => k -> v.dist(v)} }, storeW, storeM)
  }

  class RawPreferences {
    private var preferencesStore = Map.empty[String, Ordering]
    def put(key: String, value: Ordering): RawPreferences = {
      preferencesStore = preferencesStore.+((key, value))
      logger.info(preferencesStore.mkString("\n"))
      this
    }
    def buildTotalOrdering(distMatrix: DistMatrix): Map[String, List[String]] =
      distMatrix.store.map {
        case (k, m) =>
          val ord  = preferencesStore.getOrElse(k, Ordering.GT)
          val data = distMatrix.matrix(k).toList.sortBy(_._2)
          val ordering = ord match {
            case Ordering.LT => data.map(_._1).reverse
            case _           => data.map(_._1)
          }
          k -> ordering
      }
  }

  case class DistMatrix(matrix: Map[String, Map[String, Double]], store: Map[String, models.Model], anotherStore: Map[String, models.Model]) {
    def genTestForUser(user: String): List[(String, models.Model)] =
      matrix
        .get(user)
        .toList
        .flatMap { dist =>
          val all  = dist.toList.sortBy(_._2)
          val allR = all.reverse
          anotherStore.get(all.head._1).toList.map(v => all.head._1     -> v) ++
            anotherStore.get(allR.head._1).toList.map(v => allR.head._1 -> v)
        }

    def genAllTests: List[List[String]] = store.keySet.toList.map(u => genTestForUser(u).map(_._1))
  }
}
