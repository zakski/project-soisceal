/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *//*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.szadowsz.gospel.core.db.libs

import java.util

import alice.tuprolog._
import alice.tuprolog.lib.BasicLibrary
import com.szadowsz.gospel.core.Theory

import scala.util.control.NonFatal
import scala.collection.JavaConverters._
import scala.util.Try

/**
  * This class defines a set of basic built-in predicates for the tuProlog engine
  *
  */
@SerialVersionUID(1L)
class MyBasicLibrary() extends BasicLibrary {

  @throws[PrologError]
  private def evalNumExpression(arg: Term, argNum: scala.Int): Number = {
    val evaled = try {
      evalExpression(arg)
    } catch {
      case e: ArithmeticException if e.getMessage == "/ by zero" => throw PrologError.evaluation_error(engine.getEngineManager, argNum, "zero_divisor")
      case _ => throw PrologError.type_error(engine.getEngineManager, 1, "evaluable", arg.getTerm)
    }
    if (!evaled.isInstanceOf[Number]) {
      throw PrologError.type_error(engine.getEngineManager, argNum, "evaluable", arg.getTerm)
    } else {
      evaled.asInstanceOf[Number]
    }
  }

  private def expression_greater_than(num0: Number, num1: Number): Boolean = {
    if (num0.isInteger && num1.isInteger) {
      num0.longValue > num1.longValue
    } else {
      num0.doubleValue > num1.doubleValue
    }
  }

  private def expression_less_than(num0: Number, num1: Number): Boolean = {
    if (num0.isInteger && num1.isInteger) {
      num0.longValue < num1.longValue
    } else {
      num0.doubleValue < num1.doubleValue
    }
  }

  override def getIntegerNumber(num: scala.Long): Number = {
    if (num > Integer.MIN_VALUE && num < Integer.MAX_VALUE) new Int(num.toInt) else new Long(num)
  }


  /**
    * Sets a new theory provided as a text.
    *
    * @param arg1 the new theory.
    * @throws PrologError if arg1 is not a valid theory.
    * @return true if successful.
    */
  @throws[PrologError]
  override def set_theory_1(arg1: Term): Boolean = {
    arg1.getTerm match {
      case v: Var => throw PrologError.instantiation_error(engine.getEngineManager, 1)
      case nonAtom if !nonAtom.isAtom => throw PrologError.type_error(engine.getEngineManager, 1, "atom", nonAtom)
      case struct: Struct =>
        try {
          engine.setTheory(new Theory(struct.getName))
          true
        } catch {
          case ex: InvalidTheoryException => throw PrologError.syntax_error(engine.getEngineManager, ex.clause, ex.line, ex.pos, new Struct(ex.getMessage))
        }
    }
  }

  /**
    * Appends a new theory provided as a text.
    *
    * @param arg1 the new theory.
    * @throws PrologError if arg1 is not a valid theory.
    * @return true if successful.
    */
  @throws[PrologError]
  override def add_theory_1(arg1: Term): Boolean = {
    arg1.getTerm match {
      case v: Var => throw PrologError.instantiation_error(engine.getEngineManager, 1)
      case nonAtom if !nonAtom.isAtom => throw PrologError.type_error(engine.getEngineManager, 1, "atom", nonAtom)
      case struct: Struct =>
        try {
          engine.addTheory(new Theory(struct.getName))
          true
        } catch {
          case ex: InvalidTheoryException => throw PrologError.syntax_error(engine.getEngineManager, ex.clause, ex.line, ex.pos, new Struct(ex.getMessage))
        }
    }
  }

  /**
    * Gets current theory text.
    *
    * @param arg1 term to unify the theory to.
    * @return true if successfully unified, false otherwise.
    */
  override def get_theory_1(arg1: Term): Boolean = {
    try {
      unify(arg1.getTerm, new Struct(engine.getTheory.toString))
    } catch {
      case NonFatal(_) => false
    }
  }

  /**
    * Loads a library constructed from a theory.
    *
    * @param th theory text
    * @param libName
    *           name of the library
    * @return true if the library has been succesfully loaded.
    */
  override def load_library_from_theory_2(th: Term, libName: Term): Boolean = {
    try {
      val theory: Struct = th.getTerm.asInstanceOf[Struct]
      val libN: Struct = libName.getTerm.asInstanceOf[Struct]
      if (!theory.isAtom || !libN.isAtom) {
        false
      } else {
        val t: Theory = new Theory(theory.getName)
        val thlib: TheoryLibrary = TheoryLibrary(libN.getName, t)
        engine.loadLibrary(thlib)
        true
      }
    } catch {
      case NonFatal(_) => false
    }
  }

