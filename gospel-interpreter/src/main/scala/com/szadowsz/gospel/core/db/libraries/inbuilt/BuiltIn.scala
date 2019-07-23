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
package com.szadowsz.gospel.core.db.libraries.inbuilt

import com.szadowsz.gospel.core.{Interpreter, data}
import com.szadowsz.gospel.core.data.{Number, Struct, Term, Var}
import com.szadowsz.gospel.core.db.libraries.{Library, directive, predicate}
import com.szadowsz.gospel.core.db.theory.Theory
import com.szadowsz.gospel.core.db.theory.clause.Clause
import com.szadowsz.gospel.core.exception.InterpreterError
import com.szadowsz.gospel.core.exception.library.InvalidLibraryException

class BuiltIn(wam: Interpreter) extends Library(wam) with BuiltInArithmetic {
  // scalastyle:off method.name
  
  private val libStructName: String = "library"
  
  /**
    * A callable term is an atom of a compound term. See the ISO Standard definition in section 3.24.
    */
  private def isCallable(goal: Term): Boolean = goal.isAtom || goal.isCompound
  
  /**
    * Convert a term to a goal before executing it by means of call/1. See section 7.6.2 of the ISO Standard for
    * details.
    *
    * If T is a variable then G is the control construct call, whose argument is T.
    *
    * If the principal functor of T is t ,?/2 or ;/2 or ->/2, then each argument of T shall also be converted to a goal.
    *
    * If T is an atom or compound term with principal functor FT, then G is a predication whose predicate indicator is
    * FT, and the arguments, if any, of T and G are identical
    *
    * Note that a variable X and a term call(X) are converted to identical bodies. Also note that if T is a number, then
    * there is no goal which corresponds to T.
    */
  private def convertTermToGoal(term: Term): Term = {
    term.getBinding match {
      case n: Number => null
      case v: Var => new Struct("call", term)
      case s: Struct =>
        val pi: String = s.getPredicateIndicator
        if (pi == ";/2" || pi == ",/2" || pi == "->/2") {
          for (i <- 0 until s.getArity) {
            val t: Term = s(i)
            val arg: Term = convertTermToGoal(t)
            if (arg == null) return null
            s(i) = arg
          }
        }
        s
      case t => t
    }
  }
  
  override def getTheory: Option[Theory] = {Some(new Theory(
    """
      |% call/1 is coded both in Prolog, to feature the desired opacity to cut, and in Java as a primitive built-in, to
      |% account for goal transformations that should be performed before execution as mandated by ISO Standard, see 
      |% section 7.8.3.1
      |call(G) :- call_guard(G), '$call'(G).
      |
      |catch(Goal, Catcher, Handler) :- call(Goal).
    """.stripMargin
  ))}
  
  
  /**
    * Method to load Prolog Libraries/Modules.
    *
    * Load the file(s) specified with Files just like ensure_loaded/1. The files must all be module files. All exported
    * predicates from the loaded files are imported into the module from which this predicate is called. This predicate
    * is equivalent to ensure_loaded/1, except that it raises an error if Files are not module files.
    *
    * The imported predicates act as weak symbols in the module into which they are imported. This implies that a local
    * definition of a predicate overrides (clobbers) the imported definition. If the flag warn_override_implicit_import
    * is true (default), a warning is printed.
    */
  @directive(1)
  @throws(classOf[InvalidLibraryException])
  def use_module_1: Term => Unit = {
    case lib: Struct if lib.getName == libStructName =>
      libManager.loadLibrary(lib.getTerm(0).asInstanceOf[Struct].getName)
    
    case libs: Struct if libs.isList =>
      if (libs.getListIterator.forall(lib => lib.isInstanceOf[Struct] && lib.asInstanceOf[Struct].getName == libStructName)) {
        libs.getListIterator.foreach(lib => libManager.loadLibrary(
          lib.asInstanceOf[Struct].getTerm(0).asInstanceOf[Struct].getName)
        )
      }
    
    case _ =>
  }
  
  /**
    * Method to load Prolog Libraries/Modules.
    *
    * Load the file which must be a module file, and import the predicates as specified by ImportList. This predicate
    * is equivalent to ensure_loaded/1, except that it raises an error if Files are not module files.
    *
    * The imported predicates act as weak symbols in the module into which they are imported. This implies that a local
    * definition of a predicate overrides (clobbers) the imported definition. If the flag warn_override_implicit_import
    * is true (default), a warning is printed.
    *
    * Load File, which must be a module file, and import the predicates as specified by ImportList. ImportList is a list
    * of predicate indicators specifying the predicates that will be imported from the loaded module. ImportList also
    * allows for renaming or import-everything-except. See also the import option of load_files/2. The first example
    * below loads member/2 from the lists library and append/2 under the name list_concat, which is how this predicate
    * is named in YAP. The second example loads all exports from library option except for meta_options/3. These
    * renaming facilities are generally used to deal with portability issues with as few changes as possible to the
    * actual code. See also section C and section 6.7.
    */
  @directive(2)
  @throws(classOf[InvalidLibraryException])
  def use_module_2: (Term, Term) => Unit = {
    case (file: Struct, importList: Struct) if file.getName == libStructName =>
      libManager.loadLibrary(file.getTerm(0).asInstanceOf[Struct].getName, importList)
    
    case (libs: Struct, importList: Struct) if libs.isList =>
      if (libs.getListIterator.forall(lib => lib.isInstanceOf[Struct] && lib.asInstanceOf[Struct].getName == libStructName)) {
        libs.getListIterator.foreach(lib => libManager.loadLibrary(
          lib.asInstanceOf[Struct].getTerm(0).asInstanceOf[Struct].getName,
          importList
        )
        )
      }
    
    case _ =>
  }
  
  
  @throws[InterpreterError]
  @predicate(1)
  def call_guard_1: Term => Boolean = {
    goal: Term =>
      val e = goal.getExecutor
      goal.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1)
        case uncallable : Term if !isCallable(uncallable) => throw InterpreterError.buildTypeError(e, 1, "callable", uncallable)
        case _ => true
      }
  }
  
  /**
    * Invoke Goal as a goal. The same as call/1, but it is not opaque to cut.
    */
  @predicate(1)
  @throws(classOf[InterpreterError])
  def $call_1 : Term => Boolean = {
    goal: Term =>
      val e = goal.getExecutor
      goal.getBinding match {
        case v: Var => throw InterpreterError.buildInstantiationError(e, 1)
        case uncallable : Term if !isCallable(uncallable) => throw InterpreterError.buildTypeError(e, 1, "callable", uncallable)
        case binding : Term =>
          val termToGoal = convertTermToGoal(goal)
          e.identifyPredicate(termToGoal)
          e.pushSubGoal(Clause.extractBody(termToGoal))
          true
      }
  }

}
