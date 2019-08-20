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

class InvalidTheoryException(
                              message: String,
                              cause: Throwable,
                              errorTerm: String,
                              errorClause: Int,
                              errorLine: Int,
                              errorPos: Int
                            ) extends PrologException(message, cause) {

  protected val term: String = errorTerm

  protected val clause: Int = errorClause

  protected val line: Int = errorLine

  protected val pos: Int = errorPos

  /**
    * Constructs a new exception with the specified detail message.
    *
    * @param message the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(message: String) = {
    this(message, null,null,-1,-1,-1)
  }

  /**
    * Constructs a new exception with the specified detail message.
    *
    * @param message the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(cause: InvalidTermException, clause: Int) = {
    this(cause.getMessage, cause, cause.getTerm, clause, cause.getLine, cause.getCol)
  }
  
  /**
    * Constructs a new exception with the specified detail message.
    *
    * @param message the detail message (which is saved for later retrieval by the { @link #getMessage()} method).
    */
  def this(message: String, cause: Throwable, clause: Int) = {
    this(message, cause, null, clause, -1, -1)
  }
  def getClause: Int = clause

  def getTerm: String = term

  def getLine: Int = line

  def getCol: Int = pos

  /**
    * Returns a short description of this throwable.
    *
    * @return a string representation of this throwable.
    */
  override def toString: String = {
    val builder = new StringBuilder()

    // First Add the Exception Name
    builder ++= getClass.getName
    builder += ':'

    if (errorTerm != null){
      builder ++= " (Term: "
      builder ++= errorTerm
      if (errorLine >= 0 || errorPos >= 0){
        builder ++= " at"
      }
    }

    if (errorLine >= 0 || errorPos >= 0){
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
