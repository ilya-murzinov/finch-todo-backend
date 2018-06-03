package todobackend

import org.scalacheck._

object gens {
  val genTodoItem: Gen[TodoItem] = for {
    id <- Gen.uuid
    name <- Gen.alphaStr
    completed <- Gen.posNum[Int].map(_ % 2 == 0)
    order <- Gen.posNum[Int]
  } yield TodoItem(id, name, completed, order)

  implicit val arbTodoItem: Arbitrary[TodoItem] = Arbitrary(genTodoItem)
}