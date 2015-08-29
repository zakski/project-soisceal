package com.szadowsz.gospel.core.data.socket

import java.net.{InetAddress, Socket}
import java.{util => ju}

import com.szadowsz.gospel.core.data.{Term, Var}

@SerialVersionUID(1L)
class Client_Socket(s: Socket) extends AbstractSocket {
  private val _socket: Socket = s

  override def isClientSocket: Boolean = true

  override def isServerSocket: Boolean = false

  override def getSocket: Socket = _socket

  override def unify(varsUnifiedArg1: ju.List[Var], varsUnifiedArg2: ju.List[Var], theTerm : Term): Boolean = {
    val t = theTerm.getTerm
    t match {
      case variable : Var => variable.unify(varsUnifiedArg1, varsUnifiedArg2, this)

      case socket: AbstractSocket =>
        val addr: InetAddress = socket.getAddress
        socket.isClientSocket &&  _socket.getInetAddress.toString == addr.toString
      case _ =>  false
    }
  }

  override def getAddress: InetAddress = if (_socket.isBound) _socket.getInetAddress else null

  override def isDatagramSocket: Boolean = false

  override def toString: String = _socket.toString
}