package ppd

import java.io.File

import cn.pandadb.externalprops.{CustomPropertyNodeStore, InMemoryPropertyNodeStore, InMemoryPropertyNodeStoreFactory}
import cn.pandadb.server.GlobalContext
import org.junit.{After, Before, Test}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.io.fs.FileUtils

trait QueryCase {

  var db: GraphDatabaseService = null

  def buildDB(store: CustomPropertyNodeStore): Unit = {
    val dbFile: File = new File("./output/testdb")
    FileUtils.deleteRecursively(dbFile);
    dbFile.mkdirs();
    db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbFile).newGraphDatabase()
    GlobalContext.put(classOf[CustomPropertyNodeStore].getName, store)

    // create one node
    val query = "CREATE (n:Person {age: 10, name: 'bob'})"
    db.execute(query)
  }

  @After
  def shutdownDB(): Unit = {
    db.shutdown()
  }

  def testQuery(query: String, resultKey: String): Unit = {
    val rs = db.execute(query)
    var resultValue: Long = -1
    if (rs.hasNext) {
      resultValue = rs.next().get(resultKey).toString.toLong
    }
    assert(resultValue != -1)
  }

  @Test
  def lessThan(): Unit = {
    testQuery("match (n) where 18>n.age return id(n)", "id(n)")
  }

  @Test
  def greaterThan(): Unit = {
    testQuery("match (n) where 9<n.age return id(n)", "id(n)")
  }

  @Test
  def numberEqual(): Unit = {
    testQuery("match (n) where n.age = 10 return id(n)", "id(n)")
  }

  @Test
  def stringEqual(): Unit = {
    testQuery("match (n) where n.name = 'bob' return id(n)", "id(n)")
  }

  @Test
  def stringEndsWith(): Unit = {
    testQuery("match (n) where n.name ENDS WITH 'b' return id(n)", "id(n)")
  }

  @Test
  def stringStartsWith(): Unit = {
    testQuery("match (n) where n.name STARTS WITH 'b' return id(n)", "id(n)")
  }

  @Test
  def label(): Unit = {
    testQuery("match (n:Person) return id(n)", "id(n)")
  }

  @Test
  def labelAndStringEndsWith(): Unit = {
    testQuery("match (n:Person) where n.name ENDS WITH 'b' return id(n)", "id(n)")
  }

  @Test
  def join(): Unit = {
    testQuery("Match p=()--() return count(p)", "count(p)")
  }

}