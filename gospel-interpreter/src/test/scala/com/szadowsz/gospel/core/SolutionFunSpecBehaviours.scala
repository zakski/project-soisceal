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
import org.scalatest.{FunSpec, Matchers}

trait SolutionFunSpecBehaviours extends Matchers with SolutionMatchers {
  this: BaseEngineSpec with FunSpec =>
  
  def successfulQuery(wam : Interpreter, query : Term) {
  
    it (s"query $query should be successful") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beSuccessful
    }
  }
  
  def successfulQuery(wam : Interpreter, query : String) {
  
    it (s"query $query should be successful") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beSuccessful
    }
  }
  
  def successfulQuery(wam : Interpreter, query : String, variable : (String,String)) {
    
    it (s"query $query should be successful where ${variable._1} = ${variable._2}") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beSuccessful
      
      val (varName,value) = variable
      val result = solution.getVarOpt(varName)
      result shouldBe defined
      result should contain (parseTerm(wam,value))
    }
  }
  
  def successfulQuery(wam : Interpreter, query : String, variable : (String,String), varList : (String,String)*) {
    
    it (s"query $query should be successful where ${variable._1} = ${variable._2}${varList.map(kv => kv._1 + " = " + kv._2).mkString(", ",", ","")}") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beSuccessful
      
      val (varName,value) = variable
      val result = solution.getVarOpt(varName)
      result shouldBe defined
      result should contain (parseTerm(wam,value))
  
      varList.foreach(v => {
        val (vName, vValue) = v
        val result2 = solution.getVarOpt(vName)
        result2 shouldBe defined
        result2 should contain(parseTerm(wam, vValue))
      })
    }
  }
  
  def unsuccessfulQuery(wam : Interpreter, query : Term) {
  
    it (s"query $query should be unsuccessful") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beUnsuccessful
    }
  }
  
  def unsuccessfulQuery(wam : Interpreter, query : String) {
  
    it (s"query $query should be unsuccessful") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beUnsuccessful
    }
  }
  
  def haltedQuery(wam : Interpreter, query : Term) {
    
    it (s"query $query should be halted") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beUnsuccessful
      solution should beHalted
    }
  }
  
  def haltedQuery(wam : Interpreter, query : String) {
    
    it (s"query $query should be halted") {
      wam should not be (null)
      val solution = wam.solve(query)
      solution should beUnsuccessful
      solution should beHalted
    }
  }
}


