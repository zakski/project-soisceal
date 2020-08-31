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
package com.szadowsz.gospel.core.engine.flags

import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter, SolutionFunSpecBehaviours}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FlagPredicateSpec extends FunSpec with BaseEngineSpec with SolutionFunSpecBehaviours {
  
  override protected def init(): Interpreter = new Interpreter()
  
  describe("set_prolog_flag/2") {
    it should behave like unsuccessfulQuery(getInterpreter,"set_prolog_flag(unknown, fail).")
  
    it should behave like unsuccessfulQuery(getInterpreter,"set_prolog_flag(X, off).")
    
    it should behave like unsuccessfulQuery(getInterpreter,"set_prolog_flag(5, decimals).")
  
    it should behave like unsuccessfulQuery(getInterpreter,"set_prolog_flag(date, 'July 1988').")
  
    it should behave like unsuccessfulQuery(getInterpreter,"set_prolog_flag(debug, trace).")
  }
  
  describe("current_prolog_flag/2") {
    
    it should behave like successfulQuery(getInterpreter,"current_prolog_flag(debug, off).")
   
    it should behave like successfulQuery(getInterpreter,"current_prolog_flag(F, V).")
  
    it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(5, _).")
  }
  
  describe("Integer Flag Limits"){
    it should behave like successfulQuery(getInterpreter,"current_prolog_flag(max_integer, MI).",("MI",Long.MaxValue.toString))
   
    it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(max_integer, MI), X is '+'(MI, 1).")
  
    it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(max_integer, MI), X is '-'('+'(MI, 1), 1).")
    
    it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(max_integer, MI), X is '*'(MI, 2).")
   
    // TODO Reassess expected test behaviour validity
   // it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(max_integer, MI), R is float(MI) * 2, X is floor(R).")
    
    it should behave like successfulQuery(getInterpreter,"current_prolog_flag(min_integer, MI).",("MI",Long.MinValue.toString))
  
    it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(min_integer, MI), X is '-'(MI, 1).")
  
    it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(min_integer, MI), X is '+'('-'(MI, 1), 1).")
  
    it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(min_integer, MI), X is '*'(MI, 2).")
  
    // TODO Reassess expected test behaviour validity
    // it should behave like unsuccessfulQuery(getInterpreter,"current_prolog_flag(min_integer, MI), R is float(MI) * 2, X is floor(R).")
  }
}
