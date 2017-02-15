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
package com.szadowsz.gospel.core.data

import com.szadowsz.gospel.core.db.primitive.PrimitiveInfo
import com.szadowsz.gospel.core.operation.OperatorManager
import com.szadowsz.gospel.core.parser.Parser
import com.szadowsz.gospel.util.exception.data.InvalidTermException

import java.{util => ju}
import com.szadowsz.gospel.core.data.util.StructIterator

import scala.collection.Iterable
import scala.collection.JavaConverters._

/**
 * Struct class represents both compound prolog term
 * and atom term (considered as 0-arity compound).
 */
@SerialVersionUID(1L)
class Struct(name : String, arity : Int) extends Term with Iterable[Term] {
  /**
   * name of the structure
   */
  private var _name: String = validateName(name,arity)
  /**
   * arity
   */
  private var _arity: Int = arity

  /**
   * to speedup hash map operation
   */
  private var predicateIndicator: String  = _name + "/" + _arity

  /**
   * args array
   */
  private var _arg: Array[Term] = if (_arity > 0) {new Array[Term](_arity)} else null


  /**
   * primitive behaviour
   */
  @transient
  private var primitive: PrimitiveInfo = null
  /**
   * it indicates if the term is resolved
   */
  private var resolved: Boolean = false

  /**
   * Builds a Struct representing an atom
   */
  def this(f: String) {
    this(f, 0)
  }

  /**
   * Builds a compound, with an array of arguments
   * @throws InvalidTermException
   */
  @throws(classOf[InvalidTermException])
  def this(f: String, argList: Array[Term]) {
    this(f, argList.length)

    for (i <- 0 until argList.length) {
      if (argList(i) == null)
        throw new InvalidTermException("Arguments of a Struct cannot be null")
      else
        _arg(i) = argList(i)
    }
  }

  /**
   * Builds a compound, with one argument
   */
  def this(f : String, at0 : Term) {
    this(f,Array(at0))
  }

  /**
   * Builds a compound, with two arguments
   */
  def this(f : String, at0 : Term, at1 : Term) {
    this(f, Array(at0, at1))
  }

  /**
   * Builds a compound, with three arguments
   */
  def this(f : String, at0 : Term, at1 : Term, at2 : Term) {
    this(f, Array(at0, at1, at2))
  }

  /**
   * Builds a compound, with four arguments
   */
  def this(f : String, at0 : Term, at1 : Term, at2 : Term, at3 : Term) {
    this(f, Array(at0, at1, at2, at3))
  }

  /**
   * Builds a compound, with five arguments
   */
  def this(f : String, at0 : Term, at1 : Term, at2 : Term, at3 : Term, at4 : Term) {
    this(f, Array(at0, at1, at2, at3, at4))
  }

  /**
   * Builds a compound, with six arguments
   */
  def this(f : String, at0 : Term, at1 : Term, at2 : Term, at3 : Term, at4 : Term, at5 : Term) {
    this(f, Array(at0, at1, at2, at3, at4, at5))
  }

  /**
   * Builds a compound, with seven arguments
   */
  def this(f : String, at0 : Term, at1 : Term, at2 : Term, at3 : Term, at4 : Term, at5 : Term, at6 : Term) {
    this(f, Array(at0, at1, at2, at3, at4, at5, at6))
  }

  private def this(argList: Array[Term], index: Int) {
    this(if (index < argList.length)"." else "[]", if (index < argList.length) 2 else 0)
    if (index < argList.length) {
      _arg(0) = argList(index)
      _arg(1) = new Struct(argList, index + 1)
    }
  }

  /**
   * Builds a list providing head and tail
   */
  def this(h: Term, t: Term) {
    this(".", 2)
    _arg(0) = h
    _arg(1) = t
  }

  /**
   * Builds a structure representing an empty list
   */
  def this() {
    this("[]", 0)
    resolved = true
  }



  /**
   * Builds a compound, with a linked list of arguments
   */
  private[gospel] def this(f: String, al: ju.LinkedList[Term]) {
    this(f,al.asScala.toArray)
  }

  /**
   * Builds a compound, with variable arguments
   */
  def this(f: String, argList: Term*) {
    this(f, argList.toArray)
  }

  /**
   * Builds a list specifying the elements
   */
  def this(argList: Array[Term]) {
    this(argList, 0)
  }

