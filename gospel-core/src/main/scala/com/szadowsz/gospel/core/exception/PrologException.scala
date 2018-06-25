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
 */
package com.szadowsz.gospel.core.exception

/**
  * Constructs a new prolog exception with the specified detail message and cause.
  *
  * @note The detail message associated with cause is <i>not</i> automatically incorporated in this exception's
  *       detail message.
  * @param message the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
  * @param cause   the cause (which is saved for later retrieval by the { @link #getCause()} method). (A <tt>null</tt>
  *                value is permitted, and indicates that the cause is nonexistent or unknown.)
  */
class PrologException(message: String, cause: Throwable) extends Exception(message, cause) {


  /**
    * Constructs a new exception with null as its detail message.
    */
  def this() = {
    this(null, null)
  }

  /**
    * Constructs a new exception with the specified detail message.
    *
    * @param message the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(message: String) = {
    this(message, null)
  }
}
