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
package com.szadowsz.gospel.core.parser

/**
  * This class represents a token read by the prolog term tokenizer
  *
  */
private[parser] case class TermToken(seq: String, tokenType: Int) extends Serializable {

  def getType: Int = tokenType & TermTokenizer.TYPEMASK

  /**
    * attribute could be EOF or ERROR
    */
  def getAttribute: Int = tokenType & TermTokenizer.ATTRMASK

  def getValue: String = seq

  def isOperator(commaIsEndMarker: Boolean): Boolean = if (commaIsEndMarker && ("," == seq)) false else getAttribute == TermTokenizer.OPERATOR

  def isFunctor: Boolean = getAttribute == TermTokenizer.FUNCTOR

  def isNumber: Boolean = tokenType == TermTokenizer.INTEGER || tokenType == TermTokenizer.FLOAT

  def isEOF: Boolean = getAttribute == TermTokenizer.EOF

  def isType(theType: Int): Boolean = getType == theType
}