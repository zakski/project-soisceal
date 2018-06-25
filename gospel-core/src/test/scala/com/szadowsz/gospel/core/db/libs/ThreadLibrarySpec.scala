package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.data.{Int, Struct}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThreadLibrarySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = {
    val p = new PrologEngine()
    p.loadLibrary("com.szadowsz.gospel.core.db.libs.ThreadLibrary")
    p
  }

  behavior of "Thread Library"

  it should "unify the ID of the current thread to the ID (Root)" in {
    val sinfo = prolog.solve("thread_id(ID).")
    sinfo.isSuccess shouldBe true
    val id = sinfo.getVar("ID")
    id shouldBe new Int(0)
  }

  it should "create a new thread successfully" in {
    val theory = "genitore(bob,a).\ngenitore(bob,b).\ngenitore(bob,c).\ngenitore(bob,d)."
    prolog.setTheory(new Theory(theory))
    var sinfo = prolog.solve("thread_create(ID, genitore(bob,X)).")
    sinfo.isSuccess shouldBe true
    sinfo = prolog.solve("thread_create(ID, genitore(bob,X)), thread_create(ID2, genitore(b,Y)).")
    sinfo.isSuccess shouldBe true
  }

  it should "get the thread's next solution successfully" in {
    val theory =
      """
        |start(X) :- thread_create(ID, genitore(bob,X)),  loop(1,5,1,ID),  thread_read(ID, X).
        |loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.
        |loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.
        |loop(I, To, Inc, ThreadId) :- thread_read(ThreadId,A), thread_has_next(ThreadId), !, thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId).
        |loop(I, To, Inc, ThreadId).
        |genitore(b,b).
        |genitore(bob,c).
        |genitore(b,d).
        |genitore(bob,gdh).
        |genitore(b,e).
        |genitore(b,f).
        |""".stripMargin
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true
    val x = sinfo.getVar("X")
    x shouldBe prolog.createTerm("genitore(bob,gdh)")
  }

  it should "join threads successfully" in {
    val theory = "genitore(bob,a).\ngenitore(b,b)."
    prolog.setTheory(new Theory(theory))
    var sinfo = prolog.solve("thread_create(ID, genitore(bob,X)), thread_create(ID2, genitore(b,Y)), thread_join(ID2,Y), thread_join(ID,X).")
    sinfo.isSuccess shouldBe true
    val x = sinfo.getVar("X")
    x shouldBe prolog.createTerm("genitore(bob,a)")
    val y = sinfo.getVar("Y")
    y shouldBe prolog.createTerm("genitore(b,b)")
    sinfo = prolog.solve("thread_create(ID, genitore(bob,X)), thread_join(ID,X), thread_next_sol(ID).") //il thread stato rimosso
    sinfo.isSuccess shouldBe false
  }

  it should "read solutions from threads successfully" in {
    val theory =
      """
        |genitore(bob,a).
        |genitore(b,b).
        |genitore(bob,f).
        |loop(I, To, Inc, Action) :- Inc >= 0, I > To, !.
        |loop(I, To, Inc, Action) :- Inc < 0,  I < To, !.
        |loop(I, To, Inc, Action) :- Action, Next is I+Inc, loop(Next, To, Inc, Action).
        |""".stripMargin
    prolog.setTheory(new Theory(theory))
    var sinfo = prolog.solve("thread_create(ID, genitore(bob,X)), thread_read(ID,X1), thread_create(ID2, loop(1,10,1, thread_read(ID,X2))),  thread_create(ID3, loop(1,2,1, thread_read(ID,X2))), thread_next_sol(ID), thread_read(ID,X).")
    sinfo.isSuccess shouldBe true
    val x = sinfo.getVar("X")
    x shouldBe prolog.createTerm("genitore(bob,f)")
    val x1 = sinfo.getVar("X1")
    x1 shouldBe prolog.createTerm("genitore(bob,a)")
    sinfo = prolog.solve("thread_create(ID, genitore(bob,X)), thread_read(ID,X), thread_next_sol(ID).")
    sinfo.isSuccess shouldBe true
  }

  it should "check for open solutions in threads successfully" in {
    val theory =
      """
        |start(X) :- thread_create(ID, X), thread_execute(ID), lettura(ID,X).
        |lettura(ID, X):- thread_join(ID, X).
        |thread_execute(ID) :- thread_read(ID,A), thread_has_next(ID), !, thread_next_sol(ID).
        |thread_execute(ID).
        |genitore(bob,a).
        |genitore(bob,b).
        |genitore(bob,d).
        |""".stripMargin
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(genitore(bob,X)).")
    sinfo.isSuccess shouldBe true
    val x = sinfo.getVar("X")
    x shouldBe prolog.createTerm("b")
  }

  it should "not be able to get more solutions from a detached thread" in {
    val theory = "genitore(bob,a).\n" + "genitore(bob,b)."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("thread_create(ID, genitore(bob,X)), thread_detach(ID), thread_next_sol(ID).")
    sinfo.isSuccess shouldBe false
  }

  it should "successfully sleep a thread" in {
    val theory = "genitore(bob,a).\n" + "genitore(bob,b)."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("thread_create(ID, genitore(bob,X)), thread_sleep(500).")
    sinfo.isSuccess shouldBe true
  }

  it should "successfully send messages between threads" in {
    var theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" + "thread1(X) :- thread_wait_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + //Versione con 'CODA'
      "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    var sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true

    val x = sinfo.getVar("X")
    x shouldBe new Struct("messaggio molto importante")

    theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio(ID, 'messaggio molto importante'), lettura(ID,X), thread_get_msg('CODA', a(X)).\n" + //Posso nuovamente prelevare, in quanto il msg non stato eliminato
      "thread1(X) :- thread_wait_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + //Versione con ID
      "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true

    val x1 = sinfo.getVar("X")
    x1 shouldBe new Struct("messaggio molto importante")
  }

  it should "successfully get messages from threads" in {
    val theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), thread_sleep(500), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" + "thread1(X) :- thread_get_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true

    val x = sinfo.getVar("X")
    x shouldBe new Struct("messaggio molto importante")
  }

  it should "successfully peak at messages from threads" in {
    var theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), thread_sleep(500), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" + "thread1(X) :- thread_peek_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    var sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe false
    theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio(ID, 'messaggio molto importante'), lettura(ID,X), thread_get_msg('CODA', a(X)).\n" + //Posso nuovamente prelevare, in quanto il msg non stato rimosso
      "thread1(X) :- thread_peek_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true

    val x = sinfo.getVar("X")
    x shouldBe new Struct("messaggio molto importante")

    theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), lettura(ID,X).\n" + "thread1(X) :- thread_peek_msg('CODA', a(X)). \n " + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe false
  }

  it should "successfully wait for a message from another thread" in {
    var theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), thread_sleep(500), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" + "thread1(X) :- thread_wait_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    var sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true

    theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio(ID, 'messaggio molto importante'), lettura(ID,X), thread_get_msg('CODA', a(X)).\n" + "thread1(X) :- thread_wait_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true

    val x = sinfo.getVar("X")
    x shouldBe new Struct("messaggio molto importante")
  }

  it should "remove messages from the queue" in {
    val theory = "start(X) :- msg_queue_create('CODA'),  invio('CODA', 'messaggio molto importante'), thread_create(ID, thread1(X)), thread_sleep(50), thread_peek_message(ID, X).\n" + "thread1(X) :- thread_remove_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe false
  }

  it should "be able to destroy a message queue" in {
    val theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio('CODA', 'messaggio molto importante'), lettura(ID,X), msg_queue_destroy('CODA').\n" + "start2(X) :- msg_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), msg_queue_destroy('CODA'), thread_create(ID, thread1(X)),  lettura(ID,X).\n" + "thread1(X) :- thread_wait_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    var sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true

    sinfo = prolog.solve("start2(X).")
    sinfo.isSuccess shouldBe false
  }

  it should "be able to check the size of a message queue" in {
    val theory = "start(X, S) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), loop(1,5,1,invio('CODA', 'messaggio molto importante')), lettura(ID,X), msg_queue_size('CODA', S).\n" + "loop(I, To, Inc, Action) :- Inc >= 0, I > To, !.\n" + "loop(I, To, Inc, Action) :- Inc < 0,  I < To, !.\n" + "loop(I, To, Inc, Action) :- Action, Next is I+Inc, loop(Next, To, Inc, Action).\n" + "thread1(X) :- thread_wait_msg('CODA', a(X)). \n " + "invio(ID, M):- thread_send_msg(ID, a(M)). \n" + "lettura(ID, X):- thread_join(ID, thread1(X)). "
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X, S).")
    sinfo.isSuccess shouldBe true
    val x = sinfo.getVar("S")
    x shouldBe new Int(5)
  }

  it should "be able to destroy mutex locks" in {
    val theory = "start(M, X) :- mutex_create(M), mutex_lock(M), thread_create(ID, thread1(M)), thread_sleep(500), message_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), lettura(ID, X). \n" + "thread1(M) :- mutex_destroy(M). \n" + "invio(Q, M):- thread_send_msg(Q, a(M)), mutex_unlock('mutex'). \n" + "lettura(ID, X):- thread_read(ID, thread1(X))."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start('mutex', X).")
    sinfo.isSuccess shouldBe false
  }

  it should "lock and unlock mutex locks successfully" in {
    val theory = "start(X) :- mutex_lock('mutex'), thread_create(ID, thread1(X)), msg_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), lettura(ID,X). \n" + "thread1(X) :- mutex_lock('mutex'), thread_peek_msg('CODA', a(X)), mutex_unlock('mutex'). \n" + "invio(Q, M):- thread_send_msg(Q, a(M)), mutex_unlock('mutex'). \n" + "lettura(ID, X):- thread_read(ID, thread1(X))."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true
  }

  it should "fail to lock already locked mutex locks" in {
    val theory = "start(X) :- mutex_lock('mutex'), thread_create(ID, thread1(X)), message_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), lettura(ID,X). \n" + "thread1(X) :- mutex_trylock('mutex'), thread_peek_msg('CODA', a(X)), mutex_unlock('mutex'). \n" + "invio(Q, M):- thread_send_msg(Q, a(M)). \n" + "lettura(ID, X):- thread_read(ID, thread1(X))."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe false
  }

  it should "unlock all mutex locks successfully" in {
    val theory = "start(X) :- thread_create(ID, thread1(X)), mutex_lock('mutex1'). \n" + "thread1(X, M1, M2) :- mutex_lock('mutex1'), mutex_lock('mutex2'), mutex_unlock_all."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true
  }

  it should "pass factorial test" in {
    val theory = "start(N,X,M,Y):- thread_create(ID, fact1(N,X)), thread_join(ID, fact1(N,X)),thread_create(ID2, fact1(M,Y)), thread_join(ID2, fact1(M,Y)).\n" + "fact1(0,1):-!.\n" + "fact1(N,X):-M is N-1,fact1(M,Y),X is Y*N."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(7,X,8,Y).")
    sinfo.isSuccess shouldBe true
    val X = sinfo.getVar("X")
    assertEquals(new Int(5040), X)
    val Y = sinfo.getVar("Y")
    assertEquals(new Int(40320), Y)
  }

  it should "pass mutex test #1" in {
    val theory = "start(X) :- thread_create(ID, genitore(bob,X)), mutex_lock('mutex'), thread_create(ID2, lettura(ID,X)), loop(1,3,1,ID),  mutex_unlock('mutex').\n" + "genitore(bob,c).\n" + "genitore(bob,gdh).\n" + "loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.\n" + "loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.\n" + "loop(I, To, Inc, ThreadId) :- (thread_has_next(ThreadId) -> thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId); !).\n" + "lettura(ID, X):- mutex_lock('mutex'), thread_read(ID,X), mutex_unlock('mutex')."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start(X).")
    sinfo.isSuccess shouldBe true
  }

  it should "pass mutex test #2" in {
    val theory = "start :- thread_create(ID1, figlio(bob,X)), mutex_lock('mutex')," + "thread_create(ID2, lettura(ID1,X)), loop(1,5,1,ID1),  mutex_unlock('mutex').\n" + "loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.\n" + "loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.\n" + "loop(I, To, Inc, ThreadId) :- (thread_has_next(ThreadId) ->" + "thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId); !).\n" + "lettura(ID, X) :- mutex_lock('mutex'), thread_read(ID,X)," + "mutex_unlock('mutex').\n" + "figlio(bob,alex).\n" + "figlio(bob,anna).\n" + "figlio(bob,maria)."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start.")
    sinfo.isSuccess shouldBe true
  }

  it should "pass mutex test #3" in {
    val theory = "start :- thread_create(ID1, figlio(bob,X))," + "loop(1,5,1,ID1), thread_create(ID2, lettura(ID1,X)).\n" + "loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.\n" + "loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.\n" + "loop(I, To, Inc, ThreadId) :- (thread_has_next(ThreadId) ->" + "thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId); !).\n" + "lettura(ID, X) :- thread_read(ID,X).\n" + "figlio(bob,alex).\n" + "figlio(bob,anna).\n" + "figlio(bob,maria)."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("start.")
    sinfo.isSuccess shouldBe true
  }

  it should "pass concurrency test #1" in {
    val theory = "bubble(L1,L2) :- bubble(L1,0,L2).\n" + "bubble(L1,0,L2) :- sweep(L1,0,L2).\n" + "bubble(L1,0,L2) :- sweep(L1,1,LTMP),bubble(LTMP,0,L2).\n" + "sweep([X|[]],0,[X|[]]).\n" + "sweep([X,Y|REST1],CHANGED,[X|REST2]) :- X =< Y,sweep([Y|REST1],CHANGED,REST2).\n" + "sweep([X,Y|REST1],1,[Y|REST2]) :- X > Y,sweep([X|REST1],_,REST2).\n" + "plain(L1,L2) :- plain(L1,[],L2).\n" + "plain([],ACC,ACC).\n" + "plain([H|REST],ACC,L2) :- H = [_|_],plain(H,ACC,ACC1),plain(REST,ACC1,L2).\n" + "plain([H|REST],ACC,L2) :- append(ACC,[H],ACC1),plain(REST,ACC1,L2).\n" + "plain(X,ACC,L2) :- append(ACC,[X],L2).\n" + "ordina(L, N, T) :- thread_create(ID, firstResp(L, N)), secondResp(L, N, T).\n" + "secondResp(L, 0, T):- !.\n" + "secondResp([H|Tail], N, T) :- occorr(T,H,Count), C is N-1, secondResp(Tail,C, T).\n" + "firstResp(L, 0) :- !.\n" + "firstResp([H|Tail], N) :- plain(H,L_plain), bubble(L_plain,L_ord), C is N - 1, firstResp(Tail, C).\n" + "occorr(T,L,N) :- occorr(T,L,0,N).\n" + "occorr(_,[],ACC,ACC).\n" + "occorr(T,[T|REST],ACC,N) :-ACC1 is ACC+1,occorr(T,REST,ACC1,N).\n" + "occorr(T,[_|REST],ACC,N) :- occorr(T,REST,ACC,N)."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("ordina([[[2,2],2,2,1],[4,[3],2],[9,8,9,2]],3, 2).")
    sinfo.isSuccess shouldBe true
  }

  it should "pass concurrency test #2" in {
    val theory = "coppie(L,C,DX,SX):-thread_create(ID1, coppieDX(L,C,DX)), thread_create(ID2, coppieSX(L,C,SX)).\n" + "coppieDX([],_,[]).\n " + "coppieDX([[X,X]|T],X,[X|Td]) :- !,coppieDX(T,X,Td).\n " + "coppieDX([[X,Y]|T],X,[Y|Td]) :- !,coppieDX(T,X,Td).\n " + "coppieDX([_|T],X,Td) :- coppieDX(T,X,Td).\n" + "coppieSX([],_,[]).\n " + "coppieSX([[X,X]|T],X,[X|Ts]) :- !,coppieSX(T,X,Ts). \n" + "coppieSX([[Y,X]|T],X,[Y|Ts]) :- !,coppieSX(T,X,Ts). \n" + "coppieSX([_|T],X,Ts) :- coppieSX(T,X,Ts)."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("coppie([[2,3],[5,2]], 2, DX,SX).")
    sinfo.isSuccess shouldBe true
  }

  it should "pass concurrency test #3" in {
    val theory = "study(L_stud, L_exams, N,Num) :- thread_create(ID, loop(1, N, 1, L_stud, L_exams)), length(L_exams,Num). \n" + "length([],0).\n" + "length([_|Queue],N):-length(Queue,Nqueue),N is Nqueue + 1.\n" + "loop(I, To, Inc, L_stud, L_exams) :- Inc >= 0, I > To, !.\n" + "loop(I, To, Inc, L_stud, L_exams) :- Inc < 0,  I < To, !.\n" + "loop(I, To, Inc, [H|Tail], L_exams) :- thread_create((totStud(H,L_exams,N,T), N > 0, AV is T/N),ID),  Next is I+Inc, loop(Next, To, Inc, Tail, L_exams).\n" + "totStud(_,[],0,0) :- !. \n" + "totStud(S,[exam(S,_,V)|R],N,T) :- !, totStud(S,R,NN,TT), N is NN + 1, T is TT + V. \n" + "totStud(S,[_|R],N,T) :- totStud(S,R,N,T)."
    prolog.setTheory(new Theory(theory))
    val sinfo = prolog.solve("study([s1,s2,s3,s4,s5],[exam(s2,f1,30), exam(s1,f1,27), exam(s3,f1,25), exam(s1,f2,30),exam(s4,f1,25),exam(s3,f2,20),exam(s5,f1,20),exam(s2,f5,30), exam(s1,f5,27), exam(s3,f5,25), exam(s1,f4,30),exam(s4,f5,25),exam(s3,f8,20),exam(s5,f7,20)], 5,Num).")
    sinfo.isSuccess shouldBe true
  }


}
