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

import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter, SolutionFunSpecBehaviours}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimpleArithmeticFunctorsSpec extends FunSpec with BaseEngineSpec with SolutionFunSpecBehaviours {
  
  override protected def init(): Interpreter = new Interpreter()
  
  describe("Addition Functor") {
    it should behave like successfulQuery(getInterpreter,"X is '+'(7, 35).", ("X","42"))
  
    it should behave like successfulQuery(getInterpreter,"X is 7 + 35.", ("X","42"))
 
    it should behave like successfulQuery(getInterpreter,"X is '+'(0, 3+11).", ("X","14"))
    
    it should behave like successfulQuery(getInterpreter,"X is 0 + 3 + 11.", ("X","14"))
   
    it should behave like successfulQuery(getInterpreter,"X is '+'(0, 3.2+11).", ("X","14.2"))
    
    it should behave like successfulQuery(getInterpreter,"X is 0 + 3.2 + 11.", ("X","14.2"))
  }
  
  describe("Unitary Negation Functor") {
    it should behave like successfulQuery(getInterpreter,"X is '-'(7).", ("X","-7"))
  
    it should behave like successfulQuery(getInterpreter,"X is '-'(3 - 11).", ("X","8"))
  
    it should behave like successfulQuery(getInterpreter,"X is '-'(3.2 - 11).", ("X","7.8"))
  }
  
  describe("Subtraction Functor") {
    it should behave like successfulQuery(getInterpreter,"X is '-'(7, 35).", ("X","-28"))
    
    it should behave like successfulQuery(getInterpreter,"X is 7 - 35.", ("X","-28"))
 
    it should behave like successfulQuery(getInterpreter,"X is '-'(20, 3+11).", ("X","6"))
 
    it should behave like successfulQuery(getInterpreter,"X is 20 - (3 + 11).", ("X","6"))
 
    it should behave like successfulQuery(getInterpreter,"X is '-'(0, 3.2+11).", ("X","-14.2"))
  
    it should behave like successfulQuery(getInterpreter,"X is 0 - (3.2 + 11).", ("X","-14.2"))
  }
  
  describe("Multiplication Functor") {
    it should behave like successfulQuery(getInterpreter,"X is '*'(7, 35).", ("X","245"))
    
    it should behave like successfulQuery(getInterpreter,"X is 7 * 35.", ("X","245"))
  
    it should behave like successfulQuery(getInterpreter,"X is '*'(1.5, 3.2+11).", ("X","21.299999999999997"))
  
    it should behave like successfulQuery(getInterpreter,"X is 1.5 * ( 3.2 + 11).", ("X","21.299999999999997"))
  }
  
  describe("Division Functor") {
    it should behave like successfulQuery(getInterpreter,"X is '/'(7, 35).", ("X","0"))
    
    it should behave like successfulQuery(getInterpreter,"X is 7 / 35.", ("X","0"))
  
    it should behave like successfulQuery(getInterpreter,"X is '/'(7.0, 35).", ("X","0.2"))
  
    it should behave like successfulQuery(getInterpreter,"X is 7.0 / 35.", ("X","0.2"))
 
    it should behave like successfulQuery(getInterpreter,"X is '/'(140, 3+11).", ("X","10"))
  
    it should behave like successfulQuery(getInterpreter,"X is 140 / (3 + 11).", ("X","10"))
    
    it should behave like successfulQuery(getInterpreter,"X is '/'(20.164, 3.2+11).", ("X","1.4200000000000002"))
  
    it should behave like successfulQuery(getInterpreter,"X is 20.164 / (3.2 + 11).", ("X","1.4200000000000002"))
  }
  
  describe("Modulo Functor") {
    it should behave like successfulQuery(getInterpreter,"X is mod(7, 3).", ("X","1"))
    
    it should behave like successfulQuery(getInterpreter,"X is 7 mod 3.", ("X","1"))
    
    it should behave like successfulQuery(getInterpreter,"X is mod(0, 3 + 11).", ("X","0"))
    
    it should behave like successfulQuery(getInterpreter,"X is 0 mod (3 + 11).", ("X","0"))
    
    it should behave like successfulQuery(getInterpreter,"X is mod(7, -2).", ("X","-1"))
    
    it should behave like successfulQuery(getInterpreter,"X is 7 mod -2.", ("X","-1"))
  }
  
  describe("Remainder Functor") {
    it should behave like successfulQuery(getInterpreter,"X is rem(7, 3).", ("X","1"))
    
    it should behave like successfulQuery(getInterpreter,"X is 7 rem 3.", ("X","1"))
    
    it should behave like successfulQuery(getInterpreter,"X is rem(0, 3 + 11).", ("X","0"))
    
    it should behave like successfulQuery(getInterpreter,"X is 0 rem (3 + 11).", ("X","0"))
    
    it should behave like successfulQuery(getInterpreter,"X is rem(7, -2).", ("X","1"))
    
    it should behave like successfulQuery(getInterpreter,"X is 7 rem -2.", ("X","1"))
  }
  
  describe("Floor Functor") {
    it should behave like successfulQuery(getInterpreter,"X is floor(7.4).", ("X","7"))
 
    it should behave like successfulQuery(getInterpreter,"X is floor(-0.4).", ("X","-1"))
  }
  
  describe("Round Functor") {
    it should behave like successfulQuery(getInterpreter,"X is round(7.4).", ("X","7"))
   
    it should behave like successfulQuery(getInterpreter,"X is round(7.5).", ("X","8"))
  
    it should behave like successfulQuery(getInterpreter,"X is round(-0.4).", ("X","0"))
 
    it should behave like successfulQuery(getInterpreter,"X is round(-0.6).", ("X","-1"))
  }
  
  describe("Ceiling Functor") {
    it should behave like successfulQuery(getInterpreter,"X is ceiling(7.4).", ("X","8"))
    
    it should behave like successfulQuery(getInterpreter,"X is ceiling(7.5).", ("X","8"))
    
    it should behave like successfulQuery(getInterpreter,"X is ceiling(-0.5).", ("X","0"))
    
    it should behave like successfulQuery(getInterpreter,"X is ceiling(-0.6).", ("X","0"))
  }
  
  describe("Truncate Functor") {
    it should behave like successfulQuery(getInterpreter,"X is truncate(-0.5).", ("X","0"))
    
    it should behave like successfulQuery(getInterpreter,"X is truncate(0.5).", ("X","0"))
  }
  
  describe("Float Functor") {
    it should behave like successfulQuery(getInterpreter,"X is float(7).", ("X","7.0"))
    
    it should behave like successfulQuery(getInterpreter,"X is float(7.3).", ("X","7.3"))
 
    it should behave like successfulQuery(getInterpreter,"X is float(5 / 3).", ("X","1.0"))
  }
  
  describe("Abs Functor") {
    it should behave like successfulQuery(getInterpreter,"X is abs(7).", ("X","7"))
    
    it should behave like successfulQuery(getInterpreter,"X is abs(3-11).", ("X","8"))
    
    it should behave like successfulQuery(getInterpreter,"X is abs(3.2-11.0).", ("X","7.8"))
  }
}