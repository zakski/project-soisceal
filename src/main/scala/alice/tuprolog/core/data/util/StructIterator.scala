/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
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
package alice.tuprolog.core.data.util

import alice.tuprolog.core.data.{Struct, Term}

/**
 * This class represents an iterator through the arguments of a Struct list.
 *
 * @see Struct
 */
@SerialVersionUID(1L)
class StructIterator protected[tuprolog] (theStruct: Struct) extends Iterator[Term] with Serializable {
  private var _list = theStruct

  override def hasNext: Boolean = !_list.isEmptyList

  override def next(): Term = {
    if (_list.isEmptyList) {
      throw new NoSuchElementException()
    }

    // Using Struct#getTerm(int) instead of Struct#listHead and Struct#listTail
    // to avoid redundant Struct#isList calls since it is only possible to get
    // a StructIterator on a Struct instance which is already a list.
    val head = _list.getTerm(0)
    _list = _list.getTerm(1).asInstanceOf[Struct]
    head
  }
}