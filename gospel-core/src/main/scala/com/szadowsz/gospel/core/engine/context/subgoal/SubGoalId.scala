package com.szadowsz.gospel.core.engine.context.subgoal

import com.szadowsz.gospel.core.engine.context.subgoal.tree.SubGoalTree

/** Identifier Class for a single sub-Goal during the demonstration.
 *
 * @author Alex Benini
 *
 */
private[core] class SubGoalId(par: SubGoalId, rt: SubGoalTree, i: Int){
  private val root: SubGoalTree = rt
  private val index: Int = i
  private val parent: SubGoalId = par

  def getParent: SubGoalId = parent

  def getRoot: SubGoalTree = root

  def getIndex: Int = index

  override def toString: String = {
    val builder = new StringBuilder("[index: "+index)
    builder.append(" value: " + (if (root.size > index) root.getChild(index) else "At End"))
    builder.append("]")
    builder.toString()
  }
}