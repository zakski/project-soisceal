/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
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
package alice.tuprolog.core.exception.interpreter


/**
 * TODO REWRITE THIS CLASS TO TAKE SCALA CONSTRUCTOR LIMITATIONS PROPERLY INTO ACCOUNT
 *
 * This exceptions means that a not valid tuProlog theory has been specified
 *
 * @see Theory
 *
 */
@SerialVersionUID(1L)
class InvalidTheoryException(message: String, c: Int, l: Int, p: Int) extends InterpreterException(message) {
  val line = l;
  val pos = p;
  /*Castagna 06/2011*/
  val clause = c;

  def this(message: String) {
    this(message, -1, -1, -1) //TODO REWRITE THIS METHOD TO TAKE SCALA CONSTRUCTOR LIMITATIONS PROPERLY INTO ACCOUNT
  }

  def this() {
    this("") //TODO REWRITE THIS METHOD TO TAKE SCALA CONSTRUCTOR LIMITATIONS PROPERLY INTO ACCOUNT
  }

  def this(line: Int, pos: Int) {
    this("", -1, line, pos) //TODO REWRITE THIS METHOD TO TAKE SCALA CONSTRUCTOR LIMITATIONS PROPERLY INTO ACCOUNT
  }
}