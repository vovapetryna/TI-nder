import scalaz.Scalaz._

import scala.annotation.tailrec

package object core {
  case class AlgoStep(
      storeM: Map[String, models.Model],
      storeW: Map[String, models.Model],
      orderingM: Map[String, List[String]],
      orderingW: Map[String, List[String]],
      holdW: Map[String, Set[String]],
      result: Map[String, Set[String]] = Map()
  ) {
    private def proposals = orderingM.toList.map(r => r._2.headOption.getOrElse(r._1) -> r._1).groupBy(_._1).map {
      case (w, g) =>
        w -> g.map(_._2).toSet
    }
    private def updateHold(proposals: Map[String, Set[String]]) = holdW.map {
      case (k, h) =>
        k -> (h |+| proposals.getOrElse(k, Set()))
    }
    private def newHold(hold: Map[String, Set[String]]) = hold.map {
      case (w, h) =>
        w -> h.toList.map(m => m -> orderingW(w).indexOf(m)).sortBy(_._2).map(_._1).headOption.toSet
    }
    private def nextAlgoSte(rawHold: Map[String, Set[String]], newHold: Map[String, Set[String]], proposals: Map[String, Set[String]]) = {
      val diff = rawHold.map {
        case (w, h) =>
          w -> h.diff(newHold.getOrElse(w, Set()))
      }
      if (diff.forall(_._2.isEmpty)) this.copy(holdW = newHold, result = proposals)
      else {
        val rejectedMs = diff.toList.flatMap(_._2).toSet
        val newOrderingM = orderingM.map {
          case (m, o) =>
            if (rejectedMs.contains(m)) {
              m -> o.tailOption.getOrElse(List())
            } else m -> o
        }
        this.copy(orderingM = newOrderingM, holdW = newHold)
      }
    }
  }

  object AlgoStep {
    @tailrec
    def solve(step: AlgoStep): Map[String, Set[String]] = step match {
      case s if s.result.nonEmpty => s.result
      case s =>
        val proposals = s.proposals
        val rawHold   = s.updateHold(proposals)
        val newHold   = s.newHold(rawHold)
        solve(s.nextAlgoSte(rawHold, newHold, proposals))
    }
  }
}