  override def get_operators_list_1(argument: Term): Boolean = {
    val arg: Term = argument.getTerm
    val list: Struct = engine.getCurrentOperatorList.asScala.foldLeft(new Struct) { case (l, o) =>
      new Struct(new Struct("op", new Int(o.prio), new Struct(o.`type`), new Struct(o.name)), l)
    }
    unify(arg, list)
  }

  /**
    * spawns a separate prolog agent providing it a theory text
    *
    * @throws PrologError
    */
  @throws[PrologError]
  override def agent_1(th: Term): Boolean = {
    th.getTerm match {
      case v: Var => throw PrologError.instantiation_error(engine.getEngineManager, 1)
      case t if !t.isAtom => throw PrologError.type_error(engine.getEngineManager, 1, "atom", t)
      case theory: Struct =>
        try {
          new Agent(alice.util.Tools.removeApices(theory.toString)).spawn()
          true
        } catch {
          case NonFatal(ex) =>
            ex.printStackTrace()
            false
        }
    }
  }

  /**
    * spawns a separate prolog agent providing it a theory text and a goal
    *
    * @throws PrologError
    */
  @throws[PrologError]
  override def agent_2(th: Term, g: Term): Boolean = {
    (th.getTerm, g.getTerm) match {
      case (v1: Var, _) => throw PrologError.instantiation_error(engine.getEngineManager, 1)
      case (_, v2: Var) => throw PrologError.instantiation_error(engine.getEngineManager, 2)
      case (t1, _) if !t1.isAtom => throw PrologError.type_error(engine.getEngineManager, 1, "atom", t1)
      case (_, goal) if !goal.isInstanceOf[Struct] => throw PrologError.type_error(engine.getEngineManager, 2, "struct", goal)
      case (theory: Struct, goal: Struct) =>
        try {
          new Agent(alice.util.Tools.removeApices(theory.toString), goal.toString + ".").spawn()
          true
        } catch {
          case NonFatal(ex) =>
            ex.printStackTrace()
            false
        }
    }
  }

  override def spy_0: Boolean = {
    getEngine.setSpy(true)
    true
  }

  override def nospy_0: Boolean = {
    getEngine.setSpy(false)
    true
  }

  override def trace_0: Boolean = spy_0

  override def notrace_0: Boolean = nospy_0

  override def warning_0: Boolean = {
    engine.setWarning(true)
    true
  }

  override def nowarning_0: Boolean = {
    engine.setWarning(false)
    true
  }

  override def constant_1(t: Term): Boolean = t.getTerm.isAtomic

  override def number_1(t: Term): Boolean = t.getTerm.isInstanceOf[Number]

  override def integer_1(t: Term): Boolean = t.getTerm.isInstanceOf[Number] && t.getTerm.asInstanceOf[Number].isInteger

  override def float_1(t: Term): Boolean = t.getTerm.isInstanceOf[Number] && t.getTerm.asInstanceOf[Number].isReal

  override def atom_1(t: Term): Boolean = t.getTerm.isAtom

  override def compound_1(t: Term): Boolean = t.getTerm.isCompound

  override def list_1(t: Term): Boolean = t.getTerm.isList

  override def var_1(t: Term): Boolean = t.getTerm.isInstanceOf[Var]

  override def nonvar_1(t: Term): Boolean = !t.getTerm.isInstanceOf[Var]

  override def atomic_1(t: Term): Boolean = t.getTerm.isAtomic

  override def ground_1(t: Term): Boolean = t.getTerm.isGround

