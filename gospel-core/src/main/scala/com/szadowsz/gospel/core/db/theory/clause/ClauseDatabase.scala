/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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

import java.lang.Iterable
import java.{util => ju}

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.engine.context.clause.ClauseInfo

import scala.collection.JavaConverters._

/**
  * Customized HashMap for storing clauses in the TheoryManager
  *
  * @author ivar.orstavik@hist.no
  *
  *         Reviewed by Paolo Contessi
  */
@SerialVersionUID(1L)
class ClauseDatabase extends ju.HashMap[String, FamilyClausesList] with Iterable[ClauseInfo] {

  def iterator: ju.Iterator[ClauseInfo] = {
    new CompleteIterator(this)
  }

  private[gospel] def addFirst(key: String, d: ClauseInfo) {
    var family: FamilyClausesList = get(key)
    if (family == null) {
      family = new FamilyClausesList
      put(key, family)
    }
    family.addFirst(d)
  }

  private[gospel] def addLast(key: String, d: ClauseInfo) {
    var family: FamilyClausesList = get(key)
    if (family == null) {
      family = new FamilyClausesList
      put(key, family)
    }
    family.addLast(d)
  }

  private[gospel] def abolish(key: String): FamilyClausesList = {
    remove(key)
  }

  /**
    * Retrieves a list of the predicates which has the same name and arity
    * as the goal and which has a compatible first-arg for matching.
    *
    * @param headt The goal
    * @return The list of matching-compatible predicates
    */
  private[gospel] def getPredicates(headt: Term): ju.List[ClauseInfo] = {
    val family: FamilyClausesList = get((headt.asInstanceOf[Struct]).getPredicateIndicator)
    if (family == null) {
      return new ju.ArrayList[ClauseInfo]()
    }
    family.get(headt)
  }

  /**
    * Retrieves the list of clauses of the requested family
    *
    * @param key Goal's Predicate Indicator
    * @return The family clauses
    */
  private[gospel] def getPredicates(key: String): List[ClauseInfo] = {
    val family: FamilyClausesList = get(key)
    if (family == null) {
      return List[ClauseInfo]()
    }
    family.asScala.toList ::: List[ClauseInfo]()
  }

  private class CompleteIterator(clauseDatabase: ClauseDatabase) extends ju.Iterator[ClauseInfo] {
    private[gospel] val values: ju.Iterator[FamilyClausesList] = clauseDatabase.values.iterator
    private[gospel] var workingList: ju.Iterator[ClauseInfo] = _

    def hasNext: Boolean = {
      if (workingList != null && workingList.hasNext) return true
      if (values.hasNext) {
        workingList = values.next.iterator
        return hasNext //start again on next workingList
      }
      false
    }

    def next: ClauseInfo = {
      if (workingList.hasNext) workingList.next
      else null
    }

    override def remove {
      workingList.remove
    }
  }
}