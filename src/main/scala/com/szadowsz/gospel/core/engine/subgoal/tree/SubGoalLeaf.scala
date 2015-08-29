package com.szadowsz.gospel.core.engine.subgoal.tree

import com.szadowsz.gospel.core.data.Term

private[core] class SubGoalLeaf(t: Term) extends SubGoalNode {
  private val _term: Term = t

  def getValue: Term = _term

  def isLeaf: Boolean = true
  

  def isRoot: Boolean = false

  override def toString: String = _term.toString
}