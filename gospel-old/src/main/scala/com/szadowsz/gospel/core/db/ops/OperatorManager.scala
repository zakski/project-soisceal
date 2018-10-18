/*
 * tuProlog - Copyright (C) 2001-2006  aliCE team at deis.unibo.it
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
 *//*
 * tuProlog - Copyright (C) 2001-2006  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.core.db.ops

import java.io.Serializable
import java.util
import scala.collection.JavaConverters._

/**
  * This class manages Prolog operators.
  *
  * @see Operator
  */
@SerialVersionUID(1L)
object OperatorManager {
  /** lowest operator priority */
  val OP_LOW: Int = 1
  /** highest operator priority */
  val OP_HIGH: Int = 1200
}

@SerialVersionUID(1L)
class OperatorManager extends Serializable {

  /**
    * current known operators
    */
  private var operatorList = new OperatorRegister

  // standard defined operators
  opNew(":-", "xfx", 1200)
  opNew("-->", "xfx", 1200)
  opNew(":-", "fx", 1200)
  opNew("?-", "fx", 1200)
  opNew(";", "xfy", 1100)
  opNew("->", "xfy", 1050)
  opNew(",", "xfy", 1000)
  opNew("\\+", "fy", 900)
  opNew("not", "fy", 900)
  opNew("=", "xfx", 700)
  opNew("\\=", "xfx", 700)
  opNew("==", "xfx", 700)
  opNew("\\==", "xfx", 700)
  opNew("@>", "xfx", 700)
  opNew("@<", "xfx", 700)
  opNew("@=<", "xfx", 700)
  opNew("@>=", "xfx", 700)
  opNew("=:=", "xfx", 700)
  opNew("=\\=", "xfx", 700)
  opNew(">", "xfx", 700)
  opNew("<", "xfx", 700)
  opNew("=<", "xfx", 700)
  opNew(">=", "xfx", 700)
  opNew("is", "xfx", 700)
  opNew("=..", "xfx", 700)
  opNew("+", "yfx", 500)
  opNew("-", "yfx", 500)
  opNew("/\\", "yfx", 500)
  opNew("\\/", "yfx", 500)
  opNew("*", "yfx", 400)
  opNew("/", "yfx", 400)
  opNew("//", "yfx", 400)
  opNew(">>", "yfx", 400)
  opNew("<<", "yfx", 400)
  opNew("rem", "yfx", 400)
  opNew("mod", "yfx", 400)
  opNew("**", "xfx", 200)
  opNew("^", "xfy", 200)
  opNew("\\", "fx", 200)
  opNew("-", "fy", 200)

  /**
    * Creates a new operator. If the operator is already provided,
    * it replaces it with the new one
    */
  def opNew(name: String, `type`: String, prio: Int) {
    val op = Operator(name, `type`, prio)
    if (prio >= OperatorManager.OP_LOW && prio <= OperatorManager.OP_HIGH) operatorList.addOperator(op)
  }

  /**
    * Returns the priority of an operator (0 if the operator is not defined).
    */
  def opPrio(name: String, `type`: String): Int = {
    val o = operatorList.getOperator(name, `type`)
    if (o == null) 0 else o.prio
  }

  /**
    * Returns the priority nearest (lower) to the priority of a defined operator
    */
  def opNext(priority: Int): Int = {
    var nearest = 0
    for (opFromList <- operatorList.asScala) {
      if (opFromList.prio > nearest && opFromList.prio < priority) {
        nearest = opFromList.prio
      }
    }
    nearest
  }

  /**
    * Gets the list of the operators currently defined
    *
    * @return the list of the operators
    */
  def getOperators: util.List[Operator] = new util.LinkedList[Operator](operatorList)

  override def clone: OperatorManager = {
    val om: OperatorManager = new OperatorManager
    om.operatorList = this.operatorList.clone.asInstanceOf[OperatorRegister]
    om
  }
}