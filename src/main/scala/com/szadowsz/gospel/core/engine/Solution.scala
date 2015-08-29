package com.szadowsz.gospel.core.engine

import java.{util => ju}

import com.szadowsz.gospel.core
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.exception.interpreter.UnknownVarException
import com.szadowsz.gospel.util.exception.solve.NoSolutionException

import scala.collection.JavaConverters._

/**
 * Solution class represents the result of a solve
 * request made to the engine, providing information
 * about the solution
 *
 * @author Zak Szadowski
 */
@SerialVersionUID(1L)
class Solution(initGoal: Term, resultGoal: Struct, resultDemo: Int, resultVars: ju.List[Var], sinfoSetOf : String) extends Serializable {
  private val _query: Term = initGoal

  private val _goal: Struct = resultGoal
  private val _endState: Int = resultDemo
  private val _isSuccess: Boolean = _endState > EngineRunner.FALSE

  private val _bindings: List[Var] = resultVars.asScala.toList
  private val _setOfSolution: String = sinfoSetOf


  /**
   * Auxiliary Constructor for situation where we have recovered from an exception
   * @param initGoal the Initial Query given to the Intepreter
   */
  def this(initGoal: Term) {
    this(initGoal, null, EngineRunner.FALSE, null,null)
  }


  /**
   * Checks if the solve request was successful
   *
   * @return  true if the solve was successful
   */
  def isSuccess: Boolean = _isSuccess

  /**
   * Checks if the solve request was halted
   *
   * @return true if the solve was successful
   */
  def isHalted: Boolean = _endState == EngineRunner.HALT


  /**
   * Checks if the solve request has any alternatives
   *
   * @return true if the solve was successful
   */
  def hasOpenAlternatives: Boolean = _endState == EngineRunner.TRUE_CP

  /**
   * Gets the query
   * @return  the query
   */
  def getQuery: Term = _query

  /**
   * Gets the solution of the request
   *
   * @throws NoSolutionException if the solve request has no solution
   * @return true if the solve was successful
   */
  @throws(classOf[NoSolutionException])
  def getSolution: Term = {
    if (_isSuccess) {
      _goal
    } else {
      throw new NoSolutionException
    }
  }

  def getSetOfSolution: String = _setOfSolution


  /**
   * Gets the list of the variables in the solution.
   * @return the array of variables.
   *
   * @throws NoSolutionException if current solve information
   *                             does not concern a successful
   */
  @throws(classOf[NoSolutionException])
  def getBindingVars: List[Var] = {
    if (_isSuccess) {
      _bindings
    } else {
      throw new NoSolutionException
    }
  }

  /**
   * Gets the value of a variable in the substitution.
   * @throws NoSolutionException if the solve request has no solution
   * @throws UnknownVarException if the variable does not appear in the substitution.
   */
  @throws(classOf[NoSolutionException])
  @throws(classOf[UnknownVarException])
  def getTerm(varName: String): Term = {
    val t: Term = getVarValue(varName)
    if (t != null) {
      t
    } else {
      throw new UnknownVarException
    }
  }

  /**
   * Gets the value of a variable in the substitution. Returns <code>null</code>
   * if the variable does not appear in the substitution.
   */
  @throws(classOf[NoSolutionException])
  def getVarValue(varName: String): Term = {
    if (_isSuccess) {
      for (v <- _bindings){
        if (v != null && (v.getName == varName)) {
          return v.getTerm
        }
      }
      null
    } else {
      throw new NoSolutionException
    }
  }

  /**
   * Returns the string representation of the result of the demonstration.
   *
   *  @return For successful demonstration, the representation concerns variables with bindings.
   *          For failed demonstrations, the method returns false string.
   *
   */
  override def toString: String = {
    if (_isSuccess) {
      val st: StringBuffer = new StringBuffer("yes")
      if (_bindings.size > 0) {
        st.append(".\n")
      }
      else {
        st.append(". ")
      }
      for (v <- _bindings){
        if (v != null && !v.isAnonymous && v.isBound
          && (!v.getTerm.isInstanceOf[Var]
          || (!v.getTerm.asInstanceOf[Var].getName.startsWith("_")))) {
          st.append(v)
          st.append("  ")
        }
      }
      st.toString.trim
    }
    else {
      if (_endState == core.engine.EngineRunner.HALT){
        "halt."
      } else {
        "no."
      }
    }
  }
}
