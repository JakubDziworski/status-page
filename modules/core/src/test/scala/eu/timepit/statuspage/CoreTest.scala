package eu.timepit.statuspage

import eu.timepit.statuspage.core.Item.{Entry, Group}
import eu.timepit.statuspage.core.Result.{Error, Info, Ok}
import eu.timepit.statuspage.core._
import org.scalatest.{FunSuite, Matchers}

class CoreTest extends FunSuite with Matchers {
  test("asPlainText 1") {
    rootAsPlainText(Root(Nil, Overall(Ok))) shouldBe "status: OK"
  }

  test("asPlainText 2") {
    rootAsPlainText(Root(Nil, Overall(Error(None)))) shouldBe "status: ERROR"
  }

  test("asPlainText 3") {
    val msg = "Database is not accessible"
    rootAsPlainText(Root(Nil, Overall(Error(Some(msg))))) shouldBe s"status: ERROR $msg"
  }

  test("asPlainText 4") {
    val msg = "Database is not accessible"
    rootAsPlainText(Root(List(Entry("database_status", Ok)), Overall(Ok))) shouldBe
      s"""|status: OK
          |database_status: OK
       """.stripMargin.trim
  }

  test("asPlainText 5") {
    rootAsPlainText(Root(
      List(Entry("database_status", Ok), Entry("elastic_search_status", Ok)),
      Overall(Ok))) shouldBe
      s"""|status: OK
          |database_status: OK
          |elastic_search_status: OK
       """.stripMargin.trim
  }

  test("asPlainText 6") {
    rootAsPlainText(
      Root(
        List(
          Group(
            "database",
            List(Entry("customers", Info("378")), Entry("items", Info("8934748"))),
            Overall(Ok))),
        Overall(Ok))) shouldBe
      s"""|status: OK
          |database_status: OK
          |database_customers: 378
          |database_items: 8934748
       """.stripMargin.trim
  }

  test("asPlainText 7") {
    rootAsPlainText(
      Root(
        List(
          Group(
            "database",
            List(Group("node1", Nil, Overall(Ok)), Group("node2", Nil, Overall(Ok))),
            Overall(Ok))),
        Overall(Ok))) shouldBe
      s"""|status: OK
          |database_status: OK
          |database_node1_status: OK
          |database_node2_status: OK
       """.stripMargin.trim
  }
}