package ru.tolsi.matcher

class LoadExampleDataSpec extends UnitSpec with OrderParserSpec with ClientInfoParserSpec {
  val l = new Object with LoadExampleData
  describe("LoadExampleData") {
    describe("loadCreateOrdersRequests method") {
      it("should load OrderOperation.Create from file") {
        checkOrders(l.loadCreateOrdersRequests.toList)
      }
    }
    describe("loadClients method") {
      it("should load ClientInfo from file") {
        checkClients(l.loadClients.toList)
      }
    }
  }
}
