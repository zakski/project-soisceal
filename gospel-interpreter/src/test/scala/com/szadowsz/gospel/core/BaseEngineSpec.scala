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

import org.scalatest.{BeforeAndAfterEach, Matchers, Suite}

/**
  * Created on 16/02/2017.
  */
trait BaseEngineSpec extends Matchers with BeforeAndAfterEach {
  this: Suite =>

  protected var prolog : Interpreter = _

  protected def init(): Interpreter
  
  
  override def beforeEach(): Unit = {
    prolog = init()
  }
}