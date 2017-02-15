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
package com.szadowsz.gospel.core.db.lib

import java.io.{File, FileInputStream, FileNotFoundException, IOException}
import java.util

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.data.numeric.{Int, Number}
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.theory.Theory
import com.szadowsz.gospel.core.engine.clause.ClauseInfo
import com.szadowsz.gospel.core.operation.OperatorManager
import com.szadowsz.gospel.util.Tools
import com.szadowsz.gospel.util.exception.engine.PrologException
import com.szadowsz.gospel.util.exception.lib.InvalidLibraryException
import com.szadowsz.gospel.util.exception.theory.InvalidTheoryException

import scala.collection.Iterator
import scala.util.control.NonFatal

/**
  * Library of built-in predicates
  *
  * @author Alex Benini
  */
@SerialVersionUID(1L)
object BuiltIn {
  /**
    * Convert a term to a goal before executing it by means of call/1. See
    * section 7.6.2 of the ISO Standard for details.
    * <ul>
    * <li>If T is a variable then G is the control construct call, whose
    * argument is T.</li>
    * <li>If the principal functor of T is t ,?/2 or ;/2 or ->/2, then each
    * argument of T shall also be converted to a goal.</li>
    * <li>If T is an atom or compound term with principal functor FT, then G is
    * a predication whose predicate indicator is FT, and the arguments, if any,
    * of T and G are identical.</li>
    * </ul>
    * Note that a variable X and a term call(X) are converted to identical
    * bodies. Also note that if T is a number, then there is no goal which
    * corresponds to T.
    */
  private[lib] def convertTermToGoal(term: Term): Term = {
    term match {
      case n: Number => null
      case v1: Var if v1.getLink.isInstanceOf[Number] => null
      case default =>
        term.getTerm match {
          case v2: Var => new Struct("call", term)
          case s: Struct =>
            val pi: String = s.getPredicateIndicator
            if (pi == ";/2" || pi == ",/2" || pi == "->/2") {
              for (i <- 0 until s.getArity) {
                val t: Term = s.getArg(i)
                val arg: Term = convertTermToGoal(t)
                if (arg == null) return null
                s.setArg(i, arg)
              }
            }
            s
        }
    }
  }
}

@SerialVersionUID(1L)
class BuiltIn(val mediator: Prolog) extends Library {
  setEngine(mediator)

  private def engineManager = mediator.getEngineManager

  private def theoryManager = mediator.getTheoryManager

  private def libraryManager = mediator.getLibraryManager

  private def flagManager = mediator.getFlagManager

  private def primitiveManager = mediator.getPrimitiveManager

  private def operatorManager = mediator.getOperatorManager


  /**
    * A callable term is an atom of a compound term. See the ISO Standard
    * definition in section 3.24.
    */
  private def isCallable(goal: Term): Boolean = goal.isAtom || goal.isCompound

  @throws[PrologException]
  private def handleError(t: Throwable) {
    // errore durante la valutazione
    if (t.isInstanceOf[ArithmeticException]) {
      val cause: ArithmeticException = t.asInstanceOf[ArithmeticException]
      if (cause.getMessage == "/ by zero") throw PrologException.evaluation_error(engineManager, 2, "zero_divisor")
    }
  }


  private def getStringArrayFromStruct(list: Struct): Array[String] = list.map(s => Tools.removeApices(s.toString)).toArray

  /**
    * Defines some synonyms
    */
  override def getSynonymMap: Array[Array[String]] = {
    Array[Array[String]](
      Array("!", "cut", "predicate"),
      Array("=", "unify", "predicate"),
      Array("\\=", "deunify", "predicate"),
      Array(",", "comma", "predicate"),
      Array("op", "$op", "predicate"),
      Array("solve", "initialization", "directive"),
      Array("consult", "include", "directive"),
      Array("load_library", "$load_library", "directive"))
  }

