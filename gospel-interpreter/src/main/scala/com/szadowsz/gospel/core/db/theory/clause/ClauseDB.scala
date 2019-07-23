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
package com.szadowsz.gospel.core.db.theory.clause

import com.szadowsz.gospel.core.data.{Struct, Term}

import scala.collection.concurrent
import scala.collection.immutable.List

private[theory] class ClauseDB extends Iterable[Clause] {
  // scalastyle:off method.name

  private val internalMap: concurrent.Map[String, ClauseFamily] = concurrent.TrieMap()

  def apply(key : String) : List[Clause] = get(key) match {
    case None => null
    case Some(value) => value
  }

  def apply(headT : Term) : List[Clause] = get(headT) match {
    case None => Nil
    case Some(value) => value
  }

  def get(key : String) : Option[List[Clause]] = {
    internalMap.get(key).map(_.toList)
  }

  def get(headT : Term) : Option[List[Clause]] = get(headT.asInstanceOf[Struct].getPredicateIndicator)

  def contains(key : String):Boolean = internalMap.contains(key)

  def abolish(key: String): Option[ClauseFamily] = internalMap.remove(key)

  def +:=(kv : (String,Clause)): ClauseDB = {
    val family = internalMap.getOrElseUpdate(kv._1,new ClauseFamily)
    family.addFirst(kv._2)
    this
  }

  def :+=(kv : (String,Clause)): ClauseDB = {
    val family = internalMap.getOrElseUpdate(kv._1,new ClauseFamily)
    family.addLast(kv._2)
    this
  }

  def remove(clause: Clause): Unit = {
    internalMap.get(clause.head.getPredicateIndicator).foreach(f => f.remove(clause))
  }
  
  def clear() : Unit = {
    internalMap.clear()
  }

  override def iterator: Iterator[Clause] = new Iterator[Clause]() {
    private val values: Iterator[ClauseFamily] = internalMap.values.iterator
    private var workingList: Iterator[Clause] = _

    def hasNext: Boolean = {
      if (workingList != null && workingList.hasNext) {
        true
      } else if (values.hasNext) {
        workingList = values.next.iterator
        hasNext //start again on next workingList
      } else {
        false
      }
    }

    def next: Clause = {
      if (workingList.hasNext) {
        workingList.next
      } else {
        null
      }
    }
  }
}
