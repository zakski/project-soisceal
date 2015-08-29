package alice.tuprolog.core.engine.context

import alice.tuprolog.core.data.Struct
import alice.tuprolog.core.engine.subgoal.tree.SubGoalTree
import alice.tuprolog.core.theory.clause.ClauseInfo


private[engine] class GoalContext private(theClause : Struct, theBody : SubGoalTree) extends ClauseInfo(theClause, null, theBody, null){

  def this(theClause : Struct, theBody : Struct){
    this(theClause,ClauseInfo.extractBody(theBody))
  }

  override protected[tuprolog] def getHeadCopy: Struct = {
    null
  }

  override protected[tuprolog] def getBodyCopy: SubGoalTree = {
    body
  }
}
