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
package com.szadowsz.gospel.core.db.operators


object Operator {

  def apply(name: String, opType: String, prio: Int): Operator = new Operator(name, OpType.valueOf(opType.toUpperCase), prio)
}
/**
  * This class defines a Prolog operator, in terms of a name, a type, and a  priority.
  *
  * @param name operator name
  * @param opType (xf,yf,fx,fy,xfx,xfy,yfy,yfx)
  * @param prio priority
  */
final case class Operator (name: String, opType: OpType, prio: Int) extends Serializable