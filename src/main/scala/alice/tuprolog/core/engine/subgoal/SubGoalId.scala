package alice.tuprolog.core.engine.subgoal

import alice.tuprolog.core.engine.subgoal.tree.SubGoalTree

/** Identifier Class for a single sub-Goal during the demonstration.
 * @author Alex Benini
 *
 */
private[engine] class SubGoalId(parent: SubGoalId, root: SubGoalTree, index: Int){
  private val _root: SubGoalTree = root
  private val _index: Int = index
  private val _parent: SubGoalId = parent

  def getParent: SubGoalId = _parent

  def getRoot: SubGoalTree = _root

  def getIndex: Int = _index

  override def toString: String = {
    val builder = new StringBuilder("[index: "+_index)
    builder.append(" value: " + (if (_root.size > _index) _root.getChild(_index) else "At End"))
    builder.append("]")
    builder.toString()
  }
}