  /**
    * Always fail. The predicate fail/0 is translated into a single virtual machine instruction.
    *
    * @return always returns false.
    */
  def fail_0: Boolean = false

  /**
    * Always succeed. The predicate true/0 is translated into a single virtual machine instruction.
    *
    * @return always returns true
    */
  def true_0: Boolean = true

  /**
    * Terminate Prolog execution. Shutdown the JVM with System.exit(0)
    *
    * @return true, if it still exists at this point.
    */
  def halt_0: Boolean = {
    System.exit(0)
    true
  }

  /**
    * Terminate Prolog execution with a status.
    *
    * @param arg0 the status to exit with.
    * @throws PrologException if the status term is invalid.
    * @return true, if it still exists at this point.
    */
  @throws[PrologException]
  def halt_1(arg0: Term): Boolean = {
    arg0 match {
      case i: Int => System.exit(i.intValue)
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case _ => throw PrologException.type_error(engineManager, 1, "integer", arg0)
    }
    true
  }

  /**
    * Stops the enginge from backtracking.
    *
    * @return always returns true.
    */
  def cut_0: Boolean = {
    engineManager.cut()
    true
  }

  /**
    * Asserts a fact or clause in the database. Term is asserted as the first fact or clause of the corresponding predicate. Equivalent to assert/1, but Term
    * is asserted as first clause or fact of the predicate.
    *
    * @param arg0 the clause/ fact to insert.
    * @throws PrologException if it is not an asssertable clause.
    * @return true if successful, throws an exception otherwise
    */
  @throws[PrologException]
  def asserta_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case assertion: Struct =>
        if (assertion.getName == ":-") {
          for (argi <- assertion.toListStruct) {
            argi match {
              case s: Struct =>
              case v: Var => throw PrologException.instantiation_error(engineManager, 1)
              case _ => throw PrologException.type_error(engineManager, 1, "clause", arg0)
            }
          }
        }
        theoryManager.assertA(assertion, true, null)
        true
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case default => throw PrologException.type_error(engineManager, 1, "clause", default)
    }
  }


  /**
    * Asserts a fact or clause in the database. Term is asserted as the last fact or clause of the corresponding predicate. Equivalent to assert/1, but Term
    * is asserted as first clause or fact of the predicate.
    *
    * @param arg0 the clause/ fact to insert.
    * @throws PrologException if it is not an asssertable clause.
    * @return true if successful, throws an exception otherwise
    */
  @throws[PrologException]
  def assertz_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case assertion: Struct =>
        if (assertion.getName == ":-") {
          for (argi <- assertion.toListStruct) {
            argi match {
              case s: Struct =>
              case v: Var => throw PrologException.instantiation_error(engineManager, 1)
              case default => throw PrologException.type_error(engineManager, 1, "clause", assertion)
            }
          }
        }
        theoryManager.assertZ(assertion, true, null)
        true
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case default => throw PrologException.type_error(engineManager, 1, "clause", default)
    }
  }

  /**
    * When the supplied term is an atom or a term it is unified with the first unifying fact or clause in the database. The fact or clause is then removed from
    * the database.
    *
    * @param arg0 the term to remove from the database.
    * @throws PrologException if the provided clause is invalid.
    * @return true if a clause is found to retract, false if otherwise.
    */
  @throws[PrologException]
  def $retract_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case sarg0: Struct =>
        val c: ClauseInfo = theoryManager.retract(sarg0) // if clause to retract found -> retract + true
        if (c != null) {
          var clause: Struct = null
          if (!sarg0.isClause) {
            clause = new Struct(":-", arg0, new Struct("true"))
          } else {
            clause = sarg0
          }
          unify(clause, c.getClause)
          true
        } else {
          false
        }
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case default => throw PrologException.type_error(engineManager, 1, "clause", default)
    }
  }

  /**
    * Removes all clauses of a predicate with functor Functor and arity Arity from the database.
    *
    * @param arg0 a member of the family of predicates to remove.
    * @throws PrologException if the provided clause is invalid.
    * @return true if a clause is found to retract, false if otherwise.
    */
  @throws[PrologException]
  def abolish_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)

      case abArg: Struct if !arg0.isGround => throw PrologException.type_error(engineManager, 1, "predicate_indicator", abArg)

      case abArg: Struct if abArg.getArg(0).toString == "abolish" =>
        throw PrologException.permission_error(engineManager, "modify", "static_procedure", abArg, new Struct(""))

      case abArg: Struct => theoryManager.abolish(abArg)

      case t: Term => throw PrologException.type_error(engineManager, 1, "predicate_indicator", t)
    }
  }


  /**
    * loads a prolog library, given its class name.
    *
    * @param arg0 the name of library in term form.
    * @throws PrologException if the term is invalid
    * @return true if the library has been loaded successfully.
    */
  @throws[PrologException]
  def load_library_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case atom: Term if atom.isAtom =>
        try {
          libraryManager.loadLibrary(atom.asInstanceOf[Struct].getName)
          true
        } catch {
          case NonFatal(ex) => throw PrologException.existence_error(engineManager, 1, "class", atom, new Struct(ex.getMessage))
        }
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case t: Term => throw PrologException.type_error(engineManager, 1, "atom", t)
    }
  }


  /**
    * loads a prolog library, given its class name. Directive version.
    *
    * @param arg0 the name of library in term form.
    * @throws PrologException if the term is invalid
    */
  @throws[InvalidLibraryException]
  def $load_library_1(arg0: Term): Unit = {
    val lib = arg0.getTerm
    if (lib.isAtom) libraryManager.loadLibrary(lib.asInstanceOf[Struct].getName)
  }

  /**
    * unloads a prolog library, given its class name.
    *
    * @param arg0 the name of library in term form.
    * @throws PrologException if the term is invalid
    * @return true if the library has been unloaded successfully.
    */
  @throws[PrologException]
  def unload_library_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case atom: Term if atom.isAtom =>
        try {
          libraryManager.unloadLibrary(atom.asInstanceOf[Struct].getName)
          true
        } catch {
          case NonFatal(ex) => throw PrologException.existence_error(engineManager, 1, "class", atom, new Struct(ex.getMessage))
        }
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case t: Term => throw PrologException.type_error(engineManager, 1, "atom", t)
    }
  }

  /**
    * get flag list: flag_list(-List)
    *
    * @param arg0
    * @return
    */
  def flag_list_1(arg0: Term): Boolean = unify(arg0.getTerm, flagManager.getPrologFlagList)

  def comma_2(arg0: Term, arg1: Term): Boolean = {
    val s: Struct = new Struct(",", arg0.getTerm, arg1.getTerm)
    engineManager.pushSubGoal(ClauseInfo.extractBody(s))
    true
  }

  /**
    * It is the same as call/1, but it is not opaque to cut.
    *
    * @throws PrologException
    */
  @throws[PrologException]
  def $call_1(arg0: Term): Boolean = {
    arg0.getTerm match {
      case null => throw PrologException.type_error(engineManager, 1, "callable", arg0)
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case t: Term if !isCallable(t) => throw PrologException.type_error(engineManager, 1, "callable", t)
      case goal: Term =>
        val termToGoal = BuiltIn.convertTermToGoal(goal)
        engineManager.identify(termToGoal)
        engineManager.pushSubGoal(ClauseInfo.extractBody(termToGoal))
        true
    }
  }

  @throws[PrologException]
  def is_2(arg0: Term, arg1: Term): Boolean = {
    if (arg1.getTerm.isInstanceOf[Var]) throw PrologException.instantiation_error(engineManager, 2)
    var val1: Term = null
    try {
      val1 = evalExpression(arg1)
    }
    catch {
      case t: Throwable => {
        handleError(t)
      }
    }
    if (val1 == null) throw PrologException.type_error(engineManager, 2, "evaluable", arg1.getTerm)
    else return unify(arg0.getTerm, val1)
  }

  def unify_2(arg0: Term, arg1: Term): Boolean = unify(arg0, arg1)

  // \=
  def deunify_2(arg0: Term, arg1: Term): Boolean = !unify(arg0, arg1)

  // $tolist
  @throws[PrologException]
  def $tolist_2(arg0: Term, arg1: Term): Boolean = {
    // transform arg0 to a list, unify it with arg1
    arg0.getTerm match {
      case v: Var => throw PrologException.instantiation_error(engineManager, 1)
      case s: Struct =>
        val val0: Term = s.toListStruct
        val0 != null && unify(arg1.getTerm, val0)
      case default => throw PrologException.type_error(engineManager, 1, "struct", default)
    }
  }

  // $fromlist
  @throws[PrologException]
  def $fromlist_2(arg0: Term, arg1: Term): Boolean = {
    // get the compound representation of the list provided as arg1, and unify it with arg0
    arg1.getTerm match {
      case v: Var => throw PrologException.instantiation_error(engineManager, 2)
      case t: Term if !t.isList => throw PrologException.type_error(engineManager, 2, "list", t)
      case s: Struct =>
        val val1: Term = s.fromList
        if (val1 == null) {
          false
        } else {
          unify(arg0.getTerm, val1)
        }
    }
  }

  def copy_term_2(arg0: Term, arg1: Term): Boolean = {
    // unify arg1 with a renamed copy of arg0
    val id = engineManager.getEnv.nDemoSteps
    unify(arg1.getTerm, arg0.getTerm.copy(new util.IdentityHashMap[Var, Var](), id))
  }

  // $append
  @throws[PrologException]
  def $append_2(arg0: Term, arg1: Term): Boolean = {
    // append arg0 to arg1
    arg1.getTerm match {
      case v: Var => throw PrologException.instantiation_error(engineManager, 2)
      case t: Term if !t.isList => throw PrologException.type_error(engineManager, 2, "list", t)
      case s: Struct =>
        s.append(arg0.getTerm)
        true
    }
  }

  // $find
  @throws[PrologException]
  def $find_2(arg0: Term, arg1: Term): Boolean = {
    // look for clauses whose head unifies with arg0 and enqueue them to list arg1
    (arg0.getTerm, arg1.getTerm) match {
      case(a : Var, _) => throw PrologException.instantiation_error(engineManager, 1)
      case(_, b : Term)  if !b.isList => throw PrologException.type_error(engineManager, 2, "list", b)
      case (a,b: Struct) =>
        theoryManager.find(a).foreach{ c =>
          if (`match`(a, c.getHead)){
            c.getClause.resolveTerm()
            b.append(c.getClause)
          }
        }
        true
    }
  }

  // set_prolog_flag(+Name,@Value)
  @throws[PrologException]
  def set_prolog_flag_2(arg0: Term, arg1: Term): Boolean = {
    (arg0.getTerm, arg1.getTerm) match {
      case (a: Var, _) => throw PrologException.instantiation_error(engineManager, 1)
      case (_, b: Var) => throw PrologException.instantiation_error(engineManager, 2)
      case (a: Term, _) if !a.isAtom && !a.isInstanceOf[Struct] => throw PrologException.type_error (engineManager, 1, "struct", a)
      case (_, b: Term) if !b.isGround => throw PrologException.type_error(engineManager, 2, "ground", b)
      case (a, b) =>
        val name: String = a.toString
        if (flagManager.getFlag(name) == null) {
          throw PrologException.domain_error(engineManager, 1, "prolog_flag", a)
        }
        if (!flagManager.isValidValue(name, b)) {
          throw PrologException.domain_error(engineManager, 2, "flag_value", b)
        }
        if (!flagManager.isModifiable(name)) {
          throw PrologException.permission_error(engineManager, "modify", "flag", a, new Int(0))
        }
        flagManager.setFlag(name, b)
    }
  }

  // get_prolog_flag(@Name,?Value)
  @throws[PrologException]
  def get_prolog_flag_2(arg0: Term, arg1: Term): Boolean = {
    arg0.getTerm match {
      case v : Var => throw PrologException.instantiation_error (engineManager, 1)
      case t : Term if !t.isAtom && !t.isInstanceOf[Struct] => throw PrologException.type_error (engineManager, 1, "struct", t)
      case s : Struct =>
        val name: String = s.toString()
        val value: Term = flagManager.getFlag (name)
        if (value == null) {
          throw PrologException.domain_error (engineManager, 1, "prolog_flag", s)
        }
        unify (value, arg1.getTerm)
    }
  }

  @throws[PrologException]
  def $op_3(arg0: Term, arg1: Term, arg2: Term): Boolean = {
    (arg0.getTerm, arg1.getTerm, arg2.getTerm) match {
      case (a: Var, _, _) => throw PrologException.instantiation_error(engineManager, 1)
      case (_, b: Var, _) => throw PrologException.instantiation_error(engineManager, 2)
      case (_, _, c: Var) => throw PrologException.instantiation_error(engineManager, 3)
      case (a, _, _) if !a.isInstanceOf[Int] => throw PrologException.type_error(engineManager, 1, "integer", a)
      case (_, b, _) if !b.isAtom => throw PrologException.type_error(engineManager, 2, "atom", b)
      case (_, _, c) if !c.isAtom && !c.isList => throw PrologException.type_error(engineManager, 3, "atom_or_atom_list", c)
      case (a: Int, b: Struct, c: Struct) =>
        val priority = a.intValue
        if (priority < OperatorManager.OP_LOW || priority > OperatorManager.OP_HIGH) {
          throw PrologException.domain_error(engineManager, 1, "operator_priority", a)
        }

        val specifier: String = b.getName
        if (!(specifier == "fx") && !(specifier == "fy") && !(specifier == "xf") && !(specifier == "yf") && !(specifier == "xfx") && !(specifier == "yfx") &&
          !(specifier == "xfy")) {
          throw PrologException.domain_error(engineManager, 2, "operator_specifier", b)
        }

        if (c.isList) {
          for (operator <- c) {
            operatorManager.opNew(operator.asInstanceOf[Struct].getName, specifier, priority)
          }
        } else {
          operatorManager.opNew(c.getName, specifier, priority)
        }
        true
    }
  }

  @throws[PrologException]
  def op_3(arg0: Term, arg1: Term, arg2: Term): Unit = $op_3(arg0, arg1, arg2)

  def flag_4(arg0: Term, arg1: Term, arg2: Term, arg3: Term) {
    val flagName = arg0.getTerm
    val flagSet = arg1.getTerm
    val flagDefault = arg2.getTerm
    val flagModifiable = arg3.getTerm
    if (flagSet.isList && (flagModifiable == Term.TRUE || flagModifiable == Term.FALSE)) {
      val libName: String = ""
      flagManager.defineFlag(flagName.toString, flagSet.asInstanceOf[Struct], flagDefault, flagModifiable == Term.TRUE, libName)
    }
  }

  def initialization_1(goal: Term) {
    val goalTerm = goal.getTerm
    if (goalTerm.isInstanceOf[Struct]) {
      primitiveManager.identifyPredicate(goalTerm)
      theoryManager.addStartGoal(goalTerm.asInstanceOf[Struct])
    }
  }

  @throws[FileNotFoundException]
  @throws[InvalidTheoryException]
  @throws[IOException]
  def include_1(arg0: Term) {
    val theory = arg0.getTerm
    var path: String = Tools.removeApices(theory.toString)
    if (!new File(path).isAbsolute) {
      path = engine.getCurrentDirectory + File.separator + path
    }
    engine.pushDirectoryToList(new File(path).getParent)
    engine.addTheory(new Theory(new FileInputStream(path)))
    engine.popDirectoryFromList
  }
}