package com.szadowsz.gospel.core.engine.context.clause

import alice.tuprolog.Struct
import com.szadowsz.gospel.core.engine.context.subgoal.tree.SubGoalTree


private[engine] class GoalClause private(c : Struct, b : SubGoalTree) extends ClauseInfo(c, null, b, null){

  def this(c : Struct, b : Struct){
    this(c,ClauseInfo.extractBody(b))
  }

  override protected[gospel] def getHeadCopy: Struct = {
    null
  }

  override protected[gospel] def getBodyCopy: SubGoalTree = {
    body
  }
}
