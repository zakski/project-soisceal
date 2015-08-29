package com.szadowsz.gospel.core.engine.subgoal.tree

private[core]trait SubGoalNode {
  def isLeaf: Boolean

  def isRoot: Boolean
}