  private def validateName(name : String, arity : Int) = {
    if (name == null)
      throw new InvalidTermException("The functor of a Struct cannot be null")
    if (name.length == 0 && arity > 0)
      throw new InvalidTermException("The functor of a non-atom Struct cannot be an empty string")
    name
  }

  /**
   * @return
   */
  def getPredicateIndicator: String = predicateIndicator

  /**
   * Gets the number of elements of this structure
   */
  def getArity: Int = _arity

  /**
   * Gets the functor name of this structure
   */
  def getName: String = _name

  /**
   * Gets the i-th element of this structure
   *
   * No bound check is done
   */
  def getArg(index: Int): Term = _arg(index)

  /**
   * Sets the i-th element of this structure
   *
   * (Only for internal service)
   */
  private[gospel] def setArg(index: Int, argument: Term) {
    _arg(index) = argument
  }

  /**
   * Gets the i-th element of this structure
   *
   * No bound check is done. It is equivalent to
   * <code>getArg(index).getTerm()</code>
   */
  def getTerm(index: Int): Term = if (!_arg(index).isInstanceOf[Var]) _arg(index)  else _arg(index).getTerm

  override def isAtomic: Boolean = _arity == 0

  override def isCompound: Boolean = _arity > 0

  override def isAtom: Boolean = _arity == 0 || isEmptyList

  override def isList: Boolean = (_name == "." && _arity == 2 && _arg(1).isList) || isEmptyList

  override def isGround: Boolean = _arity == 0 || _arg.forall(_.isGround)

  /**
   * Check is this struct is clause or directive
   */
  def isClause: Boolean = _name == ":-" && _arity > 1 && _arg(0).getTerm.isInstanceOf[Struct]

  override def getTerm: Term = this

  /**
   * Gets an argument inside this structure, given its name
   *
   * @param name name of the structure
   * @return the argument or null if not found
   */
  def getArg(name: String): Struct = {
    for (i <- 0 until  _arg.length) {
      if (_arg(i).isInstanceOf[Struct]) {
        val s: Struct = _arg(i).asInstanceOf[Struct]
        if (s.getName == name) {
          return s
        } else {
          val sol: Struct = s.getArg(name)
          if (sol != null) {
            return sol
          }
        }
      }
    }
    null
  }

  /**
   * Test if a term is greater than other
   */
  override def isGreater(theTerm: Term): Boolean = {
    theTerm.getTerm match {
      case struct : Struct =>
        val tarity: Int = struct._arity
        if (_arity > tarity) {
          return true
        } else if (_arity == tarity) {
          if (_name.compareTo(struct._name) > 0) {
            return true
          } else if (_name.compareTo(struct._name) == 0) {
            for (c <- 0 until _arity) {
              if (_arg(c).isGreater(struct._arg(c))) {
                return true
              }
              else if (!_arg(c).isEqual(struct._arg(c))) {
                return false
              }
            }
          }
        }
        false
      case _ => true
    }
  }

  override def isGreaterRelink(theTerm: Term, vorder: ju.ArrayList[String]): Boolean = {
    theTerm.getTerm match {
      case struct : Struct =>
        val tarity: Int = struct._arity
        if (_arity > tarity) {
          return true
        } else if (_arity == tarity) {
          if (_name.compareTo(struct._name) > 0) {
            return true
          } else if (_name.compareTo(struct._name) == 0) {
            for (c <- 0 until _arity) {
              if (_arg(c).isGreaterRelink(struct._arg(c), vorder)) {
                return true
              }
              else if (!_arg(c).isEqual(struct._arg(c))) {
                return false
              }
            }
          }
        }
        false
      case _ => true
    }
  }

  /**
   * Test if a term is equal to other
   */
  override def isEqual(theTerm: Term): Boolean = {
    theTerm.getTerm match {
      case struct : Struct =>
        if (_name == struct._name ) {
          (_arity == 0 && struct._arity == 0)|| _arg.corresponds(struct._arg){(first,second) => first.isEqual(second)}
        } else {
          false
        }
      case _ =>
        false
    }
  }

  /**
   * Gets a copy of this structure
   * @param vMap is needed for register occurence of same variables
   */
  private[gospel] override def copy(vMap: ju.AbstractMap[Var, Var], idExecCtx: Int): Term = {
    val copy  = new Struct(_name,_arity)
    copy.resolved = resolved
    copy.primitive = primitive
    copy._arg = if (_arity > 0) {_arg.map( _.copy(vMap, idExecCtx))} else null
    copy
  }

