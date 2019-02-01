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
package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.db.libraries.{Library, LibraryManager}
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.db.primitives.PrimitivesManager
import com.szadowsz.gospel.core.db.theory.TheoryManager

class Interpreter {

  protected lazy val opManager : OperatorManager = new OperatorManager

  protected lazy val primManager : PrimitivesManager = new PrimitivesManager(this)

  protected lazy val libManager : LibraryManager = new LibraryManager(this)

  protected lazy val thManager : TheoryManager = new TheoryManager(this)

  private[core] def getPrimitiveManager : PrimitivesManager = primManager

  private[core] def getTheoryManager : TheoryManager = thManager

  private[core] def getLibraryManager : LibraryManager = libManager

  private[core] def getOperatorManager : OperatorManager = opManager

  def this(libs : Class[_ <: Library]*){
    this()
    libs.foreach(l => libManager.loadLibraryFromClass(l))
  }
}
