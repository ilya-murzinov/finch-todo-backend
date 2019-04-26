package todobackend

import org.scalacheck._

object gens {
  val genCreateTodoItemRequest: Gen[CreateTodoItemRequest] = for {
    title <- Gen.alphaStr
    completed <- Gen.option(Gen.posNum[Int].map(_ % 2 == 0))
    order <- Gen.option(Gen.posNum[Int])
  } yield CreateTodoItemRequest(title, completed, order)

  val genTodoItem: Gen[TodoItem] = for {
    id <- Gen.uuid
    title <- Gen.alphaStr
    completed <- Gen.posNum[Int].map(_ % 2 == 0)
    order <- Gen.posNum[Int]
  } yield TodoItem(id, title, completed, order)

  implicit val arbCreateTodoItemRequest: Arbitrary[CreateTodoItemRequest] = Arbitrary(genCreateTodoItemRequest)
  implicit val arbTodoItem: Arbitrary[TodoItem] = Arbitrary(genTodoItem)
}
