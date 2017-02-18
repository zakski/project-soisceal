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

import alice.tuprolog.{InvalidLibraryException, Library, Term}
import alice.tuprolog.interfaces.{IFlagManager, ILibraryManager, IPrimitiveManager}
import alice.tuprolog.lib.{BasicLibrary, IOLibrary, ISOLibrary, OOLibrary}
import com.szadowsz.gospel.core.db.LibManager
import com.szadowsz.gospel.core.db.primitives.PrimitiveManager
import com.szadowsz.gospel.core.engine.flags.FlagManager

/**
  * This class represents a tuProlog engine.
  *
  * Created on 15/02/2017.
  *
  * @version Gospel 2.0.0
  */
class PrologEngine protected(spy: Boolean, warning: Boolean) extends alice.tuprolog.Prolog(spy,warning) {

  private lazy val libManager = new LibManager(this) // manager of loaded libraries

  private lazy val primManager = new PrimitiveManager(this)  // primitive prolog term manager.

  private lazy val flagManager = new FlagManager() // engine flag manager.

  /**
    * Builds a Prolog engine with loaded the specified libraries.
    *
    * @param libs the (class) names of the libraries to be loaded
    */
  @throws(classOf[InvalidLibraryException])
  def this(libs: Array[String]) {
    this(false, true)
    libs.foreach(s => loadLibrary(s))
  }
  /**
    * Builds a Prolog engine with loaded the specified libraries.
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

  override protected def getLibraryPredicate(name: String, nArgs: Int): Library = primManager.getLibraryPredicate(name, nArgs) // TODO comment or remove

  override protected def getLibraryFunctor(name: String, nArgs: Int): Library = primManager.getLibraryFunctor(name, nArgs) // TODO comment or remove

    /**
    * Method to retrieve the engine component managing flags.
    *
    * @return the flag manager instance attached to this prolog engine.
    */
  override def getFlagManager: IFlagManager = flagManager // TODO Make Internal only

  /**
    * Method to retrieve the db component that manages libraries.
    *
    * @return the library manager instance attached to this prolog engine.
    */
  override def getLibraryManager: ILibraryManager = libManager  // TODO Make Internal only

  /**
    * Method to retrieve the db component that manages primitives.
    *
    * @return the primitive manager instance attached to this prolog engine.
    */
  override def getPrimitiveManager: IPrimitiveManager = primManager  // TODO Make Internal only

  /**
    * Gets the list of current libraries loaded
    *
    * @return the list of the library names
    */
  override def getCurrentLibraries: Array[String] = libManager.getCurrentLibraries

  /**
    * Gets the reference to a loaded library
    *
    * @param name the name of the library already loaded
    * @return the reference to the library loaded, null if the library is not found
    */
  override def getLibrary(name: String): Library = libManager.getLibrary(name)

  /**
    * Identify any functors.
    *
    * @param term the term to identify.
    */
  override def identifyFunctor(term: Term): Unit = primManager.identifyFunctor(term)

  /**
    * Unloads a previously loaded library
    *
    * @param name of the library to be unloaded
    * @throws InvalidLibraryException if name is not a valid loaded library
    */
  @throws(classOf[InvalidLibraryException])
  override def unloadLibrary(name: String) : Unit = libManager.unloadLibrary(name)

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param className the name of the Java class containing the library to be loaded.
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[InvalidLibraryException])
  override def loadLibrary(className: String): Library = libManager.loadLibrary(className)

  /**
    * Loads a library.
    *
    * If a library with the same name is already present,
    * a warning event is notified and the request is ignored.
    *
    * @param className name of the Java class containing the library to be loaded
    * @param paths     The path where is contained the library.
    * @return the reference to the Library just loaded
    * @throws InvalidLibraryException if name is not a valid library
    */
  @throws[InvalidLibraryException]
  override def loadLibrary(className: String, paths: Array[String]): Library = libManager.loadLibrary(className, paths)

  /**
    * Loads a specific instance of a library
    *
    * If a library with the same name is already present, a warning event is notified
    *
    * @param lib the (Java class) name of the library to be loaded
    * @throws InvalidLibraryException if name is not a valid library
    */
  @throws(classOf[InvalidLibraryException])
  override def loadLibrary(lib: Library): Unit = libManager.loadLibrary(lib)

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param libClass the library class to be loaded.
    * @tparam L parameter to allow for any subclass of library to be used.
    * @throws InvalidLibraryException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary[L <: Library](libClass: Class[L]): Library = libManager.loadLibrary(libClass)



}