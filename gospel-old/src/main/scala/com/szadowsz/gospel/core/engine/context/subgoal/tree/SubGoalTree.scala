package com.szadowsz.gospel.core.engine.context.subgoal.tree

import com.szadowsz.gospel.core.data.Term

import scala.collection.mutable
import scala.collection.JavaConverters._

private[core] class SubGoalTree(ts: mutable.ArrayDeque[SubGoalNode]) extends SubGoalNode with java.lang.Iterable[SubGoalNode] {
  private val terms: mutable.ArrayDeque[SubGoalNode] = ts

  def this() {
    this(new mutable.ArrayDeque[SubGoalNode])
  }

  def addLeaf(term: Term): SubGoalLeaf = {
    terms += new SubGoalLeaf(term)
    terms.last.asInstanceOf[SubGoalLeaf]
  }

  def addBranch(): SubGoalTree = {
    terms += new SubGoalTree()
    terms.last.asInstanceOf[SubGoalTree]
  }

  def getChild(i: Int): SubGoalNode = terms(i)

  override def iterator: java.util.Iterator[SubGoalNode] = terms.iterator.asJava // TODO change to scala

  def size: Int = terms.length

  override def isLeaf: Boolean = false

  override def isRoot: Boolean = true

  override def toString: String = terms.mkString(" [ "," , "," ] ")
}