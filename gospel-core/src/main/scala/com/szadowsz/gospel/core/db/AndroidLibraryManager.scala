/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  *
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
package com.szadowsz.gospel.core.db

import java.io.File

import com.szadowsz.gospel.core.PrologEngine
import com.szadowsz.gospel.core.event.interpreter.LibraryEvent
import com.szadowsz.gospel.core.exception.InvalidLibraryException

import scala.util.control.NonFatal

final case class AndroidLibraryManager(override protected val wam: PrologEngine) extends LibraryManager {

  /*
   * This is the directory where optimized dex files should be written. Is required to the DexClassLoader.
   */
  private var optimizedDirectory: String = _

  def setOptimizedDirectory(dir: String): Unit = {
    optimizedDirectory = dir
  }

  def getOptimizedDirectory: String = optimizedDirectory

  override def loadLibrary(className: String, paths: Array[String]): Library = {
    try {
      // Only the first path is used. Dex file doesn't contain.class files and therefore getResource() method can't be used to locate the files at runtime.
      val dexPath = paths.head
      val loaderClass = Class.forName("dalvik.system.DexClassLoader")

      /**
        * Description of DexClassLoader
        * A class loader that loads classes from .jar files containing a classes.dex entry.
        * This can be used to execute code not installed as part of an application.
        *
        * param dexPath jar file path where is contained the library.
        * param optimizedDirectory directory where optimized dex files should be written; must not be null
        * param libraryPath the list of directories containing native libraries, delimited by File.pathSeparator; may be null
        * param parent the parent class loader
        */
      val loaderConstructor = loaderClass.getConstructor(classOf[String], classOf[String], classOf[String], classOf[ClassLoader])
      val loader = loaderConstructor.newInstance(dexPath, this.getOptimizedDirectory, null, getClass.getClassLoader).asInstanceOf[ClassLoader]
      val lib = Class.forName(className, true, loader).newInstance.asInstanceOf[Library]
      Option(getLibrary(lib.getName)) match {
        case Some(oldLib) =>
          logger.warn(s"Library ${oldLib.getName} already loaded.")
          oldLib
        case None =>
          val file = new File(paths(0))
          val url = file.toURI.toURL
          externalLibs = externalLibs + (className -> url)
          bindLibrary(lib)
          logger.info(s"Loaded Library ${lib.getName}")
          val ev = new LibraryEvent(wam, lib.getName)
          wam.notifyLoadedLibrary(ev)
          lib
      }
    } catch {
      case NonFatal(ex) => throw new InvalidLibraryException(ex,className, "Failed to Load Library")
    }
  }
}