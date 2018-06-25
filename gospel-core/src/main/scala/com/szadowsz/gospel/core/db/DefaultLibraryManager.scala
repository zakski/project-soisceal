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
import java.net.{URL, URLClassLoader}

import com.szadowsz.gospel.core.PrologEngine
import com.szadowsz.gospel.core.event.interpreter.LibraryEvent
import com.szadowsz.gospel.core.exception.InvalidLibraryException

import scala.util.control.NonFatal

final case class DefaultLibraryManager(override protected val wam: PrologEngine) extends LibraryManager {

  private def getClassResource(klass: Class[_]): URL = Option(klass) match {
    case None => null
    case Some(c) => c.getClassLoader.getResource(c.getName.replace('.', '/') + ".class")
  }

  override def loadLibrary(className: String, paths: Array[String]): Library = {
    //  = {
    try {
      val urls = paths.map(p => (if (!p.contains(".class")) new File(p) else new File(p.substring(0, p.lastIndexOf(File.separator) + 1))).toURI.toURL)
      val loader = URLClassLoader.newInstance(urls, getClass.getClassLoader)
      val lib = Class.forName(className, true, loader).newInstance.asInstanceOf[Library]
      Option(getLibrary(lib.getName)) match {
        case Some(oldLib) =>
          logger.warn(s"Library ${oldLib.getName} already loaded.")
          oldLib
        case None =>
          externalLibs = externalLibs + (className -> getClassResource(lib.getClass))
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