package com.szadowsz.gospel.core.engine.context.subgoal.tree

import alice.tuprolog.Term

import scala.collection.mutable
import scala.collection.JavaConverters._

private[core] class SubGoalTree(ts: mutable.MutableList[SubGoalNode]) extends SubGoalNode with java.lang.Iterable[SubGoalNode] {
  private val terms: mutable.MutableList[SubGoalNode] = ts

  def this() {
    this(new mutable.MutableList[SubGoalNode])
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