  @throws[PrologError]
  override def expression_equality_2(arg0: Term, arg1: Term): Boolean = {
    if (arg0.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 1)
    } else if (arg1.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 2)
    }
    val val0 = evalNumExpression(arg0, 1)
    val val1 = evalNumExpression(arg1, 2)
    if (val0.isInteger && val1.isInteger) {
      // by ED: note that this would work also with intValue, even with Long args,
      // because in that case both values would be wrong, but 'equally wrong' :)
      // However, it is much better to always operate consistently on long values
      val0.longValue == val1.longValue
    } else {
      val0.doubleValue == val1.doubleValue
    }
  }

  @throws[PrologError]
  override def expression_greater_than_2(arg0: Term, arg1: Term): Boolean = {
    if (arg0.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 1)
    } else if (arg1.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 2)
    }
    val val0 = evalNumExpression(arg0, 1)
    val val1 = evalNumExpression(arg1, 2)
    expression_greater_than(val0, val1)
  }

  @throws[PrologError]
  override def expression_less_or_equal_than_2(arg0: Term, arg1: Term): Boolean = {
    if (arg0.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 1)
    } else if (arg1.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 2)
    }
    val val0 = evalNumExpression(arg0, 1)
    val val1 = evalNumExpression(arg1, 2)
    !expression_greater_than(val0, val1)
  }

  @throws[PrologError]
  override def expression_less_than_2(arg0: Term, arg1: Term): Boolean = {
    if (arg0.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 1)
    } else if (arg1.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 2)
    }
    val val0 = evalNumExpression(arg0, 1)
    val val1 = evalNumExpression(arg1, 2)
    expression_less_than(val0, val1)
  }

  @throws[PrologError]
  override def expression_greater_or_equal_than_2(arg0: Term, arg1: Term): Boolean = {
    if (arg0.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 1)
    } else if (arg1.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 2)
    }
    val val0 = evalNumExpression(arg0, 1)
    val val1 = evalNumExpression(arg1, 2)
    !expression_less_than(val0, val1)
  }


  @throws[PrologError]
  override def term_equality_2(arg0: Term, arg1: Term): Boolean = arg0.getTerm.isEqual(arg1.getTerm)

  @throws[PrologError]
  override def term_greater_than_2(arg0: Term, arg1: Term): Boolean = arg0.getTerm.isGreater(arg1.getTerm)

  @throws[PrologError]
  override def term_less_than_2(arg0: Term, arg1: Term): Boolean = !(arg0.getTerm.isGreater(arg1.getTerm) || arg0.getTerm.isEqual(arg1.getTerm))

  override def expression_plus_1(arg0: Term): Term = Try(evalExpression(arg0).asInstanceOf[Number]).toOption.orNull

  override def expression_minus_1(arg0: Term): Term = {
    Try(evalExpression(arg0).asInstanceOf[Number]).toOption match {
      case Some(i: Int) => new Int(i.intValue * -1)
      case Some(d: Double) => new Double(d.doubleValue * -1)
      case Some(l: Long) => new Long(l.longValue * -1)
      case Some(f: Float) => new Float(f.floatValue * -1)
      case _ => null
    }
  }

  override def expression_bitwise_not_1(arg0: Term): Term = {
    Try(evalExpression(arg0).asInstanceOf[Number]).toOption.map(n => new Long(~n.longValue)).orNull
  }

  override def expression_plus_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) =>
        if (val0.isInteger && val1.isInteger) {
          getIntegerNumber(val0.longValue + val1.longValue)
        } else {
          new Double(val0.doubleValue + val1.doubleValue)
        }
      case None => null
    }
  }

  override def expression_minus_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) =>
        if (val0.isInteger && val1.isInteger) {
          getIntegerNumber(val0.longValue - val1.longValue)
        } else {
          new Double(val0.doubleValue - val1.doubleValue)
        }
      case None => null
    }
  }

  override def expression_multiply_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) =>
        if (val0.isInteger && val1.isInteger) {
          getIntegerNumber(val0.longValue * val1.longValue)
        } else {
          new Double(val0.doubleValue * val1.doubleValue)
        }
      case None => null
    }
  }

  override def expression_div_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) =>
        if (val0.isInteger && val1.isInteger) {
          getIntegerNumber(val0.longValue / val1.longValue)
        } else {
          new Double(val0.doubleValue / val1.doubleValue)
        }
      case None => null
    }
  }

  override def expression_integer_div_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) => getIntegerNumber(val0.longValue / val1.longValue)
      case None => null
    }
  }

  override def expression_pow_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) => new Double(Math.pow(val0.doubleValue, val1.doubleValue))
      case None => null
    }
  }

  override def expression_bitwise_shift_right_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) => new Long(val0.longValue >> val1.longValue)
      case None => null
    }
  }

  override def expression_bitwise_shift_left_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) => new Long(val0.longValue << val1.longValue)
      case None => null
    }
  }

  override def expression_bitwise_and_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) => new Long(val0.longValue & val1.longValue)
      case None => null
    }
  }

  override def expression_bitwise_or_2(arg0: Term, arg1: Term): Term = {
    Try((evalExpression(arg0).asInstanceOf[Number], evalExpression(arg1).asInstanceOf[Number])).toOption match {
      case Some((val0, val1)) => new Long(val0.longValue | val1.longValue)
      case None => null
    }
  }

  /**
    * bidirectional text/term conversion.
    */
  override def text_term_2(arg0: Term, arg1: Term): Boolean = {
    val t0 = arg0.getTerm
    val t1 = arg1.getTerm
    getEngine.stdOutput(t0.toString + "\n" + t1.toString)
    if (!t0.isGround) {
      unify(t0, new Struct(t1.toString))
    } else {
      Try(unify(t1, engine.createTerm(alice.util.Tools.removeApices(t0.toString)))).toOption.getOrElse(false)
    }
  }

  @throws[PrologError]
  override def text_concat_3(source1: Term, source2: Term, dest: Term): Boolean = {
    (source1.getTerm, source2.getTerm) match {
      case (s1: Var, _) => throw PrologError.instantiation_error(engine.getEngineManager, 1)
      case (_, s2: Var) => throw PrologError.instantiation_error(engine.getEngineManager, 2)
      case (s1, _) if !s1.isAtom => throw PrologError.type_error(engine.getEngineManager, 1, "atom", s1)
      case (_, s2) if !s2.isAtom => throw PrologError.type_error(engine.getEngineManager, 2, "atom", s2)
      case (s1: Struct, s2: Struct) => unify(dest.getTerm, new Struct(s1.getName + s2.getName))
    }
  }

  //  @throws[PrologError]
  //  def num_atom_2(a0: Term, a1: Term): Boolean = {
  //    val result = alice.tuprolog.lib.BasicLibrary.numAtom(engine,a0,a1)
  //    unify(result.head, result.last)
  //  }
  //
  // throw/1
  @throws[PrologError]
  override def throw_1(error: Term): Boolean = throw new PrologError(error)

