package cn.pandadb.datanode

case class createNode(labels: Iterable[String] = null, properties: Map[String, Any] = null) {}

case class createNodeWithId(id: Long, labels: Iterable[String] = null, properties: Map[String, Any] = null) {}

case class getNode(id: Long) {}