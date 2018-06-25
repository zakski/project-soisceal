/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  * <p>
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * <p>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * <p>
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
package com.szadowsz.gospel.core.db.libs.builtin

import java.io.{File, FileInputStream, FileNotFoundException, IOException}

import alice.util.Tools
import com.szadowsz.gospel.core.Theory
import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.db.JLibrary
import com.szadowsz.gospel.core.db.primitives.PrimitiveManager
import com.szadowsz.gospel.core.db.theory.TheoryManager
import com.szadowsz.gospel.core.exception.InvalidTheoryException

/**
  * Directives, Functors and Predicates to do with loading Prolog Source Files.
  *
  * A Prolog source file is a plain text file containing a Prolog program or part thereof. Prolog source files come in
  * three flavours:
  *
  * 1. traditional
  * Prolog source file contains Prolog clauses and directives, but no module declaration (see module/1). They are
  * normally loaded using consult/1 or ensure_loaded/1. Currently, a non-module file can only be loaded into a single
  * module.
  *
  * 2. module
  * Prolog source file starts with a module declaration. The subsequent Prolog code is loaded into the specified
  * module, and only the exported predicates are made available to the context loading the module. Module files are
  * normally loaded with use_module/[1,2]. See chapter 6 for details.
  *
  * 3. include
  * Prolog source file is loaded using the include/1 directive, textually including Prolog text into another Prolog
  * source. A file may be included into multiple source files and is typically used to share declarations such as
  * multifile or dynamic between source files.
  */
private[builtin] trait LoadSource {
  // scalastyle:off method.name
  this : JLibrary =>

  protected def primitiveManager : PrimitiveManager

  protected def theoryManager : TheoryManager

  // def at_halt TODO http://www.swi-prolog.org/pldoc/doc_for?object=at_halt/1

  // def cancel_halt/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=cancel_halt/1

  // def compiling TODO http://www.swi-prolog.org/pldoc/doc_for?object=compiling/0

  // def consult/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=consult/1

  // def encoding/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=encoding/1

  // def ensure_load/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=ensure_loaded/1

  // def expand_file_search_path/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=expand_file_search_path/2

  // def file_search_path/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=file_search_path/2

  /**
    * Jvm hook for include/1 predicate.
    *
    * Textually include the content of File at the position where the directive :- include(File). appears. The include construct is only honoured if it appears
    * as a directive in a source file. Textual include (similar to C/C++ #include) is obviously useful for sharing declarations such as dynamic/1 or
    * multifile/1 by including a file with directives from multiple files that use these predicates.
    *
    * @see <a href="http://www.swi-prolog.org/pldoc/doc_for?object=include/1"/>
    *
    * @param file the file to be included
    * @throws FileNotFoundException if the file does not exist
    * @throws InvalidTheoryException if the textual content is not valid prolog.
    * @throws IOException if an error occurs mid file load
    */
  @throws[FileNotFoundException]
  @throws[InvalidTheoryException]
  @throws[IOException]
  def include_1(file: Term): Unit = {
    var path: String = Tools.removeApices(file.getTerm.toString)
    if (!new File(path).isAbsolute) {
      path = engine.getCurrentDirectory + File.separator + path
    }
    engine.pushDirectoryToList(new File(path).getParent)
    engine.addTheory(new Theory(new FileInputStream(path)))
    engine.popDirectoryFromList()
  }

  // def initialization/0 TODO http://www.swi-prolog.org/pldoc/doc_for?object=(initialization)/0

  /**
    * Jvm hook for initialisation/1 predicate.
    *
    * Call Goal after loading the source file in which this directive appears has been completed. The ISO standard only allows for using :- Term if Term is a
    * directive. This means that arbitrary goals can only be called from a directive by means of the initialization/1 directive.
    *
    * @note SWI-Prolog does not enforce that directive rule.
    * @param goal the goal to initialise the theory with.
    */
  def initialization_1(goal: Term) {
    goal.getTerm match {
      case goalTerm: Struct =>
        primitiveManager.identifyPredicate(goalTerm)
        theoryManager.addStartGoal(goalTerm)
      case _ =>
    }
  }

  // def initialization/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=(initialization)/2

  // def load_files/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=load_files/1

  // def load_files/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=load_files/2

  // def make/0 TODO http://www.swi-prolog.org/pldoc/doc_for?object=make/0

  // def prolog_file_type/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=prolog_file_type/2

  // def prolog_load_context/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=prolog_load_context/2

  // def require/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=require/1

  // def source_file/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_file/1

  // def source_file/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_file/2

  // def source_file_property/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_file_property/2

  // def source_location/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_location/2

  // def unload_file/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=unload_file/1
}
