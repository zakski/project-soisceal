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
package com.szadowsz.gospel.core.db.theory.clause

import java.util

import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.Executor
import com.szadowsz.gospel.core.engine.context.goal.tree.{SubGoalBranch, SubGoalLeaf}

import scala.collection.JavaConverters._

object Clause {
  
  private def isCommaSeparator(body: Term) = {
    body.isInstanceOf[Struct] && body.asInstanceOf[Struct].getName == ","
  }
  
  /**
    * Gets the first argument from a struct
    *
    * @param clause the clause to extract the head argument from
    * @return the head of the clause
    */
  private def extractHead(clause: Struct): Struct = {
    clause(0).asInstanceOf[Struct]
  }
  
  /**
    * Extracts a series of comma separated sub goals from the demonstration's query term
    *
    * @param body the body of the query the demonstration is seeking to answer
    * @return a tree of all query goals, representing all subgoals as branch and leaf nodes
    */
  private[core] def extractBody(body: Term): SubGoalBranch = {
    extractBody(new SubGoalBranch, body)
  }
  
  /**
    * Extracts a series of comma separated sub goals from the demonstration's query term, recursively
    *
    * @param parent the root branch of the current tree
    * @param body   the body of the query the demonstration is seeking to answer
    * @return a tree of all query goals, representing all subgoals as branch and leaf nodes
    */
  private def extractBody(parent: SubGoalBranch, body: Term): SubGoalBranch = {
    body match {
      case struct: Struct =>
        if (!isCommaSeparator(struct)) {
          parent.addLeaf(struct)
        } else {
          val t = struct(0)
          extractBody(if (!isCommaSeparator(t)) parent else parent.addBranch(), t)
          extractBody(parent, struct(1))
        }
      case _ => parent.addLeaf(body)
    }
    parent
  }
  
}

/**
  * This class maintains information about a clause creation (clause copy, final time T after renaming, validity,
  * stillValid Flag). This information ire to the Theory Manager to use the clause in a consistent way.
  *
  * @constructor building a valid clause with a time stamp = original time stamp + NumVar in clause
  * @param clause
  * @param head
  * @param libName
  */
private[core] case class Clause protected(clause: Struct, head: Struct, body: SubGoalBranch, libName: Option[String]) {
  
  def this(clause: Struct, lib: Option[String]) {
    this(clause, Clause.extractHead(clause), Clause.extractBody(clause(1)), lib)
  }
  
  def getLib: Option[String] = libName
  
  def performCopy(e : Executor, ExecCtx: Int) : Clause = {
    val vars = new util.IdentityHashMap[Var, Var]
    copy(clause, head.copy(e, vars, ExecCtx).asInstanceOf[Struct], bodyCopy(e, body, new SubGoalBranch, vars, ExecCtx), libName)
  }
  
  private def bodyCopy(e : Executor, source: SubGoalBranch, dest: SubGoalBranch, vars: util.AbstractMap[Var, Var], id: Int): SubGoalBranch = {
    for (node <- source.iterator.asScala) {
      if (node.isLeaf) {
        val leaf = node.asInstanceOf[SubGoalLeaf]
        val leafTerm: Term = leaf.term.copy(e, vars, id)
        dest.addLeaf(leafTerm)
      } else {
        bodyCopy(e, node.asInstanceOf[SubGoalBranch], dest.addBranch(), vars, id)
      }
    }
    dest
  }
}
