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
package com.szadowsz.gospel.core.parser

import java.io.Serializable

import alice.tuprolog.Tokenizer

/**
  * This class represents a token read by the prolog term tokenizer
  *
  * Created on 19/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
private[parser] class Token(theSeq: String, theType: Int) extends Serializable {
  var seq: String = theSeq

  private val _type: Int = theType

  def getType: Int = _type & Tokenizer.TYPEMASK

  /**
    * attribute could be EOF or ERROR
    */
  def getAttribute: Int = _type & Tokenizer.ATTRMASK

  def getValue: String = seq

  def isOperator(commaIsEndMarker: Boolean): Boolean = if (commaIsEndMarker && ("," == seq)) false else getAttribute == Tokenizer.OPERATOR

  def isFunctor: Boolean = getAttribute == Tokenizer.FUNCTOR

  def isNumber: Boolean = _type == Tokenizer.INTEGER || _type == Tokenizer.FLOAT

  def isEOF: Boolean = getAttribute == Tokenizer.EOF

  def isType(theType: Int): Boolean = getType == theType
}