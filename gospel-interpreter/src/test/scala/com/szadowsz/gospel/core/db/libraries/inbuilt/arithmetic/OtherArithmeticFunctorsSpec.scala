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
class OtherArithmeticFunctorsSpec extends FunSpec with BaseEngineSpec with SolutionFunSpecBehaviours {
  
  override protected def init(): Interpreter = new Interpreter()
 
 
  describe("(**)/2 Functor") {
    it should behave like successfulQuery(getInterpreter,"X is '**'(5, 3).", ("X","125.0"))
   
    it should behave like successfulQuery(getInterpreter,"X is 5 ** 3.", ("X","125.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is '**'(-5, 3).", ("X","-125.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is -5 ** 3.", ("X","-125.0"))
   
    it should behave like successfulQuery(getInterpreter,"X is '**'(5, -1).", ("X","0.2"))
  
    it should behave like successfulQuery(getInterpreter,"X is 5 ** -1.", ("X","0.2"))
 
    it should behave like successfulQuery(getInterpreter,"X is '**'(5, 3.0).", ("X","125.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is 5 ** 3.0.", ("X","125.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is '**'(0.0, 0).", ("X","1.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is 0.0 ** 0.", ("X","1.0"))
  }
  
  describe("sin Functor") {
    it should behave like successfulQuery(getInterpreter,"X is sin(0.0).", ("X","0.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is sin(0).", ("X","0.0"))
 
    it should behave like successfulQuery(getInterpreter,"X is sin(pi / 2).", ("X","1.0")) // 90 degrees in radians
    
    it should behave like successfulQuery(getInterpreter,"X is sin(pi).", ("X","0.0")) // 180 degrees in radians
  
    it should behave like successfulQuery(getInterpreter,"X is sin(pi + pi/2).", ("X","-1.0")) // 270 degrees in radians
  }
  
  describe("cos Functor") {
    it should behave like successfulQuery(getInterpreter,"X is cos(0.0).", ("X","1.0"))
    
    it should behave like successfulQuery(getInterpreter,"X is cos(0).", ("X","1.0"))
    
    it should behave like successfulQuery(getInterpreter,"X is cos(pi / 2).", ("X","0.0")) // 90 degrees in radians
    
    it should behave like successfulQuery(getInterpreter,"X is cos(pi).", ("X","-1.0")) // 180 degrees in radians
    
    it should behave like successfulQuery(getInterpreter,"X is cos(pi + pi/2).", ("X","0.0")) // 270 degrees in radians
  }
  
  describe("atan Functor") {
    it should behave like successfulQuery(getInterpreter,"PI is atan(1.0) * 4, X is sin(PI / 2.0).", ("X","1.0"),("PI","3.141592653589793"))
    
    it should behave like successfulQuery(getInterpreter,"PI is atan(1.0) * 4.", ("PI","3.141592653589793"))
  }

  describe("exp Functor") {
    it should behave like successfulQuery(getInterpreter,"X is exp(0.0).", ("X","1.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is exp(0).", ("X","1.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is exp(1.00).", ("X","2.718281828459045"))
  }

  describe("log Functor") {
    it should behave like successfulQuery(getInterpreter,"X is log(1.0).", ("X","0.0"))
    
    it should behave like successfulQuery(getInterpreter,"X is log(1).", ("X","0.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is log(2.718281828459045).", ("X","1.0"))
  }

  describe("sqrt Functor") {
    it should behave like successfulQuery(getInterpreter,"X is sqrt(0.0).", ("X","0.0"))
 
    it should behave like successfulQuery(getInterpreter,"X is sqrt(1.0).", ("X","1.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is sqrt(25).", ("X","5.0"))
  
    it should behave like successfulQuery(getInterpreter,"X is sqrt(1.21).", ("X","1.1"))
  }
}
