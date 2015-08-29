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
package alice.tuprolog.core.exception

import alice.tuprolog.core.data.numeric.Int
import alice.tuprolog.core.data.{Struct, Term}

@SerialVersionUID(1L)
class JVMException(thrown: Throwable) extends Throwable {

  // JVM exception that is the subject of java_throw/1 from prolog
  private val _thrown = thrown

  def getException() = {
    // java_exception
    val java_exception = _thrown.getClass().getName()

    // Cause
    val cause = _thrown.getCause()
    val causeTerm: Term = if (cause != null) new Struct(cause.toString()) else new Int(0)

    // Message
    val message = _thrown.getMessage()
    val messageTerm: Term = if (message != null) new Struct(message) else new Int(0)

    // StackTrace
    val stackTraceTerm = new Struct()
    val elements = _thrown.getStackTrace()
    for (element <- elements) {
      stackTraceTerm.append(new Struct(element.toString()))
    }
    new Struct(java_exception, causeTerm, messageTerm, stackTraceTerm)
  }

}