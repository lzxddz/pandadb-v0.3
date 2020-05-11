package cn.pandadb.datanode

import cn.pandadb.configuration.Config
import cn.pandadb.leadernode.LeaderNodeRpcEndPoint
import cn.pandadb.server.modules.LifecycleServerModule
import cn.pandadb.cluster.{ClusterService, LeaderNodeChangedEvent, NodeRoleChangedEvent, NodeRoleChangedEventListener}
import net.neoremind.kraps.RpcConf
import net.neoremind.kraps.rpc._
import net.neoremind.kraps.rpc.netty.NettyRpcEnvFactory
import org.slf4j.Logger


class PandaRpcServer(config: Config, clusterService: ClusterService) extends LifecycleServerModule {
  val logger: Logger = config.getLogger(this.getClass)

  val rpcHost = config.getListenHost()
  val rpcPort = config.getRpcPort
  val rpcServerName = config.getRpcServerName()

  val rpcConfig = RpcEnvServerConfig(new RpcConf(), rpcServerName, rpcHost, rpcPort)
  val rpcEnv: RpcEnv = NettyRpcEnvFactory.create(rpcConfig)

  val dataNodeEndpointName = config.getDataNodeEndpointName()
  val dataNodeRpcEndpoint: RpcEndpoint = new DataNodeRpcEndpoint(rpcEnv, config)
  var dataNodeRpcEndpointRef: RpcEndpointRef = null

  val leaderNodeEndpointName = config.getLeaderNodeEndpointName()
  val leaderNodeRpcEndpoint: RpcEndpoint = new LeaderNodeRpcEndPoint(rpcEnv, config, clusterService)
  var leaderNodeRpcEndpointRef: RpcEndpointRef = null

  override def init(): Unit = {
    logger.info(this.getClass + ": init")
  }

  override def start(): Unit = {
    logger.info(this.getClass + ": start")
    dataNodeRpcEndpointRef = rpcEnv.setupEndpoint(dataNodeEndpointName, dataNodeRpcEndpoint)
//    clusterService.addNodeRoleChangedEventListener(new LeaderNodeChangeListener)
    addLeaderNodeRpcEndpoint()
    rpcEnv.awaitTermination()
  }

  override def stop(): Unit = {
    logger.info(this.getClass + ": stop")
    rpcEnv.stop(dataNodeRpcEndpointRef)
    this.removeLeaderNodeRpcEndpoint()
    rpcEnv.shutdown()
  }

  override def shutdown(): Unit = {
    logger.info(this.getClass + ": stop")
  }

  def addLeaderNodeRpcEndpoint(): Unit = {
      logger.info(this.getClass + ": addLeaderNodeRpcEndpoint")
      leaderNodeRpcEndpointRef = rpcEnv.setupEndpoint(leaderNodeEndpointName, leaderNodeRpcEndpoint)
  }

  def removeLeaderNodeRpcEndpoint(): Unit = {
    if (leaderNodeRpcEndpointRef != null) {
      logger.info(this.getClass + ": removeLeaderNodeRpcEndpoint")
      rpcEnv.stop(leaderNodeRpcEndpointRef)
    }
  }

  class LeaderNodeChangeListener extends NodeRoleChangedEventListener {
    override def notifyRoleChanged(event: NodeRoleChangedEvent): Unit = {
      logger.info(this.getClass + ": notifyRoleChanged")
      event match {
        case LeaderNodeChangedEvent(isLeader, leaderNode) =>
          if (isLeader) addLeaderNodeRpcEndpoint()
          else removeLeaderNodeRpcEndpoint()
      }
    }
  }
}