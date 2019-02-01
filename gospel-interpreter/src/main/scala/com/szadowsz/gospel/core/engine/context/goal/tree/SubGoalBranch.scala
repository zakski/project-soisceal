/**
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
package com.szadowsz.gospel.core.engine.context.goal.tree

import com.szadowsz.gospel.core.data.Term

import scala.collection.JavaConverters._
import scala.collection.mutable

private[core] class SubGoalBranch(ts: mutable.MutableList[SubGoalNode]) extends SubGoalNode with java.lang.Iterable[SubGoalNode] {
  private val terms: mutable.MutableList[SubGoalNode] = ts

  def this() {
    this(new mutable.MutableList[SubGoalNode])
  }

  def addLeaf(term: Term): SubGoalLeaf = {
    terms += SubGoalLeaf(term)
    terms.last.asInstanceOf[SubGoalLeaf]
  }

  def addBranch(): SubGoalBranch = {
    terms += new SubGoalBranch()
    terms.last.asInstanceOf[SubGoalBranch]
  }

  def getChild(i: Int): SubGoalNode = terms(i)

  override def iterator: java.util.Iterator[SubGoalNode] = terms.iterator.asJava // TODO change to scala

  def size: Int = terms.length

  override def isLeaf: Boolean = false

  override def toString: String = terms.mkString(" [ "," , "," ] ")
}