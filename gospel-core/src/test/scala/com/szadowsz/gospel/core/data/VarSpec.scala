package com.szadowsz.gospel.core.data

import com.szadowsz.gospel.core.db.libs.DCGLibrary
import com.szadowsz.gospel.core.event.io.OutputEvent
import com.szadowsz.gospel.core.listener.OutputListener
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Solution, Theory}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class VarSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Var"

  it should "not be atomic" in {
    new Var("X").isAtomic shouldBe false
  }

  it should "not be an atom" in {
    new Var("X").isAtom shouldBe false
  }

  it should "not be a compound" in {
    new Var("X").isCompound shouldBe false
  }

  it should "not allow different vars to be equal" in {
    val sysoutListener = new OutputListener {
      val builder = new StringBuilder("")

      override def onOutput(ev: OutputEvent): Unit = {
        builder.append(ev.getMsg)
      }

      def getAllOutput = builder.toString()
    }
    prolog.addOutputListener(sysoutListener)

    // theory is modified code from PTTP
    val theory =
      """
        |test :- body_for_head_literal_instrumented(d(X,Y),(not_d(X,U);d(X,Y)),Bod).
        |
        |body_for_head_literal_instrumented(Head,Wff,Body) :-
        |  nl,print('body_for_head_literal input Head: '),print(Head),
        |  nl,print('                             Wff: '),print(Wff),
        |  false -> true ;
        |  Wff = (A ; B) ->
        |    nl,print('OR'),
        |    body_for_head_literal_instrumented(Head,A,A1),
        |    body_for_head_literal_instrumented(Head,B,B1),
        |    conjoin(A1,B1,Body)
        |    , nl, print('body_for_head_literal OR - Body: '),print(Body)
        |    ;
        |  Wff == Head ->
        |    Body = true;
        |  negated_literal_instrumented(Wff,Head) ->
        |    print(' '),
        |    Body = false;
        |  %true ->
        |    nl,print('OTHERWISE'),
        |    negated_literal_instrumented(Wff,Body).
        |
        |negated_literal_instrumented(Lit,NotLit) :-
        |  nl,print('*** negated_literal in Lit:'),print(Lit),
        |  nl,print('***                 NotLit:'),print(NotLit),
        |  Lit =.. [F1|L1],
        |  negated_functor(F1,F2),
        |  (var(NotLit) ->
        |    NotLit =.. [F2|L1];
        |  %true ->
        |    nl,print('                 Not var:'),print(NotLit),
        |    NotLit =.. [F2|L2],
        |    nl,print('***              Lit array:'),print(L1),
        |    nl,print('***           NotLit array:'),print(L2),
        |    L1 == L2
        |    , nl,print('***               SUCCEEDS')
        |    ).
        |
        |negated_functor(F,NotF) :-
        |  atom_chars(F,L),
        |  atom_chars(not_,L1),
        |  (list_append(L1,L2,L) ->
        |    true;
        |  %true ->
        |    list_append(L1,L,L2)),
        |  atom_chars(NotF,L2).
        |
        |conjoin(A,B,C) :-
        |  A == true ->
        |    C = B;
        |  B == true ->
        |    C = A;
        |  A == false ->
        |    C = false;
        |  B == false ->
        |    C = false;
        |  %true ->
        |    % nl,print('conjoin A: '),print(A),print(' B: '),print(B),
        |    C = (A , B)
        |    % , nl,print('    out A: '),print(A),print(' B: '),print(B)
        |    % , nl,print('        C: '),print(C)
        |  .
        |
        |list_append([X|L1],L2,[X|L3]) :-
        |  list_append(L1,L2,L3).
        |list_append([],L,L).
        |
      """.stripMargin // ERROR HAPPENS LINE 82 (L1 == L2)
    prolog.setTheory(new Theory(theory))
    val info = prolog.solve("test.")
    info.isSuccess shouldBe true
    val expected = "" + "\n" + "body_for_head_literal input Head: d(X_e1,Y_e1)" + "\n" + "                             Wff: ';'(not_d(X_e1,U_e1),d(X_e1,Y_e1))" + "\n" + "OR" + "\n" + "body_for_head_literal input Head: d(X_e25,Y_e25)" + "\n" + "                             Wff: not_d(X_e25,U_e25)" + "\n" + "*** negated_literal in Lit:not_d(X_e25,U_e25)  NotLit:d(X_e25,Y_e25)" + "\n" + "***              Lit array:[X_e122,U_e86]" + "\n" + "***           NotLit array:[X_e122,Y_e122]" + "\n" + "OTHERWISE" + "\n" + "*** negated_literal in Lit:not_d(X_e122,U_e86)  NotLit:NotLit_e136" + "\n" + "body_for_head_literal input Head: d(X_e184,Y_e122)" + "\n" + "                             Wff: d(X_e184,Y_e122)" + "\n" + "Wff == Head" + "\n" + "body_for_head_literal OR - Body: d(X_e249,U_e249)" + "\n" + ""
    sysoutListener.getAllOutput shouldBe expected
  }
}
