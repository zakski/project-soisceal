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

import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.libraries.{Library, directive, predicate}
import com.szadowsz.gospel.core.exception.InterpreterError
import com.szadowsz.gospel.core.exception.library.InvalidLibraryException

trait BuiltInSrcControl {
  this: Library =>
  // scalastyle:off method.name
  
  private val libStructName: String = "library"
  
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
  
  /**
    * Removes all clauses of a predicate with functor Functor and arity Arity from the database.
    *
    * @throws InterpreterError if the provided clause is invalid.
    * @return true if a clause is found to retract, false if otherwise.
    */
  @throws[InterpreterError]
  @predicate(1)
  def abolish_1: Term => Boolean = (arg0: Term) => {
    val e = arg0.getExecutor
    arg0.getBinding match {
      case v: Var => throw InterpreterError.buildInstantiationError(e, 1)
      
      case abArg: Struct if !arg0.isGround => throw InterpreterError.buildTypeError(e, 1, "predicate_indicator", abArg)
      
      case abArg: Struct if abArg(0).toString == "abolish" =>
        throw InterpreterError.buildPermissionError(e, "modify", "static_procedure", abArg, new Struct(""))
      
      case abArg: Struct => thManager.abolish(abArg)
      
      case t: Term => throw InterpreterError.buildTypeError(e, 1, "predicate_indicator", t)
    }
  }
  
  /**
    * Asserts a fact or clause in the database. Term is asserted as the last fact or clause of the corresponding predicate. Equivalent to assert/1, but Term
    * is asserted as first clause or fact of the predicate.
    *
    * @throws InterpreterError if it is not an asssertable clause.
    * @return true if successful, throws an exception otherwise
    */
  @throws[InterpreterError]
  @predicate(1)
  def assertz_1: Term => Boolean = (arg0: Term) => {
    val e = arg0.getExecutor
    arg0.getBinding match {
      case assertion: Struct =>
        if (assertion.getName == ":-") {
          for (argi <- assertion.toStructList.getListIterator) {
            argi match {
              case s: Struct =>
              case v: Var => throw InterpreterError.buildInstantiationError(e, 1)
              case _ => throw InterpreterError.buildTypeError(e, 1, "clause", assertion)
            }
          }
        }
        thManager.assertZ(assertion, true, None, false)
        true
      case v: Var => throw InterpreterError.buildInstantiationError(e, 1)
      case default : Term => throw InterpreterError.buildTypeError(e, 1, "clause", default)
    }
  }
}
