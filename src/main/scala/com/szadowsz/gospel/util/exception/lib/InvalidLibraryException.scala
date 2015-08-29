/* tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.util.exception.lib

import com.szadowsz.gospel.core.exception.interpreter.InterpreterException

@SerialVersionUID(1L)
class InvalidLibraryException(libName: String, line: Int, pos: Int, theCause : Throwable) extends InterpreterException(theCause) {
  protected val _libraryName = libName

  private val _line = line
  private val _pos = pos


  def this(libName: String) {
    this(libName,0,0,null)
  }

  def this(libName: String, theCause : Throwable) {
    this(libName,0,0,theCause)
  }

  def getLibraryName = _libraryName

  def getLine = _line

  def getPos = _pos

  override def getMessage = "Error in Library " + _libraryName + " at " + _line + ":" + _pos
}