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
import com.szadowsz.gospel.core.db.libraries.{Library, predicate}
import com.szadowsz.gospel.core.exception.InterpreterError

/**
  * Flags marked rw can be modified by the user using set_prolog_flag/2. Flag values are typed. Flags marked as bool
  * can have the values true or false. The predicate create_prolog_flag/3 may be used to create flags that describe or
  * control behaviour of libraries and applications. The library library(settings) provides an alternative interface f
  * or managing notably application parameters.
  *
  * Some Prolog flags are not defined in all versions, which is normally indicated in the documentation below as if
  * present and true. A boolean Prolog flag is true iff the Prolog flag is present and the Value is the atom true.
  */
trait BuiltInFlags {
  this: Library =>
  // scalastyle:off method.name
  
  // get_prolog_flag(@Name,?Value)
  
  
  @predicate(1)
  def prolog_flag_list_1: (Term) => Boolean = { t : Term => t.getBinding.unify(flagManager.getPrologFlagList)}
    
    
    
  def getFlagTheoryString: String = {
    """
      |
      |% The predicate current_prolog_flag/2 defines an interface to installation features: options compiled in, 
      |% version, home, etc. With both arguments unbound, it will generate all defined Prolog flags. With Key 
      |% instantiated, it unifies Value with the value of the Prolog flag or fails if the Key is not a Prolog flag.
      |current_prolog_flag(Name,Value) :- prolog_flag_list(L), member(flag(Name,Value),L).
      |
    """.stripMargin
  }
}
