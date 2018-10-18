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

class InvalidLibraryException(
                               message: String,
                               cause: Throwable,
                               errorLib: String,
                               errorTerm: String,
                               errorClause: Int,
                               errorLine: Int,
                               errorPos: Int
                             ) extends PrologException(message, cause) {

  protected val lib: String = errorLib

  protected val term: String = errorTerm

  protected val clause: Int = errorClause

  protected val line: Int = errorLine

  protected val pos: Int = errorPos

  /**
    * Constructs a new exception with the specified detail message.
    *
    * @param errorLib the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(cause: PrologException, errorLib: String) = {
    this(cause.getMessage, cause, errorLib, null, -1,-1,-1)
  }

  /**
    * Constructs a new exception with the specified detail message.
    *
    * @param errorLib the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(cause: Throwable, errorLib: String, msg : String) = {
    this(msg, cause, errorLib, null,-1, -1,-1)
  }

    /**
    * Constructs a new exception with the specified detail message.
    *
    * @param errorLib the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(msg: String, errorLib: String) = {
    this(msg, null, errorLib, null, -1,-1,-1)
  }

  /**
    * Constructs a new exception with the specified detail message.
    *
    * @param errorLib the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(cause: InvalidTheoryException, errorLib: String) = {
    this(cause.getMessage, cause, errorLib, cause.getTerm, cause.getClause, cause.getLine, cause.getCol)
  }

  def getClause: Int = clause

  def getTerm: String = term

  def getLine: Int = line

  def getPos: Int = pos

  /**
    * Returns a short description of this throwable.
    * The result is the concatenation of:
    * <ul>
    * <li> the {@linkplain Class#getName() name} of the class of this object
    * <li> ": " (a colon and a space)
    * <li> the result of invoking this object's {@link #getLocalizedMessage}
    * method
    * </ul>
    * If {@code getLocalizedMessage} returns {@code null}, then just
    * the class name is returned.
    *
    * @return a string representation of this throwable.
    */
  override def toString: String = {
    val builder = new StringBuilder()

    // First Add the Exception Name
    builder ++= getClass.getName
    builder += ':'

    if (lib != null){
      builder ++= " (Lib: "
      builder ++= errorLib
      builder ++= ")"
    }

    if (errorTerm != null) {
      if (clause >= 0) {
        builder ++= " (Term "
        builder.append(errorClause)
        builder ++= ": "
      } else {
        builder ++= " (Term: "
      }

      builder ++= errorTerm
      if (errorLine >= 0 || errorPos >= 0) {
        builder ++= " at"
      }
    }

    if (errorLine >= 0 || errorPos >= 0) {
      builder ++= " ["
      builder.append(errorLine)
      builder += ':'
      builder.append(errorPos)
      builder += ']'
    }

    if (errorTerm != null) {
      builder ++= " )"
    }

    // Finally add the Message
    val message = getLocalizedMessage
    if (message != null) {
      builder += ' '
      builder ++= message
    }

    builder.mkString
  }
}