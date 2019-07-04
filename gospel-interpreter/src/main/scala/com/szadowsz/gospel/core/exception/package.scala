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
package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.data.{Int, Struct, Term}

package object exception {
  
  /**
    * Implicit class to convert Throwable info into Struct
    *
    * @param thrown the throwable to convert
    */
  implicit class JvmException(thrown: Throwable) {
    
    private def buildCauseTerm(): Term = {
      Option(thrown.getCause) match {
        case Some(cause) => new Struct(cause.toString)
        case None => Int(0)
      }
    }
    
    private def buildMessageTerm(): Term = {
      Option(thrown.getMessage) match {
        case Some(message) => new Struct(message)
        case None => Int(0)
      }
    }
    
    private def buildStackTraceTerm(): Term = {
      val stackTraceTerm = new Struct
      val elements = thrown.getStackTrace
      for (element <- elements) {
        stackTraceTerm.append(new Struct(element.toString))
      }
      stackTraceTerm
    }
    
    /**
      * term to represent the Java exception in java_throw/1 struct
      */
    def getExceptionStruct: Struct = {
      new Struct(thrown.getClass.getName, buildCauseTerm(), buildMessageTerm(), buildStackTraceTerm())
    }
  }
}
