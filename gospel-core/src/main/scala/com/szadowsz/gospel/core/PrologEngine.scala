/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.core

import alice.tuprolog.{InvalidLibraryException, Library}
import alice.tuprolog.interfaces.IFlagManager
import alice.tuprolog.lib.{BasicLibrary, IOLibrary, ISOLibrary, OOLibrary}
import com.szadowsz.gospel.core.engine.flags.FlagManager

/**
  * This class represents a tuProlog engine.
  *
  * Created on 15/02/2017.
  *
  * @version Gospel 2.0.0
  */
class PrologEngine protected(spy: Boolean, warning: Boolean) extends alice.tuprolog.Prolog(spy,warning) {

  private lazy val flagManager = new FlagManager() // engine flag manager

  /**
    * Builds a tuProlog engine with loaded the specified libraries.
    *
    * @param libs the (class) names of the libraries to be loaded
    */
  @throws(classOf[InvalidLibraryException])
  def this(libs: Array[String]) {
    this(false, true)
    libs.foreach(s => loadLibrary(s))
  }
  /**
    * Builds a tuProlog engine with loaded the specified libraries.
    *
    * @param libs the classes of the libraries to be loaded
    */
  def this(libs: Array[Class[_ <: Library]]) {
    this(false, true)
    libs.foreach(c => loadLibrary(c.getName))
  }

  /**
    * Builds a Prolog engine with default libraries loaded.
    *
    * The default libraries are BasicLibrary, ISOLibrary, IOLibrary, and OOLibrary.
    */
   def this() {
    this(Array[Class[_ <: Library]](
      classOf[BasicLibrary],
      classOf[ISOLibrary],
      classOf[IOLibrary],
      classOf[OOLibrary]
    ))
  }

  /**
    * Method to retrieve the component managing flags.
    *
    * @return the flag manager instance attached to this prolog engine.
    */
  override def getFlagManager: IFlagManager = flagManager // TODO Make Internal only

}