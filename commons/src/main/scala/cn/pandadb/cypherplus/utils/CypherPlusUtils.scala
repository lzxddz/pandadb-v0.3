package cn.pandadb.cypherplus.utils

/**
  * @Author: Airzihao
  * @Description:
  * @Date: Created at 14:50 2019/11/27
  * @Modified By:
  */
object CypherPlusUtils {

  def isWriteStatement(cypherStr: String): Boolean = {
    if (cypherStr.contains("create") || cypherStr.contains("merge") ||
      cypherStr.contains("set") || cypherStr.contains("delete")) {
      true
    } else {
      false
    }
  }
}