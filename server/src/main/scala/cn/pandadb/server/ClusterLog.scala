package cn.pandadb.server

import java.io.{File, FileReader, FileWriter}

import com.google.gson.Gson

import scala.collection.mutable.ArrayBuffer

/**
  * @Author: Airzihao
  * @Description:
  * @Date: Created at 13:10 2019/11/30
  * @Modified By:
  */

class DataLogDetail(versionNum: Int, command: String) {
  private val version: Int = versionNum
  private val cypher: String = command

  def getVersion(): Int = {
    version
  }
  def getCypher: String = {
    cypher
  }
}

trait DataLogWriter {
  def write(row: DataLogDetail): Unit;
}

trait DataLogReader {
  def consume[T](consumer: (DataLogDetail) => T, sinceVersion: Int = -1): Iterable[T];
}

class DataLog(arrayBuffer: ArrayBuffer[DataLogDetail]) {
  val dataLog: Array[DataLogDetail] = arrayBuffer.toArray
}

class JsonDataLog(logFile: File) extends DataLogWriter {
  val gson = new Gson()

  var dataLog: ArrayBuffer[DataLogDetail] = {
    if (logFile.length() == 0) {
      new ArrayBuffer[DataLogDetail]()
    } else {
      val array = gson.fromJson(new FileReader(logFile), new DataLog(new ArrayBuffer[DataLogDetail]()).getClass)
      new ArrayBuffer[DataLogDetail]() ++= array.dataLog
    }
  }

  override def write(row: DataLogDetail): Unit = {
    dataLog.append(row)
    val fileWriter = new FileWriter(logFile)
    val logStr = gson.toJson(new DataLog(dataLog))

    fileWriter.write(logStr)
    fileWriter.flush();
    fileWriter.close();
  }

}