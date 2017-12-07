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

import java.{util => ju}

import com.szadowsz.gospel.core.data.{Number, Struct, Term, Var}
import com.szadowsz.gospel.core.engine.context.clause.ClauseInfo


/**
  * <code>FamilyClausesList</code> is a common <code>LinkedList</code>
  * which stores {@link ClauseInfo} objects. Internally it indexes stored data
  * in such a way that, knowing what type of clauses are required, only
  * goal compatible clauses are returned
  *
  * @author Paolo Contessi
  * @since 2.2
  * @see LinkedList
  */
@SerialVersionUID(1L)
private[theory] class FamilyClausesList extends ju.LinkedList[ClauseInfo] {
  private val numCompClausesIndex = new FamilyClausesIndex[Number]
  private val constantCompClausesIndex = new FamilyClausesIndex[String]
  private val structCompClausesIndex = new FamilyClausesIndex[String]
  private val listCompClausesList = new ju.LinkedList[ClauseInfo]

  /**
    * Adds the given clause as first of the family
    *
    * @param ci The clause to be added (with related informations)
    */
  override def addFirst(ci: ClauseInfo): Unit = {
    super.addFirst(ci)
    register(ci, true)
  }

  // Updates indexes, storing informations about the last added clause
  private def register(ci: ClauseInfo, first: Boolean): Unit = {
    // See FamilyClausesList.get(Term): same concept
    val clause = ci.getHead
    if (clause.isInstanceOf[Struct]) {
      val g: Struct = clause.getTerm.asInstanceOf[Struct]
      if (g.getArity == 0) {
        return
      }
      g.getArg(0).getTerm match {
        case v: Var =>
          numCompClausesIndex.insertAsShared(ci, first)
          constantCompClausesIndex.insertAsShared(ci, first)
          structCompClausesIndex.insertAsShared(ci, first)
          if (first) {
            listCompClausesList.addFirst(ci)
          } else {
            listCompClausesList.addLast(ci)
          }
        case t if t.isAtomic =>
          t match {
            case n: Number => numCompClausesIndex.insert(n, ci, first)
            case s: Struct => constantCompClausesIndex.insert(s.getName, ci, first)
            case _ =>
          }
        case s: Struct =>
          if (isAList(s)) {
            if (first) {
              listCompClausesList.addFirst(ci)
            } else {
              listCompClausesList.addLast(ci)
            }
          } else {
            structCompClausesIndex.insert(s.getPredicateIndicator, ci, first)
          }
        case _ =>
      }
    }
  }

  /*
   * Checks if a Struct is also a list.
   * A list can be an empty list, or a Struct with name equals to "."
   * and arity equals to 2.
   */
  private def isAList(t: Struct): Boolean = t.isEmptyList || ((t.getName == ".") && t.getArity == 2)

  override def add(o: ClauseInfo): Boolean = {
    addLast(o)
    true
  }

  /**
    * Adds the given clause as last of the family
    *
    * @param ci The clause to be added (with related informations)
    */
  override def addLast(ci: ClauseInfo) {
    super.addLast(ci)
    register(ci, false)
  }

  @deprecated
  override def addAll(index: scala.Int, c: ju.Collection[_ <: ClauseInfo]): Boolean = {
    throw new UnsupportedOperationException("Not supported.")
  }

  @deprecated
  override def add(index: scala.Int, element: ClauseInfo) {
    throw new UnsupportedOperationException("Not supported.")
  }

  @deprecated
  override def set(index: scala.Int, element: ClauseInfo): ClauseInfo = {
    throw new UnsupportedOperationException("Not supported.")
  }

  override def removeLast: ClauseInfo = {
    val ci: ClauseInfo = getLast
    if (remove(ci)) {
      ci
    } else {
      null
    }
  }

  override def remove: ClauseInfo = {
    removeFirst
  }

  override def removeFirst: ClauseInfo = {
    val ci: ClauseInfo = getFirst
    if (remove(ci)) {
      ci
    } else {
      null
    }
  }

  override def remove(index: scala.Int): ClauseInfo = {
    val ci: ClauseInfo = super.get(index)
    if (remove(ci)) {
      ci
    } else {
      null
    }
  }

  override def remove(ci: AnyRef): Boolean = {
    if (super.remove(ci.asInstanceOf[ClauseInfo])) {
      unregister(ci.asInstanceOf[ClauseInfo])
      true
    } else {
      false
    }
  }

  // Updates indexes, deleting informations about the last removed clause
  def unregister(ci: ClauseInfo) {
    val clause: Term = ci.getHead
    if (clause.isInstanceOf[Struct]) {
      val g: Struct = clause.getTerm.asInstanceOf[Struct]
      if (g.getArity == 0) {
        return
      }
      val t: Term = g.getArg(0).getTerm
      if (t.isInstanceOf[Var]) {
        numCompClausesIndex.removeShared(ci)
        constantCompClausesIndex.removeShared(ci)
        structCompClausesIndex.removeShared(ci)
        listCompClausesList.remove(ci)
      }
      else if (t.isAtomic) {
        if (t.isInstanceOf[Number]) {
          numCompClausesIndex.remove(t.asInstanceOf[Number], ci)
        }
        else if (t.isInstanceOf[Struct]) {
          constantCompClausesIndex.remove(t.asInstanceOf[Struct].getName, ci)
        }
      }
      else if (t.isInstanceOf[Struct]) {
        if (t.isList) {
          listCompClausesList.remove(ci)
        }
        else {
          structCompClausesIndex.remove(t.asInstanceOf[Struct].getPredicateIndicator, ci)
        }
      }
    }
  }

  override def clear() {
    while (size > 0) {
      removeFirst
    }
  }

  /**
    * Retrieves a sublist of all the clauses of the same family as the goal
    * and which, in all probability, could match with the given goal
    *
    * @param goal The goal to be resolved
    * @return The list of goal-compatible predicates
    */
  def get(goal: Term): ju.List[ClauseInfo] = {
    // Gets the correct list and encapsulates it in ReadOnlyLinkedList
    if (goal.isInstanceOf[Struct]) {
      val g: Struct = goal.getTerm.asInstanceOf[Struct]

      /*
      * If no arguments no optimization can be applied
      * (and probably no optimization is needed)
      */
      if (g.getArity == 0) {
        return this //.asScala.toList ::: List[ClauseInfo]()
      }

      /* Retrieves first argument and checks type */

      val t = g.getArg(0).getTerm

      if (t.isInstanceOf[Var]) {
        /*
         * if first argument is an unbounded variable,
         * no reasoning is possible, all family must be returned
         */
        this //.asScala.toList ::: List[ClauseInfo]()

      } else if (t.isAtomic) {
        if (t.isInstanceOf[Number]) {
          /* retrieves clauses whose first argument is numeric (or Var)
           * and same as goal's first argument, if no clauses
           * are retrieved, all clauses with a variable
           * as first argument
           */
          numCompClausesIndex.get(t.asInstanceOf[Number])
        } else {
          /* retrieves clauses whose first argument is a constant (or Var)
           * and same as goal's first argument, if no clauses
           * are retrieved, all clauses with a variable
           * as first argument
           */
          constantCompClausesIndex.get(t.asInstanceOf[Struct].getName)
        }
      } else if (t.isInstanceOf[Struct]) {
        if (isAList(t.asInstanceOf[Struct])) {
          /* retrieves clauses which has a list  (or Var) as first argument */
          listCompClausesList //.asScala.toList ::: List[ClauseInfo]()
        } else {
          /* retrieves clauses whose first argument is a struct (or Var)
           * and same as goal's first argument, if no clauses
           * are retrieved, all clauses with a variable
           * as first argument
           */
          structCompClausesIndex.get(t.asInstanceOf[Struct].getPredicateIndicator)
        }
      } else {
        /* Default behaviour: no optimization done */
        this //.asScala.toList ::: List[ClauseInfo]()
      }
    } else {
      /* Default behaviour: no optimization done */
      this //.asScala.toList ::: List[ClauseInfo]()
    }
  }

  override def iterator: ju.Iterator[ClauseInfo] = {
    listIterator(0)
  }

  override def listIterator(index: scala.Int): ju.ListIterator[ClauseInfo] = {
    new ListItr(this, index)
  }

  override def listIterator: ju.ListIterator[ClauseInfo] = {
    new ListItr(this, 0)
  }

  private def superListIterator(index: scala.Int): ju.ListIterator[ClauseInfo] = {
    super.listIterator(index)
  }

  private class ListItr(list: FamilyClausesList, index: scala.Int) extends ju.ListIterator[ClauseInfo] {
    private val it: ju.ListIterator[ClauseInfo] = list.superListIterator(index)
    private val l: ju.LinkedList[ClauseInfo] = list
    private var currentIndex: scala.Int = 0

    override def hasNext: Boolean = {
      it.hasNext
    }

    override def next: ClauseInfo = {
      // Alessandro Montanari - alessandro.montanar5@studio.unibo.it
      currentIndex = it.nextIndex
      it.next
    }

    override def hasPrevious: Boolean = {
      it.hasPrevious
    }

    override def previous: ClauseInfo = {
      // Alessandro Montanari - alessandro.montanar5@studio.unibo.it
      currentIndex = it.previousIndex
      it.previous
    }

    override def nextIndex: scala.Int = {
      it.nextIndex
    }

    override def previousIndex: scala.Int = {
      it.previousIndex
    }

    override def remove() {
      // Alessandro Montanari - alessandro.montanar5@studio.unibo.it
      val ci: ClauseInfo = l.get(currentIndex)
      it.remove()
      unregister(ci)
    }

    override def set(o: ClauseInfo) {
      it.set(o)
    }

    override def add(o: ClauseInfo) {
      l.addLast(o)
    }
  }
}