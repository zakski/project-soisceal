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
 *//*
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

import com.szadowsz.gospel.core.db.ops.OperatorManager
import com.szadowsz.gospel.core.db.primitives.PrimitiveInfo
import com.szadowsz.gospel.core.error.InvalidTermException
import com.szadowsz.gospel.core.parser.Parser
import java.util

import scala.collection.JavaConverters._
// scalastyle:off number.of.methods

/**
  * Struct class represents both compound prolog term
  * and atom term (considered as 0-arity compound).
  */
@SerialVersionUID(1L)
class Struct(n: String, a: scala.Int) extends Term with Iterable[Term] {

  /**
    * name of the structure
    */
  private var name: String = Option(n).getOrElse(throw new InvalidTermException("The functor of a Struct cannot be null"))

  /**
    * args array
    */
  private var arg = if (a > 0) Array.ofDim[Term](a) else null

  /**
    * arity
    */
  private var arity = if (name.length() == 0 && a > 0) throw new InvalidTermException("The functor of a non-atom Struct cannot be an empty string") else a

  /**
    * to speedup hash map operation
    */
  private var predicateIndicator = name + "/" + arity

  /**
    * primitive behaviour
    */
  private var primitive: PrimitiveInfo = _

  /**
    * it indicates if the term is resolved
    */
  private var resolved = false

  /**
    * Builds a structure representing an empty list
    */
  def this() {
    this("[]", 0)
    resolved = true
  }

  /**
    * Builds a Struct representing an atom
    */
  def this(f: String) {
    this(f, 0)
  }

  /**
    * Builds a compound, with an array of arguments
    */
  def this(f: String, argList: Array[Term]) {
    this(f, argList.length)
    for (i <- argList.indices) {
      if (argList(i) == null) {
        throw new InvalidTermException("Arguments of a Struct cannot be null")
      } else {
        arg(i) = argList(i)
      }
    }
  }


  /**
    * Builds a compound, with one argument
    */
  def this(f: String, at0: Term) {
    this(f, Array[Term](at0))
  }

  /**
    * Builds a compound, with two arguments
    */
  def this(f: String, at0: Term, at1: Term) {
    this(f, Array[Term](at0, at1))
  }

  /**
    * Builds a compound, with three arguments
    */
  def this(f: String, at0: Term, at1: Term, at2: Term) {
    this(f, Array[Term](at0, at1, at2))
  }

  /**
    * Builds a compound, with four arguments
    */
  def this(f: String, at0: Term, at1: Term, at2: Term, at3: Term) {
    this(f, Array[Term](at0, at1, at2, at3))
  }

  /**
    * Builds a compound, with five arguments
    */
  def this(f: String, at0: Term, at1: Term, at2: Term, at3: Term, at4: Term) {
    this(f, Array[Term](at0, at1, at2, at3, at4))
  }

  /**
    * Builds a compound, with six arguments
    */
  def this(f: String, at0: Term, at1: Term, at2: Term, at3: Term, at4: Term, at5: Term) {
    this(f, Array[Term](at0, at1, at2, at3, at4, at5))
  }

  /**
    * Builds a compound, with seven arguments
    */
  def this(f: String, at0: Term, at1: Term, at2: Term, at3: Term, at4: Term, at5: Term, at6: Term) {
    this(f, Array[Term](at0, at1, at2, at3, at4, at5, at6))
  }

  /**
    * Builds a list providing head and tail
    */
  def this(h: Term, t: Term) {
    this(".", 2)
    arg(0) = h
    arg(1) = t
  }

  def this(argList: Array[Term], index: scala.Int) {
    this(if (index < argList.length) "." else "[]", if (index < argList.length) 2 else 0)
    if (index < argList.length) {
      arg(0) = argList(index)
      arg(1) = new Struct(argList, index + 1)
    } // else build an empty list
  }

  /**
    * Builds a list specifying the elements
    */
  def this(argList: Array[Term]) {
    this(argList, 0)
  }

  /**
    * Builds a compound, with a linked list of arguments
    */
  def this(f: String, al: util.LinkedList[Term]) {
    this(f, al.size())
    if (arity > 0) {
      for (c <- arg.indices) {
        arg(c) = al.removeFirst()
      }
    }
  }


