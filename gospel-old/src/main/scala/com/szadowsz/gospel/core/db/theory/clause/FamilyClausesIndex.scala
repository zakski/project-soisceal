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

import com.szadowsz.gospel.core.engine.context.clause.ClauseInfo

import scala.collection.immutable.TreeMap


/**
  * <code>FamilyClausesIndex</code> enables family clauses indexing
  * in {@link alice.gospel.core.theory.ClauseDatabase}.
  *
  * @author Paolo Contessi
  * @since 2.2
  */
private[clause] final class FamilyClausesIndex[K <: Comparable[K]] {
  private val varsClauses: ju.LinkedList[ClauseInfo] = new ju.LinkedList[ClauseInfo]
  private var root: TreeMap[K, ju.LinkedList[ClauseInfo]] = TreeMap()

  private def createNewNode(key: K, clause: ClauseInfo, first: Boolean): Unit = {
    val list: ju.LinkedList[ClauseInfo] = new ju.LinkedList[ClauseInfo](varsClauses)
    if (first) list.addFirst(clause) else list.addLast(clause)
    root = root + (key -> list)
  }

  /*
   * I want to store a reference to the clause in the same order that they
   *
   * If the key does not exist
   * Add a new node
   */
  def insertAsShared(clause: ClauseInfo, first: Boolean): Unit = {
    if (first) {
      varsClauses.addFirst(clause)
    } else {
      varsClauses.addLast(clause)
    }
    root.foreach { case (k, v) => if (first) v.addFirst(clause) else v.addLast(clause) }
  }

  /**
    * Creates a new entry (<code>key</code>) in the index, relative to the
    * given <code>clause</code>. If other clauses is associated to <code>key</code>
    * <code>first</code> parameter is used to decide if it is the first or
    * the last clause to be retrieved.
    *
    * @param key    The key of the index
    * @param clause The value to be binded to the given key
    * @param first  If the clause must be binded as first or last element
    */
  def insert(key: K, clause: ClauseInfo, first: Boolean): Unit = {
    root.get(key) match {
      case Some(v) => if (first) v.addFirst(clause) else v.addLast(clause)
      case None =>
        createNewNode(key, clause, first)
    }
  }

  def removeShared(clause: ClauseInfo): Unit = {
    if (varsClauses.remove(clause)) {
      root.foreach { case (k, v) => v.remove(clause) }
    } else {
      throw new IllegalArgumentException("Invalid clause: not registered in this index")
    }
  }

  def remove(key: K, c: ClauseInfo): Unit = {
    root.get(key) match {
      case None =>
      case Some(v) =>
        if (v.contains(c) && v.size > 1) {
          v.remove(c)
        } else if (v.contains(c)) {
          root = root - key
        }
    }
  }

  /**
    * Retrieves all the clauses related to the key
    *
    * @param key The key
    * @return The related clauses
    */
  def get(key: K): ju.LinkedList[ClauseInfo] = root.getOrElse(key, varsClauses)
}