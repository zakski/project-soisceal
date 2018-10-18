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
package com.szadowsz.gospel.core.exception.library

import com.szadowsz.gospel.core.exception.PrologException

/**
  * Classification of Prolog Exceptions caused by a failure to create or validate a Library.
  *
  * @param libName the name of the prolog library that caused the error
  * @param message the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
  * @param cause   the cause (which is saved for later retrieval by the { @link #getCause()} method). (A <tt>null</tt>
  *                value is permitted, and indicates that the cause is nonexistent or unknown.)
  */
abstract class LibraryException(libName : String, message: String, cause : Throwable) extends PrologException(message, cause) {

  protected val lib: String = libName

  def getLib : String = lib
}