  /**
    * @deprecated Use Struct#getPredicateIndicator instead.
    */
  private[data] def getHashKey = getPredicateIndicator

  /**
    * @return
    */
  def getPredicateIndicator: String = predicateIndicator

  /**
    * Gets the number of elements of this structure
    */
  def getArity: scala.Int = arity

  /**
    * Gets the functor name  of this structure
    */
  def getName: String = name

  /**
    * Gets the i-th element of this structure
    *
    * No bound check is done
    */
  def getArg(index: scala.Int): Term = arg(index)

  /**
    * Sets the i-th element of this structure
    *
    * (Only for internal service)
    */
  def setArg(index: scala.Int, argument: Term): Unit = {
    arg(index) = argument
  }

  /**
    * Gets the i-th element of this structure
    * <p>
    * No bound check is done. It is equivalent to
    * <code>getArg(index).getTerm()</code>
    */
  def getTerm(index: scala.Int): Term = {
    if (!arg(index).isInstanceOf[Var]) {
      arg(index)
    } else {
      arg(index).getTerm
    }
  }

  override def isAtomic: Boolean = arity == 0

  override def isCompound: Boolean = arity > 0

  override def isAtom: Boolean = arity == 0 || isEmptyList

  override def isList: Boolean = (name == "." && arity == 2 && arg(1).isList) || isEmptyList

  override def isGround: Boolean = Option(arg).isEmpty || arg.forall(_.isGround)


  /**
    * Check is this struct is clause or directive
    */
  def isClause: Boolean = name == ":-" && arity > 1 && arg(0).getTerm.isInstanceOf[Struct]

  override def getTerm: Term = this

  /**
    * Gets an argument inside this structure, given its name
    *
    * @param name name of the structure
    * @return the argument or null if not found
    */
  private def getArg(name: String): Struct = {
    if (arity == 0) {
      null
    } else {
      for (anArg1 <- arg) {
        if (anArg1.isInstanceOf[Struct]) {
          val s = anArg1.asInstanceOf[Struct]
          if (s.getName == name) {
            return s
          }
        }
      }
      for (anArg <- arg) {
        if (anArg.isInstanceOf[Struct]) {
          val s = anArg.asInstanceOf[Struct]
          val sol = s.getArg(name)
          if (sol != null) {
            return sol
          }
        }
      }
      null
    }
  }

  /**
    * Test if a term is greater than other
    */
  override def isGreater(t: Term): Boolean = {
    val t2 = t.getTerm
    if (!(t2.isInstanceOf[Struct])) {
      true
    } else {
      val ts = t2.asInstanceOf[Struct]
      if (arity > ts.arity) {
        true
      } else if (arity == ts.arity) {
        if (name.compareTo(ts.name) > 0) {
          true
        } else if (name.compareTo(ts.name) == 0) {
          for (c <- 0 until arity) {
            if (arg(c).isGreater(ts.arg(c))) {
              return true
            } else if (!arg(c).isEqual(ts.arg(c))) {
              return false
            }
          }
          false
        } else {
          false
        }
      } else {
        false
      }
    }
  }


  /**
    * Gets a copy of this structure
    *
    * @param vMap is needed for register occurence of same variables
    */
  override def copy(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = {
    val t = new Struct(name, arity)
    t.resolved = resolved
    t.primitive = primitive
    for (c <- 0 until arity) {
      //if(!this.arg[c].isCyclic)
      t.arg(c) = arg(c).copy(vMap, idExecCtx)
      //else
      //	t.arg[c] = this.arg[c];
    }
    t
  }

  override def copyAndRetainFreeVar(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = {
    val t = new Struct(name, arity)
    t.resolved = resolved
    t.primitive = primitive
    for (c <- 0 until arity) {
      //if(!this.arg[c].isCyclic)
      t.arg(c) = arg(c).getTerm.copyAndRetainFreeVar(vMap, idExecCtx)
      //qui una .getTerm() necessaria solo in $wt_list!
      //else
      //	t.arg[c] = this.arg[c];
    }
    t
  }


  override def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]): Term = {
    val t = new Struct(name, arity)
    t.resolved = false
    t.primitive = null
    for (c <- 0 until arity) {
      //if(!this.arg[c].isCyclic)
      t.arg(c) = arg(c).copy(vMap, substMap)
      //else
      //	t.arg[c] = this.arg[c];
    }
    t
  }


