import argonaut._
import scalaz.Scalaz._
import scalaz._

package object models {
  abstract class PValue extends Order[PValue] {
    val value: Int
    val min: Int
    val max: Int
    def order(x: PValue, y: PValue): Ordering = implicitly[Order[Int]].order(x.value, y.value)
    def dist(another: PValue): Double         = (value - another.value).abs.toDouble / (max - min).toDouble
  }

  case class Place(override val value: Int) extends PValue {
    override val min: Int = 1
    override val max: Int = 5
  }
  object Place {
    def parse(value: String): Place = value.toLowerCase match {
      case "football"   => Place(1)
      case "bar"        => Place(2)
      case "restaurant" => Place(3)
      case "theatre"    => Place(4)
      case "park"       => Place(5)
    }
    def repr(value: Place): String = value.value match {
      case 1 => "football"
      case 2 => "bar"
      case 3 => "restaurant"
      case 4 => "theatre"
      case 5 => "park"
    }
    implicit val r: DecodeJson[Place] = implicitly[DecodeJson[String]].map(parse)
    implicit val w: EncodeJson[Place] = implicitly[EncodeJson[String]].contramap(repr)
  }

  case class Hobby(override val value: Int) extends PValue {
    override val min: Int = 1
    override val max: Int = 6
  }
  object Hobby {
    def parse(value: String): Hobby = value.toLowerCase match {
      case "extreme_sport"    => Hobby(1)
      case "sport"            => Hobby(2)
      case "leisure"          => Hobby(3)
      case "passive_rest"     => Hobby(4)
      case "self-development" => Hobby(5)
      case "meditations"      => Hobby(5)
    }
    def repr(value: Hobby): String = value.value match {
      case 1 => "extreme_sport"
      case 2 => "sport"
      case 3 => "leisure"
      case 4 => "passive_rest"
      case 5 => "self-development"
      case 6 => "meditations"
    }
    implicit val r: DecodeJson[Hobby] = implicitly[DecodeJson[String]].map(parse)
    implicit val w: EncodeJson[Hobby] = implicitly[EncodeJson[String]].contramap(repr)
  }

  case class Age(override val value: Int) extends PValue {
    override val min: Int = 18
    override val max: Int = 50
  }
  object Age {
    def parse(value: Int): Age      = Age(value)
    implicit val r: DecodeJson[Age] = implicitly[DecodeJson[Int]].map(parse)
    implicit val w: EncodeJson[Age] = EncodeJson.derive
  }

  case class Height(override val value: Int) extends PValue {
    override val min: Int = 140
    override val max: Int = 200
  }
  object Height {
    def parse(value: Int): Height      = Height(value)
    implicit val r: DecodeJson[Height] = implicitly[DecodeJson[Int]].map(parse)
    implicit val w: EncodeJson[Height] = EncodeJson.derive
  }

  case class Model(place: Place, hobby: Hobby, age: Age, height: Height) {
    def dist(another: Model): Double =
      place.dist(another.place) + hobby.dist(another.hobby) + age.dist(another.age) + height.dist(another.height)
  }
  object Model {
    implicit val r: DecodeJson[Model] = DecodeJson.derive
    implicit val w: EncodeJson[Model] = EncodeJson.derive
  }
}
