package com.szadowsz.gospel.core.engine.context

import com.szadowsz.gospel.core.engine.clause.ClauseInfo
import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.engine.subgoal.tree.SubGoalTree


private[engine] class GoalContext private(theClause : Struct, theBody : SubGoalTree) extends ClauseInfo(theClause, null, theBody, null){

  def this(theClause : Struct, theBody : Struct){
    this(theClause,ClauseInfo.extractBody(theBody))
  }

  override protected[gospel] def getHeadCopy: Struct = {
    null
  }

  override protected[gospel] def getBodyCopy: SubGoalTree = {
    body
  }
}