  /**
   * Gets a copy of this structure
   * @param vMap is needed for register occurence of same variables
   */
  private[gospel] override def copy(vMap: ju.AbstractMap[Var, Var], substMap: ju.AbstractMap[Term, Var]): Term = {
    val copy = new Struct(_name,_arity)
    copy.resolved = false
    copy.primitive = null
    copy._arg = if (_arity > 0) {_arg.map( _.copy(vMap, substMap))} else null
    copy
  }

  /**
   * resolve term
   */
  private[gospel] def resolveTerm(count: Long): Long = {
    if (resolved) {
      count
    } else {
      val vars = new ju.LinkedList[Var]
      resolveTerm(vars, count)
    }
  }

  /**
   * Resolve name of terms
   * @param vl list of variables resolved
   * @param count start timestamp for variables of this term
   * @return next timestamp for other terms
   */
  private[gospel] def resolveTerm(vl: ju.LinkedList[Var], count: Long): Long = {
    var newcount: Long = count
    for (c <- 0 until _arity) {
      var term: Term = _arg(c)
      if (term != null) {

        //--------------------------------
        // we want to resolve only not linked variables:
        // so linked variables must get the linked term
        term = term.getTerm

        if (term.isInstanceOf[Var]) {
          val t: Var = term.asInstanceOf[Var]
          t.setTimestamp({newcount += 1; newcount - 1})

          if (!t.isAnonymous) {
            // searching a variable with the same name in the list
            val name: String = t.getName
            val it: Iterator[Var] = vl.iterator.asScala
            var found: Var = null

            while (it.hasNext && found == null) {
              val vn: Var = it.next()
              if (name == vn.getName) {
                found = vn
              }
            }
            if (found != null) {
              _arg(c) = found
            }
            else {
              vl.add(t)
            }
          }
        }
        else if (term.isInstanceOf[Struct]) {
          newcount = term.asInstanceOf[Struct].resolveTerm(vl, newcount)
        }
      }
    }
    resolved = true
    newcount
  }

  /**
   * Is this structure an empty list?
   */
  def isEmptyList: scala.Boolean = _name == "[]" && _arity == 0

  /**
   * Gets the head of this structure, which is supposed to be a list.
   *
   * <p>
   * Gets the head of this structure, which is supposed to be a list.
   * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
   * </p>
   */
  def listHead: Term = {
    if (!isList) {
      throw new UnsupportedOperationException("The structure " + this + " is not a list.")
    }else {
      _arg(0).getTerm
    }
  }

  /**
   * Gets the tail of this structure, which is supposed to be a list.
   *
   * <p>
   * Gets the tail of this structure, which is supposed to be a list.
   * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
   * </p>
   */
  def listTail: Struct = {
    if (!isList) {
      throw new UnsupportedOperationException("The structure " + this + " is not a list.")
    }else {
      _arg(1).getTerm.asInstanceOf[Struct]
    }
  }

  /**
   * Gets the number of elements of this structure, which is supposed to be a list.
   *
   * <p>
   * Gets the number of elements of this structure, which is supposed to be a list.
   * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
   * </p>
   */
  def listSize: Int = {
    if (!isList) {
      throw new UnsupportedOperationException("The structure " + this + " is not a list.")
    } else {
      def listSize(struct : Struct, count : Int): Int = if(struct.isEmptyList)count else listSize(struct.listTail,count+1)
      listSize(this,0)
    }
  }

  /**
   * Gets an iterator on the elements of this structure, which is supposed to be a list.
   *
   * <p>
   * Gets an iterator on the elements of this structure, which is supposed to be a list.
   * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
   * </p>
   */
  override def iterator: Iterator[Term] = {
    if (!isList) {
      throw new UnsupportedOperationException("The structure " + this + " is not a list.")
    } else {
      new StructIterator(this)
    }
  }

  /**
   * Gets a list Struct representation, with the functor as first element.
   */
  private[gospel] def toListStruct: Struct = {
    val tail = if (_arity > 0) _arg.foldRight(new Struct){(tail,head) => new Struct(head.getTerm, tail)} else new Struct
    new Struct(new Struct(_name), tail)
  }

