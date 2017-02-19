/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.core


import java.io.{IOException, InputStream}
import java.util

import alice.tuprolog.interfaces.ITheory
import alice.tuprolog.json.JSONSerializerManager
import alice.tuprolog.{InvalidTheoryException, Prolog, Struct, Term}
import com.szadowsz.gospel.core.parser.Parser

/**
  * This class represents prolog theory which can be provided to a prolog engine.
  *
  * Actually theory incapsulates only textual representation of prolog theories, without doing any check about validity.
  *
  * Created on 19/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
class Theory private() extends ITheory {
  private var theory: String = ""
  private var clauseList: Struct = _

  /**
    * Creates a theory getting its source text from an input stream
    *
    * @param is the input stream acting as source
    */
  @throws(classOf[IOException])
  def this(is: InputStream) {
    this()
    val info: Array[Byte] = new Array[Byte](is.available)
    is.read(info)
    theory = new String(info)
  }

  /**
    * Creates a theory from its source text
    *
    * @param theory the source text
    * @throws InvalidTheoryException if theory is null
    */
  @throws(classOf[InvalidTheoryException])
  def this(theory: String) {
    this()
    if (theory == null) {
      throw new InvalidTheoryException
    }
    this.theory = theory
  }

  /**
    * Creates a theory from a clause list
    *
    * @param clauseList the source text
    * @throws InvalidTheoryException if clauseList is null or is not a prolog list
    */
  @throws(classOf[InvalidTheoryException])
  def this(clauseList: Struct) {
    this()
    theory = null
    if (clauseList == null || !clauseList.isList) {
      throw new InvalidTheoryException
    }
    this.clauseList = clauseList
  }

  def iterator(engine: Prolog): util.Iterator[_ <: Term] = {
    if (isTextual)
      new Parser(engine.getOperatorManager, theory).iterator
    else
      clauseList.listIterator
  }

  /**
    * Adds (appends) a theory to this.
    *
    * @param th is the theory to be appended
    * @throws InvalidTheoryException if the theory object are not compatibles (they are compatibles when both have been built from texts or both from clause
    *                                lists)
    */
  @throws(classOf[InvalidTheoryException])
  def append(th: ITheory) {
    if (th.isTextual && isTextual) {
      theory += th.toString
    } else if (!th.isTextual && !isTextual) {
      val otherClauseList: Struct = th.getClauseListRepresentation
      if (clauseList.isEmptyList) {
        clauseList = otherClauseList
      } else {
        var p = clauseList
        var q: Struct = p.getArg(1).asInstanceOf[Struct]
        while (!q.isEmptyList) {
          p = q
          q = p.getArg(1).asInstanceOf[Struct]
        }
        p.setArg(1, otherClauseList)

      }
    } else if (!isTextual && th.isTextual) {
      theory = theory.toString + "\n" + th
      clauseList = null

    } else if (isTextual && !th.isTextual) {
      theory += th.toString

    } else {
      throw new InvalidTheoryException
    }
  }

  /**
    * Checks if the theory has been built from a text or a clause list
    *
    */
  private[gospel] def isTextual: Boolean = {
    theory != null
  }

  private[gospel] def getClauseListRepresentation: Struct = {
    clauseList
  }

  override def toString: String = {
    if (theory != null) theory else clauseList.toString
  }

  override def toJSON: String = JSONSerializerManager.toJSON(this)
}