//  override def getTheory: String = {
//    """':-'(op( 1200, fx,   ':-')).
//      |:- op( 1200, xfx,  ':-').
//      |:- op( 1200, fx,   '?-').
//      |:- op( 1100, xfy,  ';').
//      |:- op( 1050, xfy,  '->').
//      |:- op( 1000, xfy,  ',').
//      |:- op(  900, fy,   '\+').
//      |:- op(  900, fy,   'not').
//      |:- op(  700, xfx,  '=').
//      |:- op(  700, xfx,  '\=').
//      |:- op(  700, xfx,  '==').
//      |:- op(  700, xfx,  '\==').
//      |:- op(  700, xfx,  '@>').
//      |:- op(  700, xfx,  '@<').
//      |:- op(  700, xfx,  '@=<').
//      |:- op(  700, xfx,  '@>=').
//      |:- op(  700, xfx,  '=:=').
//      |:- op(  700, xfx,  '=\=').
//      |:- op(  700, xfx,  '>').
//      |:- op(  700, xfx,  '<').
//      |:- op(  700, xfx,  '=<').
//      |:- op(  700, xfx,  '>=').
//      |:- op(  700, xfx,  'is').
//      |:- op(  700, xfx,  '=..').
//      |:- op(  500, yfx,  '+').
//      |:- op(  500, yfx,  '-').
//      |:- op(  500, yfx,  '/\').
//      |:- op(  500, yfx,  '\/').
//      |:- op(  400, yfx,  '*').
//      |:- op(  400, yfx,  '/').
//      |:- op(  400, yfx,  '//').
//      |:- op(  400, yfx,  '>>').
//      |:- op(  400, yfx,  '<<').
//      |:- op(  400, yfx,  'rem').
//      |:- op(  400, yfx,  'mod').
//      |:- op(  200, xfx,  '**').
//      |:- op(  200, xfy,  '^').
//      |:- op(  200, fy,   '\').
//      |:- op(  200, fy,   '-').
//      |current_prolog_flag(Name,Value) :- catch(get_prolog_flag(Name,Value), Error, false),!.
//      |current_prolog_flag(Name,Value) :- flag_list(L), member(flag(Name,Value),L).
//      |'=:='(X,Y):- expression_equality(X,Y).
//      |'=\='(X,Y):- not expression_equality(X,Y).
//      |'>'(X,Y):- expression_greater_than(X,Y).
//      |'<'(X,Y):- expression_less_than(X,Y).
//      |'>='(X,Y):- expression_greater_or_equal_than(X,Y).
//      |'=<'(X,Y):- expression_less_or_equal_than(X,Y).
//      |'=='(X,Y):- term_equality(X,Y).
//      |'\=='(X,Y):- not term_equality(X,Y).
//      |'@>'(X,Y):- term_greater_than(X,Y).
//      |'@<'(X,Y):- term_less_than(X,Y).
//      |'@>='(X,Y):- not term_less_than(X,Y).
//      |'@=<'(X,Y):- not term_greater_than(X,Y).
//      |'=..'(T, [T]) :- atomic(T), !.
//      |'=..'(T,L)  :- compound(T),!, '$tolist'(T,L).
//      |'=..'(T,L)  :- nonvar(L), catch('$fromlist'(T,L),Error,false).
//      |functor(Term, Functor, Arity) :- \+ var(Term), !, Term =.. [Functor|ArgList],length(ArgList, Arity).
//      |functor(Term, Functor, Arity) :- var(Term), atomic(Functor), Arity == 0, !, Term = Functor.
//      |functor(Term, Functor, Arity) :- var(Term), current_prolog_flag(max_arity, Max), Arity>Max, !, false.
//      |functor(Term, Functor, Arity) :- var(Term), atom(Functor), number(Arity), Arity > 0, !,length(ArgList, Arity),Term =.. [Functor|ArgList].
//      |functor(_Term, _Functor, _Arity) :-false.
//      |arg(N,C,T):- arg_guard(N,C,T), C =.. [_|Args], element(N,Args,T).
//      |clause(H, B) :- clause_guard(H,B), L = [], '$find'(H, L), copy_term(L, LC), member((':-'(H, B)), LC).
//      |call(G) :- call_guard(G), '$call'(G).
//      |'\+'(P):- P,!,fail.
//      |'\+'(_).
//      |C -> T ; B :- !, or((call(C), !, call(T)), '$call'(B)).
//      |C -> T :- call(C), !, call(T).
//      |or(A, B) :- '$call'(A).
//      |or(A, B) :- '$call'(B).
//      |A ; B :- A =.. ['->', C, T], !, ('$call'(C), !, '$call'(T) ; '$call'(B)).
//      |A ; B :- '$call'(A).
//      |A ; B :- '$call'(B).
//      |unify_with_occurs_check(X,Y):- !,X=Y.
//      |current_op(Pri,Type,Name):-get_operators_list(L),member(op(Pri,Type,Name),L).
//      |once(X) :- myonce(X).
//      |myonce(X):-X,!.
//      |repeat.
//      |repeat :- repeat.
//      |not(G) :- G,!,fail.
//      |not(_).
//      |catch(Goal, Catcher, Handler) :- call(Goal).
//      |findall(Template, Goal, Instances) :-
//      |all_solutions_predicates_guard(Template, Goal, Instances),
//      |L = [],
//      |'$findall0'(Template, Goal, L),
//      |Instances = L.
//      |'$findall0'(Template, Goal, L) :-
//      |call(Goal),
//      |copy_term(Template, CL),
//      |'$append'(CL, L),
//      |fail.
//      |'$findall0'(_, _, _).
//      |variable_set(T, []) :- atomic(T), !.
//      |variable_set(T, [T]) :- var(T), !.
//      |variable_set([H | T], [SH | ST]) :-
//      |variable_set(H, SH), variable_set(T, ST).
//      |variable_set(T, S) :-
//      |T =.. [_ | Args], variable_set(Args, L), flatten(L, FL), no_duplicates(FL, S), !.
//      |flatten(L, FL) :- '$flatten0'(L, FL), !.
//      |'$flatten0'(T, []) :- nonvar(T), T = [].
//      |'$flatten0'(T, [T]) :- var(T).
//      |'$flatten0'([H | T], [H | FT]) :-
//      |not(islist(H)), !, '$flatten0'(T, FT).
//      |'$flatten0'([H | T], FL) :-
//      |'$flatten0'(H, FH), '$flatten0'(T, FT), append(FH, FT, FL).
//      |islist([]).
//      |islist([_|L]):- islist(L).
//      |existential_variables_set(Term, Set) :- '$existential_variables_set0'(Term, Set), !.
//      |'$existential_variables_set0'(Term, []) :- var(Term), !.
//      |'$existential_variables_set0'(Term, []) :- atomic(Term), !.
//      |'$existential_variables_set0'(V ^ G, Set) :-
//      |variable_set(V, VS), '$existential_variables_set0'(G, EVS), append(VS, EVS, Set).
//      |'$existential_variables_set0'(Term, []) :- nonvar(Term), !.
//      |free_variables_set(Term, WithRespectTo, Set) :-
//      |variable_set(Term, VS),
//      |variable_set(WithRespectTo, VS1), existential_variables_set(Term, EVS1), append(VS1, EVS1, BV),
//      |list_difference(VS, BV, List), no_duplicates(List, Set), !.
//      |list_difference(List, Subtrahend, Difference) :- '$ld'(List, Subtrahend, Difference).
//      |'$ld'([], _, []).
//      |'$ld'([H | T], S, D) :- is_member(H, S), !, '$ld'(T, S, D).
//      |'$ld'([H | T], S, [H | TD]) :- '$ld'(T, S, TD).
//      |no_duplicates([], []).
//      |no_duplicates([H | T], L) :- is_member(H, T), !, no_duplicates(T, L).
//      |no_duplicates([H | T], [H | L]) :- no_duplicates(T, L).
//      |is_member(E, [H | _]) :- E == H, !.
//      |is_member(E, [_ | T]) :- is_member(E, T).
//      |'$wt_list'([], []).
//      |'$wt_list'([W + T | STail], [WW + T | WTTail]) :- copy_term(W, WW), '$wt_list'(STail, WTTail).
//      |'$s_next'(Witness, WT_List, S_Next) :- copy_term(Witness, W2), '$s_next0'(W2, WT_List, S_Next), !.
//      |bagof(Template, Goal, Instances) :-
//      |all_solutions_predicates_guard(Template, Goal, Instances),
//      |free_variables_set(Goal, Template, Set),
//      |Witness =.. [witness | Set],
//      |iterated_goal_term(Goal, G),
//      |all_solutions_predicates_guard(Template, G, Instances), 'splitAndSolve'(Witness, S, Instances,Set,Template,G,Goal).
//      |count([],0).
//      |count([T1|Ts],N):- count(Ts,N1), N is (N1+1).
//      |is_list(X) :- var(X), !,fail.
//      |is_list([]).
//      |is_list([_|T]) :- is_list(T).
//      |list_to_term([T1|Ts],N):- count(Ts,K),K==0
//      |->  N = T1,!
//      |; list_to_term(Ts,N1), N = ';'(T1,N1),!.
//      |list_to_term(Atom,Atom).
//      |quantVar(X^Others, [X|ListOthers]) :- !,quantVar(Others, ListOthers).
//      |quantVar(_Others, []).
//      |'splitAndSolve'(Witness, S, Instances,Set,Template,G,Goal):- splitSemicolon(G,L),
//      |variable_set(Template,TT),
//      |quantVar(Goal, Qvars),
//      |append(TT,Qvars,L1),
//      |'aggregateSubgoals'(L1,L,OutputList),
//      |member(E,OutputList),
//      |list_to_term(E, GoalE),
//      |findall(Witness + Template, GoalE, S),
//      |'bag0'(Witness, S, Instances,Set,Template,GoalE).
//      |splitSemicolon(';'(G1,Gs),[G1|Ls]) :-!, splitSemicolon(Gs,Ls).
//      |splitSemicolon(G1,[G1]).
//      |aggregateSubgoals(Template, List, OutputList) :-
//      |aggregateSubgoals(Template, List, [], [], OutputList).
//      |aggregateSubgoals(Template, [H|T], CurrentAccumulator, Others, OutputList) :-
//      |'occurs0'(Template, H)
//      |-> aggregateSubgoals(Template, T, [H|CurrentAccumulator], Others, OutputList)
//      |; aggregateSubgoals(Template, T, CurrentAccumulator, [H|Others], OutputList).
//      |aggregateSubgoals(_, [], CurrentAccumulator, NonAggregatedList, [Result1|Result2]):-
//      |reverse(CurrentAccumulator, Result1), reverse(NonAggregatedList, Result2).
//      |occurs_member_list([], L):-!,fail.
//      |occurs_member_list([H|T], L) :- is_member(H,L)
//      |-> true,!
//      |; occurs_member_list(T,L).
//      |occurs_member_list_of_term(L, []):-!,fail.
//      |occurs_member_list_of_term(Template,[H|T]):-'occurs0'(Template, H)
//      |-> true,!
//      |; occurs_member_list_of_term(Template,T).
//      |'check_sub_goal'(Template, H, _Functor, Arguments):- ((_Functor==';';_Functor==',')
//      |-> 'occurs0'(Template,Arguments)
//      |; ((_Functor=='.')
//      |->  occurs_member_list_of_term(Template,Arguments)
//      |; occurs_member_list(Template, Arguments))).
//      |'occurs0'(Template, H):-
//      |H =.. [_Functor | Arguments],
//      |'check_sub_goal'(Template, H, _Functor, Arguments).
//      |'bag0'(_, [], _,_,_,_) :- !, fail.
//      |'bag0'(Witness, S, Instances,Set,Template,Goal) :-
//      |S==[] -> fail, !;
//      |'$wt_list'(S, WT_List),
//      |'$wt_unify'(Witness, WT_List, Instances,Set,Template,Goal).
//      |'bag0'(Witness, S, Instances,Set,Template,Goal) :-
//      |'$wt_list'(S, WT_List),
//      |'$s_next'(Witness, WT_List, S_Next),
//      |'bag0'(Witness, S_Next, Instances,Set,Template,Goal).
//      |setof(Template, Goal, Instances) :-
//      |all_solutions_predicates_guard(Template, Goal, Instances),
//      |bagof(Template, Goal, List),
//      |quicksort(List, '@<', OrderedList),
//      |no_duplicates(OrderedList, Instances).
//      |forall(A,B):- \+(call(A),\+call(B)).
//      |assert(C) :- assertz(C).
//      |retract(Rule) :- retract_guard(Rule), Rule = ':-'(Head, Body), !, clause(Head, Body), '$retract'(Rule).
//      |retract(Fact) :- retract_guard(Fact), clause(Fact, true), '$retract'(Fact).
//      |retractall(Head) :- retract_guard(Head), findall(':-'(Head, Body), clause(Head, Body), L), '$retract_clause_list'(L), !.
//      |'$retract_clause_list'([]).
//      |'$retract_clause_list'([E | T]) :- !, '$retract'(E), '$retract_clause_list'(T).
//      | member(E,L) :- member_guard(E,L), member0(E,L).
//      |member0(E,[E|_]).
//      |member0(E,[_|L]):- member0(E,L).
//      |length(L, S) :- number(S), !, lengthN(L, S), !.
//      |length(L, S) :- var(S), lengthX(L, S).
//      |lengthN([],0).
//      |lengthN(_, N) :- N < 0, !, fail.
//      |lengthN([_|L], N) :- M is N - 1, lengthN(L,M).
//      |lengthX([],0).
//      |lengthX([_|L], N) :- lengthX(L,M), N is M + 1.
//      |append([],L2,L2).
//      |append([E|T1],L2,[E|T2]):- append(T1,L2,T2).
//      |reverse(L1,L2):- reverse_guard(L1,L2), reverse0(L1,[],L2).
//      |reverse0([],Acc,Acc).
//      |reverse0([H|T],Acc,Y):- reverse0(T,[H|Acc],Y).
//      |delete(E,S,D) :- delete_guard(E,S,D), delete0(E,S,D).
//      |delete0(E,[],[]).
//      |delete0(E,[E|T],L):- !,delete0(E,T,L).
//      |delete0(E,[H|T],[H|L]):- delete0(E,T,L).
//      |element(P,L,E):- element_guard(P,L,E), element0(P,L,E).
//      |element0(1,[E|L],E):- !.
//      |element0(N,[_|L],E):- M is N - 1,element0(M,L,E).
//      |quicksort([],Pred,[]).
//      |quicksort([X|Tail],Pred,Sorted):-
//      |   split(X,Tail,Pred,Small,Big),
//      |   quicksort(Small,Pred,SortedSmall),
//      |   quicksort(Big,Pred,SortedBig),
//      |   append(SortedSmall,[X|SortedBig],Sorted).
//      |split(_,[],_,[],[]).
//      |split(X,[Y|Tail],Pred,Small,[Y|Big]):-
//      |   Predicate =..[Pred,X,Y],
//      |   call(Predicate), !,
//      |   split(X,Tail,Pred,Small,Big).
//      |split(X,[Y|Tail],Pred,[Y|Small],Big):-
//      |   split(X,Tail,Pred,Small,Big).""".stripMargin.replaceAll("\r\n","\n")
//  }


  @throws[PrologError]
  override def arg_guard_3(a0: Term, a1: Term, arg2: Term): Boolean = {
    val arg0 = a0.getTerm
    val arg1 = a1.getTerm
    if (arg0.isInstanceOf[Var]) throw PrologError.instantiation_error(engine.getEngineManager, 1)
    if (arg1.isInstanceOf[Var]) throw PrologError.instantiation_error(engine.getEngineManager, 2)
    if (!arg0.isInstanceOf[Int]) throw PrologError.type_error(engine.getEngineManager, 1, "integer", arg0)
    if (!arg1.isCompound) throw PrologError.type_error(engine.getEngineManager, 2, "compound", arg1)
    val arg0int: Int = arg0.asInstanceOf[Int]
    if (arg0int.intValue < 1) throw PrologError.domain_error(engine.getEngineManager, 1, "greater_than_zero", arg0)
    true
  }

  @throws[PrologError]
  override def clause_guard_2(arg0: Term, arg1: Term): Boolean = {
    if (arg0.getTerm.isInstanceOf[Var]) {
      throw PrologError.instantiation_error(engine.getEngineManager, 1)
    } else {
      true
    }
  }

  @throws[PrologError]
  override def call_guard_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case v: Var => throw PrologError.instantiation_error(engine.getEngineManager, 1)
      case a if !a.isAtom && !a.isCompound => throw PrologError.type_error(engine.getEngineManager, 1, "callable", a)
      case _ => true
    }
  }

  @throws[PrologError]
  override def all_solutions_predicates_guard_3(arg0: Term, arg1: Term, arg2: Term): Boolean = {
    arg1.getTerm match {
      case v: Var => throw PrologError.instantiation_error(engine.getEngineManager, 2)
      case a if !a.isAtom && !a.isCompound => throw PrologError.type_error(engine.getEngineManager, 2, "callable", a)
      case _ => true
    }
  }

  @throws[PrologError]
  override def retract_guard_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case v: Var => throw PrologError.instantiation_error(engine.getEngineManager, 1)
      case t if !t.isInstanceOf[Struct] => throw PrologError.type_error(engine.getEngineManager, 1, "clause", arg0)
      case _ => true
    }
  }

  @throws[PrologError]
  override def member_guard_2(arg0: Term, arg1: Term): Boolean = {
    arg1.getTerm match {
      case arg if !arg.isInstanceOf[Var] && !arg.isList => throw PrologError.type_error(engine.getEngineManager, 2, "list", arg)
      case _ => true
    }
  }

  @throws[PrologError]
  override def reverse_guard_2(arg0: Term, arg1: Term): Boolean = {
    arg0.getTerm match {
      case arg if !arg.isInstanceOf[Var] && !arg.isList => throw PrologError.type_error(engine.getEngineManager, 1, "list", arg)
      case _ => true
    }
  }

  @throws[PrologError]
  override def delete_guard_3(arg0: Term, arg1: Term, arg2: Term): Boolean = {
    arg1.getTerm match {
      case arg if !arg.isInstanceOf[Var] && !arg.isList => throw PrologError.type_error(engine.getEngineManager, 2, "list", arg)
      case _ => true
    }
  }

  @throws[PrologError]
  override def element_guard_3(arg0: Term, arg1: Term, arg2: Term): Boolean = {
    arg1.getTerm match {
      case arg if !arg.isInstanceOf[Var] && !arg.isList => throw PrologError.type_error(engine.getEngineManager, 2, "list", arg)
      case _ => true
    }
  }

  override def $wt_copyAndRetainFreeVar_2(arg0: Term, arg1: Term): Boolean = {
    val id = engine.getEngineManager.getEnv.getNDemoSteps
    unify(arg1.getTerm, arg0.getTerm.copyAndRetainFreeVar(new util.IdentityHashMap[Var, Var], id))
  }

  override def $wt_unify_3(witness: Term, wtList: Term, tList: Term): Boolean = {
    val list: Struct = wtList.getTerm.asInstanceOf[Struct]
    val result: Struct = new Struct
    val it: util.Iterator[_ <: Term] = list.listIterator
    while (it.hasNext) {
      {
        val element: Struct = it.next.asInstanceOf[Struct]
        val w: Term = element.getArg(0)
        val t: Term = element.getArg(1)
        if (unify(witness, w)) result.append(t)
      }
    }
    unify(tList, result)
  }

  override def $s_next0_3(witness: Term, wtList: Term, sNext: Term): Boolean = {
    val list: Struct = wtList.getTerm.asInstanceOf[Struct]
    val result: Struct = new Struct
    val it: util.Iterator[_ <: Term] = list.listIterator
    while (it.hasNext) {
      {
        val element: Struct = it.next.asInstanceOf[Struct]
        val w: Term = element.getArg(0)
        if (!unify(witness, w)) result.append(element)
      }
    }
    unify(sNext, result)
  }

  override def iterated_goal_term_2(term: Term, goal: Term): Boolean = unify(term.getTerm.iteratedGoalTerm, goal)

  /**
    * Defines some synonyms
    */
  override def getSynonymMap: Array[Array[String]] = {
    Array[Array[String]](
      Array("+", "expression_plus", "functor"),
      Array("-", "expression_minus", "functor"),
      Array("*", "expression_multiply", "functor"),
      Array("/", "expression_div", "functor"),
      Array("**", "expression_pow", "functor"),
      Array(">>", "expression_bitwise_shift_right", "functor"),
      Array("<<", "expression_bitwise_shift_left", "functor"),
      Array("/\\", "expression_bitwise_and", "functor"),
      Array("\\/", "expression_bitwise_or", "functor"),
      Array("//", "expression_integer_div", "functor"),
      Array("\\", "expression_bitwise_not", "functor"))
  }
}