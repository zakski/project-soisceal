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
 */
package com.szadowsz.gospel.core.engine.clause

import com.szadowsz.gospel.core.operation.OperatorManager

import java.{util => ju}
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.subgoal.tree.{SubGoalLeaf, SubGoalTree}


object ClauseInfo {

  private def canExtractStruct(body : Term) = body.isInstanceOf[Struct] && body.asInstanceOf[Struct].getName == ","

  /**
   * Gets a clause from a generic Term
   */
  private def extractHead(clause: Struct): Struct = {
    clause.getArg(0).asInstanceOf[Struct]
  }

  /**
   * Gets a clause from a generic Term
   */

  /* TODO renable private[gospel]*/ def extractBody(body: Term): SubGoalTree = {
    extractBody(new SubGoalTree, body)
  }

  private def extractBody(parent: SubGoalTree, theBody: Term): SubGoalTree = {
    if (!canExtractStruct(theBody)){
      parent.addLeaf(theBody)
    } else {
      val t = theBody.asInstanceOf[Struct].getArg(0)
      extractBody(if (!canExtractStruct(t))parent else parent.addBranch,t)
      extractBody(parent,theBody.asInstanceOf[Struct].getArg(1))
    }
    parent
  }

  private def indentPredicates(theTerm: Term): String = {
    theTerm match {
      case struct : Struct =>
        if (struct.getName == ",") {
          struct.getArg(0).toString + ",\n\t" + indentPredicates(struct.getArg(1))
        } else {
          theTerm.toString
        }
      case _ =>
        theTerm.toString
    }
  }

  private def indentPredicatesAsArgX(theTerm: Term, op: OperatorManager, p: scala.Int): String = {
    theTerm match {
      case co : Struct =>
        if (co.getName == ",") {
          val prio: scala.Int = op.opPrio(",", "xfy")
          val sb: StringBuilder = new StringBuilder(if (prio >= p) "(" else "")
          sb.append(co.getArg(0).toStringAsArgX(op, prio))
          sb.append(",\n\t")
          sb.append(indentPredicatesAsArgY(co.getArg(1), op, prio))

          if (prio >= p)
            sb.append(")")

          sb.toString()
        } else {
          co.toStringAsArgX(op, p)
        }
      case _ =>
        theTerm.toStringAsArgX(op, p)
    }
  }

  private def indentPredicatesAsArgY(theTerm: Term, op: OperatorManager, p: scala.Int): String = {
    theTerm match {
      case co : Struct =>
        if (co.getName == ",") {
          val prio: scala.Int = op.opPrio(",", "xfy")
          val sb: StringBuilder = new StringBuilder(if (prio > p) "(" else "")
          sb.append(co.getArg(0).toStringAsArgX(op, prio))
          sb.append(",\n\t")
          sb.append(indentPredicatesAsArgY(co.getArg(1), op, prio))

          if (prio > p)
            sb.append(")")

          sb.toString()
        }
        else {
          co.toStringAsArgY(op, p)
        }
      case _ =>
        theTerm.toStringAsArgY(op, p)
    }
  }
}

/**
 * This class mantains information about a clause creation
 * (clause copy, final time T after renaming, validity stillValid Flag).
 * These information are necessary to the Theory Manager
 * to use the clause in a consistent way
 *
 * CONSTRUCTOR
 * building a valid clause with a time stamp = original time stamp + NumVar in clause
 */
class ClauseInfo protected(theClause : Struct, theHead : Struct, theBody : SubGoalTree, theLib : String) {
  /**
   * referring clause
   */
  protected val clause: Struct = theClause
  /**
   * head of clause
   */
  protected val head: Struct = theHead

  /**
   * body of clause
   */
  protected val body: SubGoalTree = theBody

  /**
   * if the clause is part of a theory in a lib (null if not)
   */
  private val libName: String = theLib

  private var headCopy: Struct = null

  private var bodyCopy: SubGoalTree = null

  def this (theClause : Struct, theLib : String){
    this(theClause,ClauseInfo.extractHead(theClause),ClauseInfo.extractBody(theClause.getArg(1)),theLib)
  }

  protected[gospel] def getLib = libName

  protected[gospel] def getHead= head

  protected[gospel] def getBody =body

  protected[gospel] def getClause = clause

  private[gospel] def performCopy(theExecCtx: Int) = {
    val vars = new ju.IdentityHashMap[Var, Var]
    headCopy =head.copy(vars, theExecCtx).asInstanceOf[Struct]
    bodyCopy = bodyCopy(body,  new SubGoalTree, vars, theExecCtx)
    this
  }

  private def bodyCopy(theSource: SubGoalTree, theDest: SubGoalTree, theVars: ju.AbstractMap[Var, Var], theId: Int): SubGoalTree = {
    for (node <- theSource) {
      if (node.isLeaf) {
        val leaf = node.asInstanceOf[SubGoalLeaf]
        val leafTerm: Term = leaf.getValue.copy(theVars, theId)
        theDest.addLeaf(leafTerm)
      }
      else {
        bodyCopy(node.asInstanceOf[SubGoalTree], theDest.addBranch, theVars, theId)
      }
    }
    theDest
  }
  protected[gospel] def getHeadCopy: Struct = {
     headCopy
  }

  protected[gospel] def getBodyCopy: SubGoalTree = {
    bodyCopy
  }

  /**
   * Gets the string representation with default operator representation
   */
  override def toString: String = {
    clause.getArg(0).toString + " :-\n\t" + ClauseInfo.indentPredicates(clause.getArg(1)) + ".\n"
  }

  /**
   * Gets the string representation
   * recognizing operators stored by
   * the operator manager
   */
  def toString(op: OperatorManager): String = {
    var p: scala.Int = 0
    if ({p = op.opPrio(":-", "xfx"); p} >= OperatorManager.OP_LOW) {
      val st: String = ClauseInfo.indentPredicatesAsArgX(clause.getArg(1), op, p)
      val head: String = clause.getArg(0).toStringAsArgX(op, p)
      if (st == "true") {
        return head + ".\n"
      }
      else {
        return head + " :-\n\t" + st + ".\n"
      }
    }
    if ({p = op.opPrio(":-", "yfx"); p} >= OperatorManager.OP_LOW) {
      val st: String = ClauseInfo.indentPredicatesAsArgX(clause.getArg(1), op, p)
      val head: String = clause.getArg(0).toStringAsArgY(op, p)
      if (st == "true") {
        return head + ".\n"
      }
      else {
        return head + " :-\n\t" + st + ".\n"
      }
    }
    if ({p = op.opPrio(":-", "xfy"); p} >= OperatorManager.OP_LOW) {
      val st: String = ClauseInfo.indentPredicatesAsArgY(clause.getArg(1), op, p)
      val head: String = clause.getArg(0).toStringAsArgX(op, p)
      if (st == "true") {
        return head + ".\n"
      }
      else {
        return head + " :-\n\t" + st + ".\n"
      }
    }
    clause.toString()
  }
}