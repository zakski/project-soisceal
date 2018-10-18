package com.szadowsz.gospel.core.db.ops

import java.util
/**
  * Register for operators
  * Cashes operator by name+type description.
  * Retains insertion order as LinkedHashSet.
  *
  * todo Not 100% sure if 'insertion-order-priority' should be completely replaced
  * by the explicit priority given to operators.
  *
  * @author ivar.orstavik@hist.no
  *
  *
  * Created on 03/03/2017.
  */
private[ops] class OperatorRegister extends util.LinkedHashSet[Operator] with Cloneable {
  /**
    *  map of operators by name and type
    *  key is the nameType of an operator (for example ":-xfx")
    *  value is an Operator
    */
  private val nameTypeToKey: util.HashMap[String, Operator] = new util.HashMap[String, Operator]

  def addOperator(op: Operator): Boolean = {
    val nameTypeKey: String = op.name + op.`type`
    val matchingOp: Operator = nameTypeToKey.get(nameTypeKey)
    if (matchingOp != null) {
      super.remove(matchingOp) //removes found match from the main list
    }
    nameTypeToKey.put(nameTypeKey, op) //writes over found match in nameTypeToKey map
    super.add(op) //adds new operator to the main list
  }

  def getOperator(name: String, `type`: String): Operator = nameTypeToKey.get(name + `type`)

  override def clone: AnyRef = {
    val or: OperatorRegister = super.clone.asInstanceOf[OperatorRegister]
    val ior: util.Iterator[Operator] = or.iterator
   while (ior.hasNext) {
        val o: Operator = ior.next
        or.nameTypeToKey.put(o.name + o.`type`, o)
    }
    or
  }
}