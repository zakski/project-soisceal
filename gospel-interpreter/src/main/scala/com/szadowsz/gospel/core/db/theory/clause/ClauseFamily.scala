/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  *
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

import com.szadowsz.gospel.core.data.Struct

import scala.collection.mutable.ArrayBuffer

private[theory] class ClauseFamily extends Iterable[Clause] {

  protected var internalList: ArrayBuffer[Clause] = ArrayBuffer[Clause]()

  override def iterator: Iterator[Clause] = internalList.iterator

  /**
    * Updates indexes, storing information about the last added clause
    *
    * @param ci
    * @param first
    */
  private def register(ci: Clause, first: Boolean): Unit = {
    // See FamilyClausesList.get(Term): same concept
    val clause = ci.getHead
    val g: Struct = clause.getBinding.asInstanceOf[Struct]
//    if (g.getArity == 0) {
//      return
//    }
//    g.getArg(0).getTerm match {
//      case v: Var =>
//        numCompClausesIndex.insertAsShared(ci, first)
//        constantCompClausesIndex.insertAsShared(ci, first)
//        structCompClausesIndex.insertAsShared(ci, first)
//        if (first) {
//          listCompClausesList.addFirst(ci)
//        } else {
//          listCompClausesList.addLast(ci)
//        }
//      case t if t.isAtomic =>
//        t match {
//          case n: Number => numCompClausesIndex.insert(n, ci, first)
//          case s: Struct => constantCompClausesIndex.insert(s.getName, ci, first)
//          case _ =>
//        }
//      case s: Struct =>
//        if (isAList(s)) {
//          if (first) {
//            listCompClausesList.addFirst(ci)
//          } else {
//            listCompClausesList.addLast(ci)
//          }
//        } else {
//          structCompClausesIndex.insert(s.getPredicateIndicator, ci, first)
//        }
//      case _ =>
//    }
  }

  def addFirst(clause: Clause): Unit = {
    internalList.prepend(clause)
    register(clause, true)
  }

  def addLast(clause: Clause): Unit = {
    internalList += clause
    register(clause, false)
  }

  def remove(clause: Clause): Unit = {
    internalList = internalList - clause
  }
}
