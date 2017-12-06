package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Solution, Theory}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SocketLibrarySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = {
    val p = new PrologEngine()
    p.loadLibrary("com.szadowsz.gospel.core.db.libs.ThreadLibrary")
    p
  }

  behavior of "Socket Library"

  it should "write to a server socket successfully" in {
    val theory = "server(Y):- thread_create(ID1, Y). \n" + "doServer(S) :- tcp_socket_server_open('127.0.0.1:4445', S, []), " + "tcp_socket_server_accept(S, '127.0.0.1:4445', ClientSock),  " + "write_to_socket(ClientSock, 'msg inviato dal server'), " + "thread_sleep(1), " + "mutex_lock('mutex'), " + "tcp_socket_server_close(S)," + "mutex_unlock('mutex').\n" + "client(X):- thread_create(ID2,X), " + "thread_read(ID2,X).\n" + "doClient(Sock, Msg) :- tcp_socket_client_open('127.0.0.1:4445',Sock), " + "mutex_lock('mutex'), " + "read_from_socket(Sock, Msg, []), " + "mutex_unlock('mutex')."
    prolog.setTheory(new Theory(theory))
    val result = prolog.solve("server(doServer(SS)), client(doClient(CS,Msg)).")
    result.isSuccess shouldBe true
    val msg = result.getTerm("Msg").asInstanceOf[Struct]
    msg shouldBe prolog.createTerm("'msg inviato dal server'")
  }

  it should "write to a client socket successfully" in {
    val theory = "server(ID1):- thread_create(ID1, doServer(SS, Msg)). \n" + "doServer(S, Msg) :- tcp_socket_server_open('127.0.0.1:4445', S, []), " + "tcp_socket_server_accept(S, '127.0.0.1:4445', ClientSock), " + "mutex_lock('mutex'), " + "read_from_socket(ClientSock, Msg, []), " + "mutex_unlock('mutex'), " + "tcp_socket_server_close(S).\n" + "client(X):- thread_create(ID2,X), " + "thread_read(ID2,X).\n" + "doClient(Sock) :- tcp_socket_client_open('127.0.0.1:4444',Sock),  " + "write_to_socket(Sock, 'msg inviato dal client'), " + "thread_sleep(1).\n" + "read(ID1,Y):- thread_read(ID1,Y)."
    prolog.setTheory(new Theory(theory))
    val result = prolog.solve("server(ID1), client(doClient(CS)), read(ID1,doServer(SS,Msg)).")
    result.isSuccess shouldBe true
    val msg = result.getTerm("Msg").asInstanceOf[Struct]
    msg shouldBe prolog.createTerm("'msg inviato dal client'")
  }

}