  /**
   * Gets a flat Struct from this structure considered as a List
   *
   * If this structure is not a list, null object is returned
   */
  private[gospel] def fromList: Struct = {
    val ft: Term = _arg(0).getTerm
    if (!ft.isAtom) {
      return null
    }
    var at: Struct = _arg(1).getTerm.asInstanceOf[Struct]
    val al = new ju.LinkedList[Term]
    while (!at.isEmptyList) {
      if (!at.isList) {
        return null
      }
      al.addLast(at.getTerm(0))
      at = at.getTerm(1).asInstanceOf[Struct]
    }
    new Struct(ft.asInstanceOf[Struct]._name, al)
  }

  /**
   * Appends an element to this structure supposed to be a list
   */
  def append(t: Term) {
    if (isEmptyList) {
      _name = "."
      _arity = 2
      predicateIndicator = _name + "/" + _arity
      _arg = new Array[Term](_arity)
      _arg(0) = t
      _arg(1) = new Struct
    }
    else if (_arg(1).isList) {
      _arg(1).asInstanceOf[Struct].append(t)
    }
    else {
      _arg(1) = t
    }
  }

  /**
   * Inserts (at the head) an element to this structure supposed to be a list
   */
  private[gospel] def insert(t: Term) {
    val co: Struct = new Struct
    co._arg(0) = _arg(0)
    co._arg(1) = _arg(1)
    _arg(0) = t
    _arg(1) = co
  }

  /**
   * Try to unify two terms
   * @param t the term to unify
   * @param vl1 list of variables unified
   * @param vl2 list of variables unified
   * @return true if the term is unifiable with this one
   */
  private[gospel] def unify(vl1: ju.List[Var], vl2: ju.List[Var], t: Term): Boolean = {
    val term = t.getTerm
    if (term.isInstanceOf[Struct]) {
      val ts: Struct = term.asInstanceOf[Struct]
      if (_arity == ts._arity && (_name == ts._name)) {
        for(c <- 0 until _arity) {
          if (!_arg(c).unify(vl1, vl2, ts._arg(c))) {
            return false
          }

        }
        return true
      }
    }
    else if (term.isInstanceOf[Var]) {
      return term.unify(vl2, vl1, this)
    }
    return false
  }

  /** dummy method */
  def free() {
  }

  /**
   * Set primitive behaviour associated at structure
   */
  private[gospel] def setPrimitive(b: PrimitiveInfo) {
    primitive = b
  }

  /**
   * Get primitive behaviour associated at structure
   */
  def getPrimitive: PrimitiveInfo = primitive

  /**
   * Check if this term is a primitive struct
   */
  def isPrimitive: Boolean = primitive != null

  /**
   * Gets the string representation of this structure
   *
   * Specific representations are provided for lists and atoms.
   * Names starting with upper case letter are enclosed in apices.
   */
  override def toString(): String = {
    if (isEmptyList)
      "[]"
    else if ((_name == ".") && _arity == 2) {
      "[" + toString0 + "]"
    }
    else if (_name == "{}") {
      "{" + toString0_bracket + "}"
    } else {
      var name: String = if (Parser.isAtom(_name)) _name else "'" + _name + "'"
      if (_arity > 0) {
        name = name + "("
        for(c <- 1 until _arity) {
          if (!_arg(c - 1).isInstanceOf[Var]) {
            name = name + _arg(c - 1).toString + ","
          } else {
            name = name + _arg(c - 1).asInstanceOf[Var].toStringFlattened + ","
          }
        }

        if (!_arg(_arity - 1).isInstanceOf[Var]) {
          name = name + _arg(_arity - 1).toString + ")"
        } else {
          name = name + _arg(_arity - 1).asInstanceOf[Var].toStringFlattened + ")"
        }
      }
      name
    }
  }

  private def toString0: String = {
    val h: Term = _arg(0).getTerm
    val t: Term = _arg(1).getTerm
    if (t.isList) {
      val tl: Struct = t.asInstanceOf[Struct]
      if (tl.isEmptyList) {
        return h.toString
      }
      if (h.isInstanceOf[Var]) {
        h.asInstanceOf[Var].toStringFlattened + "," + tl.toString0
      } else {
        h.toString + "," + tl.toString0
      }
    } else {
      var h0: String = null
      var t0: String = null
      if (h.isInstanceOf[Var]) {
        h0 = h.asInstanceOf[Var].toStringFlattened
      }
      else {
        h0 = h.toString
      }
      if (t.isInstanceOf[Var]) {
        t0 = t.asInstanceOf[Var].toStringFlattened
      }
      else {
        t0 = t.toString
      }
      h0 + "|" + t0
    }
  }

