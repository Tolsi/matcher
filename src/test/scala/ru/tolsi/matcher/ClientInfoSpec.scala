package ru.tolsi.matcher

import java.io.File
import scala.util._
import ru.tolsi.matcher.util.EitherUtils._

trait ClientInfoParserSpec { self: UnitSpec =>
  def checkClientsErrors(errors: Seq[String]): Unit = {
    assert(errors.size == 2)
    errors should contain inOrderOnly(
      "Can't parse client info from line #2: 123",
      "Can't parse client info from line #5: omg!11"
    )
  }
  def checkClients(clients: Seq[ClientInfo]): Unit = {
    assert(clients.size == 3)
    clients should contain inOrderOnly(
      ClientInfo("C1", 1000, 1, 2, 3, 4),
      ClientInfo("C2", 100, 5, 6, 7, 8),
      ClientInfo("C3", 200, 9, 0, 1, 1)
    )
  }
}

class ClientInfoSpec extends UnitSpec with LoadExampleData {
  describe("ClientInfo") {
    describe("parseFromLine method") {
      it("should load ClientInfo from correct line") {
        ClientInfo.parseFromLine("A\t1\t2\t3\t4\t5") should be(Success(ClientInfo("A", 1, 2, 3, 4, 5)))
      }
      it("should not load ClientInfo from line with empty id") {
        ClientInfo.parseFromLine("\t1\t2\t3\t4\t5") should be('failure)
      }
      it("should not load ClientInfo from incorrect line") {
        ClientInfo.parseFromLine("hahayoureadthis") should be('failure)
      }
    }
    describe("loadFromFile method") {
      it("should load ClientInfo from file") {
        val (errors, clients) = splitEitherIterator(ClientInfo.loadFromFile(
          new File(getClass.getResource("/clients.txt").toURI)))

        val errorsSeq = errors.toList
        val clientsSeq = clients.toList

        assert(errorsSeq.size == 2)
        errorsSeq should contain inOrderOnly(
          "Can't parse client info from line #2: 123",
          "Can't parse client info from line #5: omg!11"
        )

        assert(clientsSeq.size == 3)
        clientsSeq should contain inOrderOnly(
          ClientInfo("C1", 1000, 1, 2, 3, 4),
          ClientInfo("C2", 100, 5, 6, 7, 8),
          ClientInfo("C3", 200, 9, 0, 1, 1)
        )
      }
    }
    describe("on create") {
      it("should fail with empty id") {
        an[IllegalArgumentException] should be thrownBy ClientInfo("", 1, 2, 3, 4, 5)
      }
    }
  }
}
