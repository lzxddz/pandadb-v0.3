
import java.io.File
import scala.collection.JavaConverters._

import cn.pandadb.server.PNodeServer
import org.junit.{After, Before, Test}
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.io.fs.FileUtils
import cn.pandadb.externalprops.{InMemoryPropertyNodeStore, InMemoryPropertyNodeStoreFactory}


class UpdatePropertyQueryAPITest extends UpdateQueryTestBase {
  val tmpns = InMemoryPropertyNodeStore

  @Test
  def test1(): Unit = {
    // update and add node properties using 'set n.prop1=value1,n.prop2=value2'

    // create node
    val tx = db.beginTx()
    val query = "create (n1:Person) return id(n1)"
    val rs = db.execute(query)
    var id1: Long = -1
    if(rs.hasNext) {
      val row = rs.next()
      id1 = row.get("id(n1)").toString.toLong
    }
    tx.success()
    tx.close()
    assert(id1 != -1 )
    assert(tmpns.nodes.size == 1)
    assert(tmpns.nodes.get(id1).get.props.size == 0)
    assert(tmpns.nodes.get(id1).get.labels.size == 1 && tmpns.nodes.get(id1).get.labels.toList(0) == "Person")

    // update and add properties
    val tx2 = db.beginTx()
    val query2 = s"match (n1:Person) where id(n1)=$id1 set n1.name='test01', n1.age=10 return n1.name,n1.age"
    db.execute(query2)
    tx2.success()
    tx2.close()
    assert(tmpns.nodes.size == 1)
    val fields = tmpns.nodes.get(id1).get.props
    assert(fields.size == 2 && fields("name").equals("test01") && fields("age").equals(10)  )
  }


  @Test
  def test2(): Unit = {
    // update node properties using 'set n={prop1:value1, prop2:value2}'

    // create node
    val tx = db.beginTx()
    val query = "create (n1:Person{name:'test01',age:10}) return id(n1)"
    val rs = db.execute(query)
    var id1: Long = -1
    if(rs.hasNext) {
      val row = rs.next()
      id1 = row.get("id(n1)").toString.toLong
    }
    tx.success()
    tx.close()
    assert(id1 != -1 )
    assert(tmpns.nodes.size == 1)
    val fields1 = tmpns.nodes.get(id1).get.props
    assert(fields1.size == 2 && fields1("name").equals("test01") && fields1("age").equals(10))

    // update property
    val tx2 = db.beginTx()
    val query2 = s"match (n1:Person) where id(n1)=$id1 set n1={name:'test02', sex:'male'} return n1"
    db.execute(query2)
    tx2.success()
    tx2.close()
    assert(tmpns.nodes.size == 1)
    val fields2 = tmpns.nodes.get(id1).get.props
    assert(fields2.size == 2 && fields2("name").equals("test02") && fields2("sex").equals("male"))
  }


  @Test
  def test3(): Unit = {
    // update or add node properties using 'set n +={prop1:value1, prop2:value2}'

    // create node
    val tx = db.beginTx()
    val query = "create (n1:Person{name:'test01',age:10}) return id(n1)"
    val rs = db.execute(query)
    var id1: Long = -1
    if(rs.hasNext) {
      val row = rs.next()
      id1 = row.get("id(n1)").toString.toLong
    }
    tx.success()
    tx.close()
    assert(id1 != -1 )
    assert(tmpns.nodes.size == 1)
    val fields1 = tmpns.nodes.get(id1).get.props
    assert(fields1.size == 2 && fields1("name").equals("test01") && fields1("age").equals(10))

    // update property
    val tx2 = db.beginTx()
    val query2 = s"match (n1:Person) where id(n1)=$id1 set n1 +={name:'test02',sex:'male', work:'dev'} return n1"
    db.execute(query2)
    tx2.success()
    tx2.close()
    assert(tmpns.nodes.size == 1)
    val fields2 = tmpns.nodes.get(id1).get.props
    assert(fields2.size == 4 && fields2("name").equals("test02") && fields2("age").equals(10) &&
            fields2("sex").equals("male") && fields2("work").equals("dev"))
  }

  @Test
  def test4(): Unit = {
    // remove node properties using 'remove n.prop1'

    // create node
    val tx = db.beginTx()
    val query = "create (n1:Person{name:'test01',age:10}) return id(n1)"
    val rs = db.execute(query)
    var id1: Long = -1
    if(rs.hasNext) {
      val row = rs.next()
      id1 = row.get("id(n1)").toString.toLong
    }
    tx.success()
    tx.close()
    assert(id1 != -1 )
    assert(tmpns.nodes.size == 1)
    val fields1 = tmpns.nodes.get(id1).get.props
    assert(fields1.size == 2 && fields1("name").equals("test01") && fields1("age").equals(10))

    // remove one property
    val tx2 = db.beginTx()
    val query2 = s"match (n1:Person) where id(n1)=$id1 remove n1.age"
    db.execute(query2)
    tx2.success()
    tx2.close()
    assert(tmpns.nodes.size == 1)
    val fields2 = tmpns.nodes.get(id1).get.props
    assert(fields2.size == 1 && fields2("name").equals("test01") )
  }

  @Test
  def test5(): Unit = {
    // remove node all properties using 'set n={}'

    // create node
    val tx = db.beginTx()
    val query = "create (n1:Person{name:'test01',age:10}) return id(n1)"
    val rs = db.execute(query)
    var id1: Long = -1
    if(rs.hasNext) {
      val row = rs.next()
      id1 = row.get("id(n1)").toString.toLong
    }
    tx.success()
    tx.close()
    assert(id1 != -1 )
    assert(tmpns.nodes.size == 1)
    val fields1 = tmpns.nodes.get(id1).get.props
    assert(fields1.size == 2 && fields1("name").equals("test01") && fields1("age").equals(10))

    // remove property
    val tx2 = db.beginTx()
    val query2 = s"match (n1:Person) where id(n1)=$id1 set n1={} return n1"
    db.execute(query2)
    tx2.success()
    tx2.close()
    assert(tmpns.nodes.size == 1)
    val fields2 = tmpns.nodes.get(id1).get.props
    assert(fields2.size == 0)
  }

}


class UpdateLabelQueryAPITest extends UpdateQueryTestBase {
  val tmpns = InMemoryPropertyNodeStore

}