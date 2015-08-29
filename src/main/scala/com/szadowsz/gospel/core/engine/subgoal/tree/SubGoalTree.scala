package com.szadowsz.gospel.core.engine.subgoal.tree

import com.szadowsz.gospel.core.data.Term

import scala.collection.{mutable => m}


private[core] class SubGoalTree(terms: m.MutableList[SubGoalNode]) extends SubGoalNode with Iterable[SubGoalNode] {
  private val _terms: m.MutableList[SubGoalNode] = terms

  def this() {
    this(new m.MutableList[SubGoalNode])
  }

  def addLeaf(term: Term) = {
    _terms += new SubGoalLeaf(term)
    _terms.last.asInstanceOf[SubGoalLeaf]
  }

  def addBranch() = {
    _terms += new SubGoalTree()
    _terms.last.asInstanceOf[SubGoalTree]
  }

  def getChild(i: Int): SubGoalNode = _terms(i)

  override def iterator: Iterator[SubGoalNode] = _terms.iterator

  override def size: Int = _terms.length

  override def isLeaf: Boolean = false

  override def isRoot: Boolean = true

  override def toString(): String = {
     _terms.addString(new StringBuilder," [ "," , "," ] ").toString()
  }
}