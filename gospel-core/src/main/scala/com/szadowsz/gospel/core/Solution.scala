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
 *//*
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

import java.io._
import java.util

import scala.collection.JavaConverters._
import alice.tuprolog.json.JSONSerializerManager
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.ExecutionResultType
import com.szadowsz.gospel.core.error.{NoSolutionException, UnknownVarException}

@SerialVersionUID(1L)
object Solution {
  def fromJSON(jsonString: String): Solution = JSONSerializerManager.fromJSON(jsonString, classOf[Solution])
}

/**
  *
  * Solution represents the result of a solve request made to the engine, providing information about the end result.
  *
  * Created on 01/03/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
class Solution(q: Term, g: Struct, result: ExecutionResultType.Value, resultVars: util.List[Var]) extends Serializable {
  private val query: Term = q
  private val goal: Struct = g
  private val endState: ExecutionResultType.Value = result
  private val bindings: util.List[Var] = resultVars

  def this(initGoal: Term) {
    this(initGoal, null, ExecutionResultType.FALSE, new util.ArrayList[Var]())
  }

  /**
    * Checks if the solve request was successful
    *
    * @return true if the solve was successful
    */
  def isSuccess: Boolean = endState > ExecutionResultType.FALSE

  /**
    * Checks if the solve request was halted
    *
    * @return true if the solve was successful
    */
  def isHalted: Boolean = endState == ExecutionResultType.HALT

  /**
    * Checks if the solve request was halted
    *
    * @return true if the solve was successful
    */
  def hasOpenAlternatives: Boolean = endState == ExecutionResultType.TRUE_CP

  /**
    * Gets the query
    *
    * @return the query
    */
  def getQuery: Term = query

  /**
    * Gets the solution of the request
    *
    * @throws NoSolutionException if the query had no solution.
    */
  @throws[NoSolutionException]
  def getSolution: Term = if (isSuccess) goal else throw new NoSolutionException

  /**
    * Gets the list of the variables in the solution.
    *
    * @return the array of variables.
    *
    * @throws NoSolutionException if the query had no solution.
    */
  @throws[NoSolutionException]
  def getBindingVars: util.List[Var] = if (isSuccess) bindings else throw new NoSolutionException

  /**
    * Gets the value of a variable in the substitution.
    *
    * @throws NoSolutionException if the solve request has no solution
    * @throws UnknownVarException if the variable does not appear in the substitution.
    */
  @throws[NoSolutionException]
  @throws[UnknownVarException]
  def getTerm(varName: String): Term = {
    val term: Term = getVarValue(varName)
    if (term == null) throw new UnknownVarException else term
  }

  /**
    * Gets the value of a variable in the substitution. Returns <code>null</code>
    * if the variable does not appear in the substitution.
    */
  @throws[NoSolutionException]
  def getVarValue(varName: String): Term = {
    if (isSuccess) bindings.asScala.find(v => v.getName == varName).map(_.getTerm).orNull else throw new NoSolutionException
  }

  /**
    * Returns the string representation of the result of the demonstration.
    *
    * For successful demonstration, the representation concerns
    * variables with bindings.  For failed demo, the method returns false string.
    *
    */
  override def toString: String = {
    if (isSuccess) {
      val st: StringBuffer = new StringBuffer("yes")
      if (bindings.size > 0) {
        st.append(".\n")
      } else {
        st.append(". ")
      }
     for (v <- bindings.asScala){
          if (v != null && !v.isAnonymous && v.isBound && (!v.getTerm.isInstanceOf[Var] || (!v.getTerm.asInstanceOf[Var].getName.startsWith("_")))) {
            st.append(v)
            st.append("  ")
          }
        }
      st.toString.trim
    } else {
     if (endState == ExecutionResultType.HALT) "halt." else "no."
    }
  }

  def toJSON: String = JSONSerializerManager.toJSON(this)
}