  /**
    * resolve term
    */
  override def resolveTerm(count: scala.Long): scala.Long = {
    if (resolved) {
      count
    } else {
      val vars = new util.LinkedList[Var]
      resolveTerm(vars, count)
    }
  }


  /**
    * Resolve name of terms
    *
    * @param vl    list of variables resolved
    * @param count start timestamp for variables of this term
    * @return next timestamp for other terms
    */
  private def resolveTerm(vl: util.LinkedList[Var], count: scala.Long): scala.Long = {
    var newcount = count
    for (c <- 0 until arity) {
      var term = arg(c)
      if (term != null) {
        //--------------------------------
        // we want to resolve only not linked variables:
        // so linked variables must get the linked term
        term = term.getTerm
        //--------------------------------
        if (term.isInstanceOf[Var]) {
          var t = term.asInstanceOf[Var]
          t.setInternalTimestamp(newcount)
          newcount += 1
          if (!t.isAnonymous) {
            // searching a variable with the same name in the list
            val name = t.getName
            val it = vl.iterator()
            var found: Var = null
            while (it.hasNext && found == null) {
              var vn = it.next()
              if (name.equals(vn.getName)) {
                found = vn
              }
            }
            if (found != null) {
              arg(c) = found
            } else {
              vl.add(t)
            }
          }
        } else if (term.isInstanceOf[Struct]) {
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
  override def isEmptyList: Boolean = {
    name == "[]" && arity == 0
  }

  /**
    * Gets the head of this structure, which is supposed to be a list.
    * <p>
    * <p>
    * Gets the head of this structure, which is supposed to be a list.
    * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
    * </p>
    */
  def listHead: Term = {
    if (!isList) {
      throw new UnsupportedOperationException(s"The structure $this is not a list.")
    } else {
      arg(0).getTerm
    }
  }


  /**
    * Gets the tail of this structure, which is supposed to be a list.
    * <p>
    * <p>
    * Gets the tail of this structure, which is supposed to be a list.
    * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
    * </p>
    */
  def listTail: Struct = {
    if (!isList) {
      throw new UnsupportedOperationException(s"The structure $this is not a list.")
    } else {
      arg(1).getTerm.asInstanceOf[Struct]
    }
  }

  /**
    * Gets the number of elements of this structure, which is supposed to be a list.
    * <p>
    * <p>
    * Gets the number of elements of this structure, which is supposed to be a list.
    * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
    * </p>
    */
  def listSize: scala.Int = {
    if (!isList) {
      throw new UnsupportedOperationException(s"The structure $this is not a list.")
    } else {
      var t = this
      var count = 0
      while (!t.isEmptyList) {
        count += 1
        t = t.arg(1).getTerm.asInstanceOf[Struct]
      }
      count
    }
  }

  /**
    * Gets an iterator on the elements of this structure, which is supposed to be a list.
    * <p>
    * <p>
    * Gets an iterator on the elements of this structure, which is supposed to be a list.
    * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
    * </p>
    */
  def listIterator: util.Iterator[_ <: Term] = {
   iterator.asJava
  }

  override def iterator: Iterator[Term] = {
    if (!isList) {
      throw new UnsupportedOperationException(s"The structure $this is not a list.")
    } else {
      new Iterator[Term] {
        var list: Struct = Struct.this

        override def hasNext: Boolean = !list.isEmptyList

        override def next(): Term = {
          if (list.isEmptyList) {
            throw new NoSuchElementException
          } else {
            val head = list.getTerm(0)
            list = list.getTerm(1).asInstanceOf[Struct]
            head
          }
        }
      }
    }
  }

  /**
    * Gets a list Struct representation, with the functor as first element.
    */
  def toStructList: Struct = {
    var t = new Struct
    for (c <- arity - 1 to 0 by -1) {
      t = new Struct(arg(c).getTerm, t)
    }
    new Struct(new Struct(name), t)
  }

  /**
    * Gets a flat Struct from this structure considered as a List
    * <p>
    * If this structure is not a list, null object is returned
    */
  def fromStructList: Struct = {
    val ft = arg(0).getTerm

    if (!ft.isAtom) {
      null
    } else {
      var at = arg(1).getTerm.asInstanceOf[Struct]
      val al = new util.LinkedList[Term]
      while (!at.isEmptyList) {
        if (!at.isList) {
          return null
        }
        al.addLast(at.getTerm(0))
        at = at.getTerm(1).asInstanceOf[Struct]
      }
      new Struct(ft.asInstanceOf[Struct].name, al)
    }
  }


  /**
    * Appends an element to this structure supposed to be a list
    */
  def append(t: Term): Unit = {
    if (isEmptyList) {
      name = "."
      arity = 2
      predicateIndicator = name + "/" + arity /* Added by Paolo Contessi */
      arg = new Array[Term](arity)
      arg(0) = t
      arg(1) = new Struct
    } else if (arg(1).isList) {
      arg(1).asInstanceOf[Struct].append(t)
    } else {
      arg(1) = t
    }
  }

  /**
    * Try to unify two terms
    *
    * @param t   the term to unify
    * @param vl1 list of variables unified
    * @param vl2 list of variables unified
    * @return true if the term is unifiable with this one
    */
  override def unify(vl1: util.List[Var], vl2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean = {
    // In fase di unificazione bisogna annotare tutte le variabili della struct completa.
    val t2 = t.getTerm
    if (t2.isInstanceOf[Struct]) {
      val ts = t2.asInstanceOf[Struct]
      if (arity == ts.arity && name.equals(ts.name)) {
        for (c <- 0 until arity) {
          if (!arg(c).unify(vl1, vl2, ts.arg(c), isOccursCheckEnabled)) {
            return false
          }
        }
        return true
      }
    } else if (t2.isInstanceOf[Var]) {
      return t2.unify(vl2, vl1, this, isOccursCheckEnabled)
    }
    false
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
    * Set primitive behaviour associated at structure
    */
  def setPrimitive(b: PrimitiveInfo): Unit = {
    primitive = b
  }


  /**
    * Gets the string representation of this structure
    * <p>
    * Specific representations are provided for lists and atoms.
    * Names starting with upper case letter are enclosed in apices.
    */
  override def toString: String = {
    if (isEmptyList) { // empty list case
      "[]"
    } else if (name.equals(".") && arity == 2) { // list case
      "[" + toString0 + "]"
    } else if (name.equals("{}")) {
      "{" + toString0_bracket + "}"
    } else {
      var s = if (Parser.isAtom(name)) name else "'" + name + "'"
      if (arity > 0) {
        s = s + "("
        for (c <- 1 until arity) {
          if (!arg(c - 1).isInstanceOf[Var]) {
            s = s + arg(c - 1).toString + ","
          } else {
            s = s + arg(c - 1).asInstanceOf[Var].toStringFlattened + ","
          }
        }
        if (!(arg(arity - 1).isInstanceOf[Var])) {
          s = s + arg(arity - 1).toString + ")"
        } else {
          s = s + arg(arity - 1).asInstanceOf[Var].toStringFlattened + ")"
        }
      }
      s
    }
  }


  private def toString0: String = {
    val h = arg(0).getTerm
    val t = arg(1).getTerm
    if (t.isList) {
      val tl = t.asInstanceOf[Struct]
      if (tl.isEmptyList) {
        h.toString
      } else if (h.isInstanceOf[Var]) {
        h.asInstanceOf[Var].toStringFlattened + "," + tl.toString0
      } else {
        h.toString + "," + tl.toString0
      }
    } else {
      val h0 = if (h.isInstanceOf[Var]) {
        h.asInstanceOf[Var].toStringFlattened
      } else {
        h.toString
      }
      val t0 = if (t.isInstanceOf[Var]) {
        t.asInstanceOf[Var].toStringFlattened
      } else {
        t.toString
      }
      h0 + "|" + t0
    }
  }


  private def toString0_bracket: String = {
    if (arity == 0) {
      ""
    } else if (arity == 1 && !(arg(0).isInstanceOf[Struct] && arg(0).asInstanceOf[Struct].getName == ",")) {
      arg(0).getTerm.toString
    } else {
      // comma case
      var head = arg(0).asInstanceOf[Struct].getTerm(0)
      var tail = arg(0).asInstanceOf[Struct].getTerm(1)
      val buf = new StringBuilder(head.toString)
      while (tail.isInstanceOf[Struct] && tail.asInstanceOf[Struct].getName == ",") {
        head = tail.asInstanceOf[Struct].getTerm(0)
        buf.append("," + head.toString());
        tail = tail.asInstanceOf[Struct].getTerm(1)
      }
      buf.append("," + tail.toString)
      buf.toString()
    }
  }

  private def toStringAsList(op: OperatorManager): String = {
    val h = arg(0)
    val t = arg(1).getTerm
    if (t.isList) {
      val tl = t.asInstanceOf[Struct]
      if (tl.isEmptyList) {
        h.toStringAsArgY(op, 0)
      } else {
        h.toStringAsArgY(op, 0) + "," + tl.toStringAsList(op)
      }
    } else {
      h.toStringAsArgY(op, 0) + "|" + t.toStringAsArgY(op, 0)
    }
  }

  override def toStringAsArg(op: OperatorManager, prio: scala.Int, x: Boolean): String = {
    var p = 0
    var v: String = null
    if (name.equals(".") && arity == 2) {
      if (arg(0).isEmptyList) {
        "[]"
      } else {
        "[" + toStringAsList(op) + "]"
      }
    } else if (name.equals("{}")) {
      "{" + toString0_bracket + "}"
    } else {
      if (arity == 2) {
        p = op.opPrio(name, "xfx")
        if (p >= OperatorManager.OP_LOW) {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + arg(0).toStringAsArgX(op, p) + " " +
            name + " " + arg(1).toStringAsArgX(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        }
        p = op.opPrio(name, "yfx")
        if (p >= OperatorManager.OP_LOW) {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + arg(0).toStringAsArgY(op, p) + " " +
            name + " " + arg(1).toStringAsArgX(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        }
        p = op.opPrio(name, "xfy")
        if (p >= OperatorManager.OP_LOW) {
          if (!name.equals(",")) {
            return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + arg(0).toStringAsArgX(op, p) +
              " " + name + " " + arg(1).toStringAsArgY(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
          } else {
            return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + arg(0).toStringAsArgX(op, p) + //",\n\t"+
              "," + arg(1).toStringAsArgY(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
          }
        }
      } else if (arity == 1) {
        p = op.opPrio(name, "fx")
        if (p >= OperatorManager.OP_LOW) {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + name + " " +
            arg(0).toStringAsArgX(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        }
        p = op.opPrio(name, "fy")
        if (p >= OperatorManager.OP_LOW) {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + name + " " +
            arg(0).toStringAsArgY(op, p) + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        }
        p = op.opPrio(name, "xf")
        if (p >= OperatorManager.OP_LOW) {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + arg(0).toStringAsArgX(op, p) +
            " " + name + " " + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        }
        p = op.opPrio(name, "yf")
        if (p >= OperatorManager.OP_LOW) {
          return (if ((x && p >= prio) || (!x && p > prio)) "(" else "") + arg(0).toStringAsArgY(op, p) +
            " " + name + " " + (if ((x && p >= prio) || (!x && p > prio)) ")" else "")
        }
      }
      v = if (Parser.isAtom(name)) name else "'" + name + "'"
      if (arity == 0) {
        return v
      }
      v = v + "("
      for (p <- 1 until arity) {
        v = v + arg(p - 1).toStringAsArgY(op, 0) + ","
      }
      v = v + arg(arity - 1).toStringAsArgY(op, 0)
      v + ")"
    }
  }

  override def iteratedGoalTerm: Term = {
    if (name == "^" && arity == 2) {
      val goal = getTerm(1)
      goal.iteratedGoalTerm
    } else {
      super.iteratedGoalTerm
    }
  }

  override def unify(varsUnifiedArg1: util.List[Var], varsUnifiedArg2: util.List[Var], t: Term): Boolean = {
    unify(varsUnifiedArg1, varsUnifiedArg2, t, true)
  }
}