  private def toString0_bracket: String = {
    if (_arity == 0) {
      ""
    } else if (_arity == 1 && !(_arg(0).isInstanceOf[Struct] && (_arg(0).asInstanceOf[Struct].getName == ","))) {
      _arg(0).getTerm.toString
    } else {
      var head: Term = _arg(0).asInstanceOf[Struct].getTerm(0)
      var tail: Term = _arg(0).asInstanceOf[Struct].getTerm(1)
      val buf: StringBuffer = new StringBuffer(head.toString)
      while (tail.isInstanceOf[Struct] && (tail.asInstanceOf[Struct].getName == ",")) {
        head = tail.asInstanceOf[Struct].getTerm(0)
        buf.append("," + head.toString)
        tail = tail.asInstanceOf[Struct].getTerm(1)
      }
      buf.append("," + tail.toString)
      buf.toString
    }
  }

  private def toStringAsList(op: OperatorManager): String = {
    val h: Term = _arg(0)
    val t: Term = _arg(1).getTerm
    if (t.isList) {
      val tl: Struct = t.asInstanceOf[Struct]
      if (tl.isEmptyList) {
        return h.toStringAsArgY(op, 0)
      }
      h.toStringAsArgY(op, 0) + "," + tl.toStringAsList(op)
    } else {
      h.toStringAsArgY(op, 0) + "|" + t.toStringAsArgY(op, 0)
    }
  }

  private[gospel] override def toStringAsArg(op: OperatorManager, prio: Int, x: Boolean): String = {
    var p: Int = 0
    var v: String = ""
    if ((_name == ".") && _arity == 2) {
      if (_arg(0).isEmptyList) {
        return "[]"
      } else {
        return "[" + toStringAsList(op) + "]"
      }
    } else if (_name == "{}") {
      return "{" + toString0_bracket + "}"
    }
    if (_arity == 2) {
      if ({p = op.opPrio(_name, "xfx"); p} >= OperatorManager.OP_LOW) {
        return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _arg(0).toStringAsArgX(op, p) + " " + _name + " " + _arg(1).toStringAsArgX(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
      }
      if ({p = op.opPrio(_name, "yfx"); p} >= OperatorManager.OP_LOW) {
        return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _arg(0).toStringAsArgY(op, p) + " " + _name + " " + _arg(1).toStringAsArgX(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
      }
      if ({p = op.opPrio(_name, "xfy"); p} >= OperatorManager.OP_LOW) {
        if (!(_name == ",")) {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _arg(0).toStringAsArgX(op, p) + " " + _name + " " + _arg(1).toStringAsArgY(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        } else {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _arg(0).toStringAsArgX(op, p) + "," + _arg(1).toStringAsArgY(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        }
      }
    } else if (_arity == 1) {
      if ({p = op.opPrio(_name, "fx"); p} >= OperatorManager.OP_LOW) {
        return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _name + " " + _arg(0).toStringAsArgX(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
      }
      if ({p = op.opPrio(_name, "fy"); p} >= OperatorManager.OP_LOW) {
        return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _name + " " + _arg(0).toStringAsArgY(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
      }
      if ({p = op.opPrio(_name, "xf"); p} >= OperatorManager.OP_LOW) {
        return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _arg(0).toStringAsArgX(op, p) + " " + _name + " " + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
      }
      if ({p = op.opPrio(_name, "yf"); p} >= OperatorManager.OP_LOW) {
        return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + _arg(0).toStringAsArgY(op, p) + " " + _name + " " + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
      }
    }
    v = if (Parser.isAtom(_name)) _name else "'" + _name + "'"
    if (_arity == 0) {
      return v
    }
    v = v + "("
    for(p <- 1 until _arity) {
      v = v + _arg(p - 1).toStringAsArgY(op, 0) + ","
    }
    v = v + _arg(_arity - 1).toStringAsArgY(op, 0)
    v = v + ")"
    v
  }

  override def iteratedGoalTerm: Term = {
    if ((_name == "^") && _arity == 2) {
      val goal: Term = getTerm(1)
      goal.iteratedGoalTerm
    }
    else {
      super.iteratedGoalTerm
    }
  }
}