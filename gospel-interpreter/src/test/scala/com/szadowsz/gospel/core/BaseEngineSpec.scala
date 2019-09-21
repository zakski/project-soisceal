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
package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.parser.NParser
import org.scalatest.{BeforeAndAfterEach, Matchers, Suite}

/**
  * Created on 16/02/2017.
  */
trait BaseEngineSpec extends Matchers with BeforeAndAfterEach {
  this: Suite =>

  protected var prolog : Interpreter = _

  protected def init(): Interpreter
  
  protected def getInterpreter : Interpreter = {
    Option(prolog) match {
      case None =>
        prolog = init()
        prolog
      case Some (_) => prolog
    }
  }
  
  protected def parseTerm(prolog: Interpreter, term: String): Term = {
    new NParser().parseTerm(term)(prolog.getOperatorManager)
  }
  
  protected def parseTerm(term: String): Term = {
    new NParser().parseTerm(term)(prolog.getOperatorManager)
  }
  
  override def beforeEach(): Unit = {
    Option(prolog) match {
      case None =>
        prolog = init()
      case Some (_) =>
    }
  }
  
  override def afterEach(): Unit = {
    prolog = null
  }
}