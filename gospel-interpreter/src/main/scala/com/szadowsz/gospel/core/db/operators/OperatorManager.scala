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

// scalastyle:off magic.number
import scala.collection.mutable

object OperatorManager {
  /** lowest operator priority */
  val OP_LOW: Int = 1
  /** highest operator priority */
  val OP_HIGH: Int = 1200


  private val standardList = List[Operator](
    Operator(":-", OpType.XFX, 1200),
    Operator("-->", OpType.XFX, 1200),
    Operator(":-", OpType.FX, 1200),
    Operator("?-", OpType.FX, 1200),
    Operator(";", OpType.XFY, 1100),
    Operator("->", OpType.XFY, 1050),
    Operator(",", OpType.XFY, 1000),
    Operator("\\+", OpType.FY, 900),
    Operator("not", OpType.FY, 900),
    Operator("=", OpType.XFX, 700),
    Operator("\\=", OpType.XFX, 700),
    Operator("==", OpType.XFX, 700),
    Operator("\\==", OpType.XFX, 700),
    Operator("@>", OpType.XFX, 700),
    Operator("@<", OpType.XFX, 700),
    Operator("@=<", OpType.XFX, 700),
    Operator("@>=", OpType.XFX, 700),
    Operator("=:=", OpType.XFX, 700),
    Operator("=\\=", OpType.XFX, 700),
    Operator(">", OpType.XFX, 700),
    Operator("<", OpType.XFX, 700),
    Operator("=<", OpType.XFX, 700),
    Operator(">=", OpType.XFX, 700),
    Operator("as", OpType.YFX, 700),
    Operator("is", OpType.XFX, 700),
    Operator("=..", OpType.XFX, 700),
    Operator("+", OpType.YFX, 500),
    Operator("-", OpType.YFX, 500),
    Operator("/\\", OpType.YFX, 500),
    Operator("\\/", OpType.YFX, 500),
    Operator("*", OpType.YFX, 400),
    Operator("/", OpType.YFX, 400),
    Operator("//", OpType.YFX, 400),
    Operator(">>", OpType.YFX, 400),
    Operator("<<", OpType.YFX, 400),
    Operator("rem", OpType.YFX, 400),
    Operator("mod", OpType.YFX, 400),
    Operator("**", OpType.XFX, 200),
    Operator("^", OpType.XFY, 200),
    Operator("\\", OpType.FX, 200),
    Operator("-", OpType.FY, 200)
  )

  private val defaultReg: OpRegistry = OpRegistry(validatedStandardList())

  private def validatedStandardList(): mutable.LinkedHashMap[String, Operator] = {
    val validation = mutable.LinkedHashMap[String, Operator]()
    validation ++= standardList.filter(op => op.prio >= OP_LOW && op.prio <= OP_HIGH).map(op => (op.name + op.opType) -> op)
    assert(validation.size == standardList.length)
    validation
  }
}

private[core] class OperatorManager extends Serializable {

  private val registry : OpRegistry = OperatorManager.defaultReg.copyOf()

  /**
    * Creates a new operator. If the operator is already provided,
    * it replaces it with the new one
    */
  def opNew(name: String, `type`: String, prio: Int) {
    val op = Operator(name, `type`, prio)
    if (prio >= OperatorManager.OP_LOW && prio <= OperatorManager.OP_HIGH) registry.addOperator(op)
  }

  /**
    * Returns the priority of an operator (0 if the operator is not defined).
    */
  def opPrio(name: String, opType: String): Int = {
    registry.getOperator(name, OpType.valueOf(opType.toUpperCase)) match {
      case Some(o) => o.prio
      case None => 0
    }
  }

  /**
    * Returns the priority nearest (lower) to the priority of a defined operator
    */
  def opNext(priority: Int): Int = {
    var nearest = 0
    for (opFromList <- registry.getOperators) {
      if (opFromList.prio > nearest && opFromList.prio < priority) {
        nearest = opFromList.prio
      }
    }
    nearest
  }
}
