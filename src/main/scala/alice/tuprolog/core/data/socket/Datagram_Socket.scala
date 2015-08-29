package alice.tuprolog.core.data.socket

import java.net.{DatagramSocket, InetAddress}
import java.{util => ju}

import alice.tuprolog.core.data.{Term, Var}

@SerialVersionUID(1L)
class Datagram_Socket(theSocket: DatagramSocket) extends AbstractSocket {
  private val _socket: DatagramSocket = theSocket

  override def isClientSocket: Boolean = false

  override def isServerSocket: Boolean = false

  override def isDatagramSocket: Boolean = true

  override def getSocket: DatagramSocket = _socket

  override def getAddress: InetAddress = if (_socket.isBound) _socket.getInetAddress else null

  override def unify(varsUnifiedArg1: ju.List[Var], varsUnifiedArg2: ju.List[Var], theTerm: Term): Boolean = {
    val t = theTerm.getTerm
    t match {
      case variable : Var => variable.unify(varsUnifiedArg1, varsUnifiedArg2, this)

      case socket: AbstractSocket =>
        val addr: InetAddress = socket.getAddress
        socket.isDatagramSocket &&  _socket.getInetAddress.toString == addr.toString
      case _ =>  false
    }

  }

  override def toString: String = _socket.toString
}