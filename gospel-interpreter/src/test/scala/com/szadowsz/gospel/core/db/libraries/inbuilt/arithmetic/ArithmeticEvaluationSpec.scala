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
package com.szadowsz.gospel.core.db.libraries.inbuilt.arithmetic

import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter, SolutionFlatSpecBehaviours}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ArithmeticEvaluationSpec extends FlatSpec with BaseEngineSpec with SolutionFlatSpecBehaviours {

  override protected def init(): Interpreter = new Interpreter()

  behavior of "Arithmetic Evaluation Predicate"

  it should behave like successfulQuery(getInterpreter,"'is'(3, 3).")
  
  it should behave like successfulQuery(getInterpreter,"3 is 3.")
  
  it should behave like successfulQuery(getInterpreter,"'is'(Result, 3 + 11.0).")
  
  it should behave like successfulQuery(getInterpreter,"Result is 3 + 11.0.")
  
  it should behave like successfulQuery(getInterpreter,"X = 1 + 2, Y is X * 3.")
  
  it should behave like unsuccessfulQuery(getInterpreter,"'is'(3, 3.0).")
  
  it should behave like unsuccessfulQuery(getInterpreter,"3 is 3.0.")
  
  it should behave like unsuccessfulQuery(getInterpreter,"'is'(foo, 77).")
  
  it should behave like unsuccessfulQuery(getInterpreter,"foo is 77.")
  
  it should behave like unsuccessfulQuery(getInterpreter,"is(_, foo).")

  it should behave like unsuccessfulQuery(getInterpreter,"'is'(77, N).")
}
