package com.szadowsz.gospel.core.engine.context.subgoal.tree

import com.szadowsz.gospel.core.data.Term

private[core] class SubGoalLeaf(t: Term) extends SubGoalNode {
  private val term: Term = t

  def getValue: Term = term

  def isLeaf: Boolean = true
  

  def isRoot: Boolean = false

  override def toString: String = term.toString
}