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

import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter, SolutionFlatSpecBehaviours, SolutionFunSpecBehaviours}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, FunSpec}
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ArithmeticComparisonSpec extends FunSpec with BaseEngineSpec with SolutionFunSpecBehaviours {

  override protected def init(): Interpreter = new Interpreter()

  describe("Not Equals Predicate") {
  
    it should behave like successfulQuery(getInterpreter, "'=\\='(0, 1).")
  
    it should behave like successfulQuery(getInterpreter, "0 =\\= 1.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "1 =\\= 1.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "1.0 =\\= 1.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "3 * 2 =\\= 7 - 1.")
  }
  
  describe("Less Than Predicate") {
  
    it should behave like successfulQuery(getInterpreter, "'<'(0, 1).")
  
    it should behave like successfulQuery(getInterpreter, "0 < 1.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "1 < 1.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "2 < 1.")
 
    it should behave like unsuccessfulQuery(getInterpreter, "3 * 2 < 7 - 1.")
  }
  
  describe("Less Than Or Equal Predicate") {
    
    it should behave like successfulQuery(getInterpreter, "'=<'(0, 1).")
    
    it should behave like successfulQuery(getInterpreter, "0 =< 1.")
    
    it should behave like successfulQuery(getInterpreter, "1 =< 1.")
   
    it should behave like successfulQuery(getInterpreter, "1.0 =< 1.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "2 =< 1.")
  
    it should behave like successfulQuery(getInterpreter, "3 * 2 =< 7 - 1.")
  
  }
 
  describe("Equals Predicate") {
    
    it should behave like unsuccessfulQuery(getInterpreter, "'=:='(0, 1).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "0 =:= 1.")
    
    it should behave like successfulQuery(getInterpreter, "1 =:= 1.")
   
    it should behave like successfulQuery(getInterpreter, "1.0 =:= 1.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "2 =:= 1.")
    
    it should behave like successfulQuery(getInterpreter, "3 * 2 =:= 7 - 1.")
   
    it should behave like haltedQuery(getInterpreter, "X =:= 5.")
  }
  
  describe("Greater Than Or Equal Predicate") {
    
    it should behave like successfulQuery(getInterpreter, "'=<'(1, 0).")
    
    it should behave like successfulQuery(getInterpreter, "1 >= 0.")
    
    it should behave like successfulQuery(getInterpreter, "1 >= 1.")
    
    it should behave like successfulQuery(getInterpreter, "1.0 >= 1.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "1 >= 2.")
 
    it should behave like successfulQuery(getInterpreter, "3 * 2 >= 7 - 1.")
  }
  
  describe("Greater Than Predicate") {
    
    it should behave like successfulQuery(getInterpreter, "'>'(1, 0).")
    
    it should behave like successfulQuery(getInterpreter, "1 > 0.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "1 > 1.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "1 > 2.")
  
    it should behave like unsuccessfulQuery(getInterpreter, "3 * 2 > 7 - 1.")
  }
  
//  it should "pass exception test #1" in {
//    val ex = getExceptionListener
//    prolog.addExceptionListener(ex)
//    val solution = prolog.solve("'=:='(X, 5).")
//    ex.exFound shouldBe true
//    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_equality(X_e1,5)"
//  }
//
//
//  it should "pass exception test #2" in {
//    val ex = getExceptionListener
//    prolog.addExceptionListener(ex)
//    val solution = prolog.solve("'=\\='(X, 5).")
//    ex.exFound shouldBe true
//    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_equality(X_e1,5)"
//  }
//
//
//  it should "pass exception test #3" in {
//    val ex = getExceptionListener
//    prolog.addExceptionListener(ex)
//    val solution = prolog.solve("'<'(X, 5).")
//    ex.exFound shouldBe true
//    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_less_than(X_e1,5)"
//  }
//
//
//  it should "pass exception test #4" in {
//    val ex = getExceptionListener
//    prolog.addExceptionListener(ex)
//    val solution = prolog.solve("'>'(X, 5).")
//    ex.exFound shouldBe true
//    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_greater_than(X_e1,5)"
//  }
//
//
//  it should "pass exception test #5" in {
//    val ex = getExceptionListener
//    prolog.addExceptionListener(ex)
//    val solution = prolog.solve("'>='(X, 5).")
//    ex.exFound shouldBe true
//    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_greater_or_equal_than(X_e1,5)"
//  }
//
//
//  it should "pass exception test #6" in {
//    val ex = getExceptionListener
//    prolog.addExceptionListener(ex)
//    val solution = prolog.solve("'=<'(X, 5).")
//    ex.exFound shouldBe true
//    ex.exMsg shouldBe "Instantiation error in argument 1 of expression_less_or_equal_than(X_e1,5)"
//  }
}
