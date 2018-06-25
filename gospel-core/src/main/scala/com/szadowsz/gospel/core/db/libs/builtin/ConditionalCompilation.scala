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

import com.szadowsz.gospel.core.db.JLibrary

/**
  * Non-iso prolog
  *
  * SO Prolog defines no way for program transformations such as macro expansion or conditional compilation. Expansion
  * through term_expansion/2 and expand_term/2 can be seen as part of the de-facto standard. This mechanism can do
  * arbitrary translation between valid Prolog terms read from the source file to Prolog terms handed to the compiler.
  * As term_expansion/2 can return a list, the transformation does not need to be term-to-term.
  *
  * Various Prolog dialects provide the analogous goal_expansion/2 and expand_goal/2 that allow for translation of
  * individual body terms, freeing the user of the task to disassemble each clause.
  */
trait ConditionalCompilation {
  // scalastyle:off method.name
  this : JLibrary =>

  // def compile_aux_clauses/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=compile_aux_clauses/1

  // def dcg_translate_rule/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=dcg_translate_rule/2

  // def elif/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=elif/1

  // def else/0 TODO http://www.swi-prolog.org/pldoc/doc_for?object=else/0

  // def endif/0 TODO http://www.swi-prolog.org/pldoc/doc_for?object=endif/0

  // def expand_goal/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=expand_goal/2

  // def expand_term/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=expand_term/2

  // def goal_expansion/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=goal_expansion/2

  // def if/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=if/1

  // def term_expansion/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=term_expansion/2

  // def var_property/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=var_property/2
}
