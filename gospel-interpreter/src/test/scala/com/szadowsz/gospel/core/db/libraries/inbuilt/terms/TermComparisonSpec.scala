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
package com.szadowsz.gospel.core.db.libraries.inbuilt.terms

import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter, SolutionFunSpecBehaviours}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TermComparisonSpec extends FunSpec with BaseEngineSpec with SolutionFunSpecBehaviours {
  
  override protected def init(): Interpreter = new Interpreter()
  
  describe("@=</2 Predicate") {
    
    it should behave like successfulQuery(getInterpreter, "1.0 @=< 1.")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(1.0, 1).")
    
    it should behave like successfulQuery(getInterpreter, "aardvark @=< zebra.")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(aardvark, zebra).")
    
    it should behave like successfulQuery(getInterpreter, "short @=< short.")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(short, short).")
    
    it should behave like successfulQuery(getInterpreter, "short @=< shorter.")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(short, shorter).")
    
    it should behave like successfulQuery(getInterpreter, "X @=< Y.")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(X, Y).")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(_, _).")
    
    it should behave like successfulQuery(getInterpreter, "foo(X, a) @=< foo(Y, b).")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(foo(X, a), foo(Y, b)).")
    
    it should behave like successfulQuery(getInterpreter, "X @=< X.")
    
    it should behave like successfulQuery(getInterpreter, "'@=<'(X, X).")
  }
  
  describe("@>=/2 Predicate") {
    
    it should behave like successfulQuery(getInterpreter, "1.0 @>= 1.0.")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(1.0, 1).")
    
    it should behave like successfulQuery(getInterpreter, "zebra @>= aardvark.")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(zebra, aardvark).")
    
    it should behave like successfulQuery(getInterpreter, "short @>= short.")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(short, short).")
    
    it should behave like successfulQuery(getInterpreter, "shorter @>= short.")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(shorter, short).")
    
    it should behave like successfulQuery(getInterpreter, "Y @>= X.")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(Y, X).")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(_, _).")
    
    it should behave like successfulQuery(getInterpreter, "foo(Y, b) @>= foo(X, a).")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(foo(Y, b), foo(X, a)).")
    
    it should behave like successfulQuery(getInterpreter, "X @>= X.")
    
    it should behave like successfulQuery(getInterpreter, "'@>='(X, X).")
  }
  
  describe("@>/2 Predicate") {
    it should behave like successfulQuery(getInterpreter, "foo(b) @> foo(a).")
    
    it should behave like successfulQuery(getInterpreter, "'@>'(foo(b), foo(a)).")
    
  }
  
  describe("@</2 Predicate") {
    it should behave like successfulQuery(getInterpreter, "'@<'(foo(a, b), north(a)).")
    
    it should behave like successfulQuery(getInterpreter, "'@<'(1.0, 1).")
    
    it should behave like successfulQuery(getInterpreter, "'@<'(foo(a, X), foo(b, Y)).")
    
    it should behave like successfulQuery(getInterpreter, "'@<'(foo(X, a), foo(Y, b)).")
  }
  
  
  describe("==/2 Predicate") {
    
    it should behave like successfulQuery(getInterpreter, "1.0 == 1.0.")
    
    it should behave like successfulQuery(getInterpreter, "'=='(1.0, 1.0).")
  
    it should behave like unsuccessfulQuery(getInterpreter, "1.0 == 1.")

    it should behave like unsuccessfulQuery(getInterpreter, "'=='(1.0, 1).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "zebra == aardvark.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'=='(zebra, aardvark).")
    
    it should behave like successfulQuery(getInterpreter, "short == short.")
    
    it should behave like successfulQuery(getInterpreter, "'=='(short, short).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "shorter == short.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'=='(shorter, short).")
  
    it should behave like successfulQuery(getInterpreter, "Y = a,X = a, X == Y.")
   
    it should behave like unsuccessfulQuery(getInterpreter, "Y == X.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'=='(Y, X).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'=='(_, _).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "foo(Y, b) ==foo(X, a).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'=='(foo(Y, b), foo(X, a)).")
    
    it should behave like successfulQuery(getInterpreter, "X == X.")
    
    it should behave like successfulQuery(getInterpreter, "'=='(X, X).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "X == Y.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'=='(X, Y).")
  }
  
  describe("\\==/2 Predicate") {
    
    it should behave like unsuccessfulQuery(getInterpreter, "X \\== X.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'\\=='(X, X).")
    
    it should behave like successfulQuery(getInterpreter, "X \\== Y.")
    
    it should behave like successfulQuery(getInterpreter, "'\\=='(X, Y).")
    
    it should behave like successfulQuery(getInterpreter, "'\\=='(_, _).")
    
    it should behave like unsuccessfulQuery(getInterpreter, "1 \\== 1.")
    
    it should behave like unsuccessfulQuery(getInterpreter, "'\\=='(1, 1).")
    
    it should behave like successfulQuery(getInterpreter, "2 \\== 1.")
    
    it should behave like successfulQuery(getInterpreter, "'\\=='(2, 1).")
  }
}