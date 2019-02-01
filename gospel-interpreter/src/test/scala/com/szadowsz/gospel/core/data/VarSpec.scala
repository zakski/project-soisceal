/**
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
package com.szadowsz.gospel.core.data

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class VarSpec extends FunSpec with Matchers with BeforeAndAfter {//} with BaseEngineSpec {

//  override protected def init(): PrologEngine = new PrologEngine()
  
  describe("Unbound Var") {
  
    it("should not be Atomic") {
      new Var("X").isAtomic shouldBe false
    }
  
    it("should not be an Atom") {
      new Var("X").isAtom shouldBe false
    }
  
    it("should not be a Compound") {
      new Var("X").isCompound shouldBe false
    }
  
    it("should not be an empty List") {
      new Var("X").isEmptyList shouldBe false
    }
  
    it("should not be a Grounded") {
      new Var("X").isGround shouldBe false
    }
    
    it("should not be a List") {
      new Var("X").isList shouldBe false
    }
  }

//  it should "not allow different vars to be equal" in {
//    val sysoutListener = new OutputListener {
//      val builder = new StringBuilder("")
//
//      override def onOutput(ev: OutputEvent): Unit = {
//        builder.append(ev.getMsg)
//      }
//
//      def getAllOutput = builder.toString()
//    }
//    prolog.addOutputListener(sysoutListener)
//
//    // theory is modified code from PTTP
//    val theory =
//      """
//        |test :- body_for_head_literal_instrumented(d(X,Y),(not_d(X,U);d(X,Y)),Bod).
//        |
//        |body_for_head_literal_instrumented(Head,Wff,Body) :-
//        |  nl,print('body_for_head_literal input Head: '),print(Head),
//        |  nl,print('                             Wff: '),print(Wff),
//        |  false -> true ;
//        |  Wff = (A ; B) ->
//        |    nl,print('OR'),
//        |    body_for_head_literal_instrumented(Head,A,A1),
//        |    body_for_head_literal_instrumented(Head,B,B1),
//        |    conjoin(A1,B1,Body)
//        |    , nl, print('body_for_head_literal OR - Body: '),print(Body)
//        |    ;
//        |  Wff == Head ->
//        |    Body = true;
//        |  negated_literal_instrumented(Wff,Head) ->
//        |    print(' '),
//        |    Body = false;
//        |  %true ->
//        |    nl,print('OTHERWISE'),
//        |    negated_literal_instrumented(Wff,Body).
//        |
//        |negated_literal_instrumented(Lit,NotLit) :-
//        |  nl,print('*** negated_literal in Lit:'),print(Lit),
//        |  nl,print('***                 NotLit:'),print(NotLit),
//        |  Lit =.. [F1|L1],
//        |  negated_functor(F1,F2),
//        |  (var(NotLit) ->
//        |    NotLit =.. [F2|L1];
//        |  %true ->
//        |    nl,print('                 Not var:'),print(NotLit),
//        |    NotLit =.. [F2|L2],
//        |    nl,print('***              Lit array:'),print(L1),
//        |    nl,print('***           NotLit array:'),print(L2),
//        |    L1 == L2
//        |    , nl,print('***               SUCCEEDS')
//        |    ).
//        |
//        |negated_functor(F,NotF) :-
//        |  atom_chars(F,L),
//        |  atom_chars(not_,L1),
//        |  (list_append(L1,L2,L) ->
//        |    true;
//        |  %true ->
//        |    list_append(L1,L,L2)),
//        |  atom_chars(NotF,L2).
//        |
//        |conjoin(A,B,C) :-
//        |  A == true ->
//        |    C = B;
//        |  B == true ->
//        |    C = A;
//        |  A == false ->
//        |    C = false;
//        |  B == false ->
//        |    C = false;
//        |  %true ->
//        |    % nl,print('conjoin A: '),print(A),print(' B: '),print(B),
//        |    C = (A , B)
//        |    % , nl,print('    out A: '),print(A),print(' B: '),print(B)
//        |    % , nl,print('        C: '),print(C)
//        |  .
//        |
//        |list_append([X|L1],L2,[X|L3]) :-
//        |  list_append(L1,L2,L3).
//        |list_append([],L,L).
//        |
//      """.stripMargin // ERROR HAPPENS LINE 82 (L1 == L2)
//    prolog.setTheory(new Theory(theory))
//    val info = prolog.solve("test.")
//    info.isSuccess shouldBe true
//    val expected = "" + "\n" + "body_for_head_literal input Head: d(X_e1,Y_e1)" + "\n" + "                             Wff: ';'(not_d(X_e1,U_e1),d(X_e1,Y_e1))" + "\n" + "OR" + "\n" + "body_for_head_literal input Head: d(X_e25,Y_e25)" + "\n" + "                             Wff: not_d(X_e25,U_e25)" + "\n" + "*** negated_literal in Lit:not_d(X_e25,U_e25)  NotLit:d(X_e25,Y_e25)" + "\n" + "***              Lit array:[X_e122,U_e86]" + "\n" + "***           NotLit array:[X_e122,Y_e122]" + "\n" + "OTHERWISE" + "\n" + "*** negated_literal in Lit:not_d(X_e122,U_e86)  NotLit:NotLit_e136" + "\n" + "body_for_head_literal input Head: d(X_e184,Y_e122)" + "\n" + "                             Wff: d(X_e184,Y_e122)" + "\n" + "Wff == Head" + "\n" + "body_for_head_literal OR - Body: d(X_e249,U_e249)" + "\n" + ""
//    sysoutListener.getAllOutput shouldBe expected
//  }
}
