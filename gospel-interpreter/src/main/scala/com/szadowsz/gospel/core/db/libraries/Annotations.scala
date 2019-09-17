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
package com.szadowsz.gospel.core.db.libraries

import scala.annotation.StaticAnnotation
// scalastyle:off class.name

case class clause(args: Int, useAliasesOnly: Boolean, aliases : String*) extends StaticAnnotation

class directive(args: Int, useAliasesOnly : Boolean, aliases : String*) extends clause(args, useAliasesOnly, aliases:_*){
  
  def this(args: Int, aliases : String*){
    this(args,false,aliases:_*)
  }
}

class functor(args: Int, useAliasesOnly : Boolean, aliases : String*) extends clause(args, useAliasesOnly, aliases:_*){
  
  def this(args: Int, aliases : String*){
    this(args,false,aliases:_*)
  }
}

class predicate(args: Int, useAliasesOnly : Boolean, aliases : String*) extends clause(args, useAliasesOnly, aliases:_*){
  
  def this(args: Int, aliases : String*){
    this(args,false,aliases:_*)
  }
}
