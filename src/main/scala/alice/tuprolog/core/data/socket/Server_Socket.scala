package alice.tuprolog.core.data.socket

import java.net.{InetAddress, ServerSocket}
import java.{util => ju}


import alice.tuprolog.core.data.{Term, Var}

@SerialVersionUID(1L)
class Server_Socket(s: ServerSocket) extends AbstractSocket {
  private val _socket: ServerSocket = s

  override  def getSocket: ServerSocket = _socket

  override def isClientSocket: Boolean = false

  override def isServerSocket: Boolean = true

  override def unify(varsUnifiedArg1: ju.List[Var], varsUnifiedArg2: ju.List[Var], theTerm : Term): Boolean = {
    val t = theTerm.getTerm
    t match {
      case variable : Var => variable.unify(varsUnifiedArg1, varsUnifiedArg2, this)

      case socket: AbstractSocket =>
        val addr: InetAddress = socket.getAddress
        socket.isServerSocket &&  _socket.getInetAddress.toString == addr.toString
      case _ =>  false
    }
  }

  override def getAddress: InetAddress = if (_socket.isBound) _socket.getInetAddress else null


  override def isDatagramSocket: Boolean = false


  override def toString: String = _socket.toString
}