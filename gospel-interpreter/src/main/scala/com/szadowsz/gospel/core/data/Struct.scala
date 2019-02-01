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
package com.szadowsz.gospel.core.data

import java.util

import com.szadowsz.gospel.core.db.primitives.Primitive
import com.szadowsz.gospel.core.parser.Parser

// scalastyle:off number.of.methods
class Struct(val name: String, val arity: scala.Int, val args: List[Term] = Nil) extends Term {
  
  /**
    * primitive java/scala behaviour
    */
  private var primitive: Primitive = _
  
  /**
    * it indicates if the struct has it's vars resolved
    */
  private var resolved = false
  
  /**
    * Builds a structure representing an empty list
    */
  def this() {
    this("[]", 0)
    //  resolved = true
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
  def this(f: String, argSeq: Seq[Term]) {
    this(f, argSeq.length, argSeq.toList)
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
    this(".", 2, List(h, t))
  }
  
  def this(argList: Array[Term], index: scala.Int) {
    this(if (index < argList.length) "." else "[]", if (index < argList.length) 2 else 0)
    if (index < argList.length) {
      //      arg(0) = argList(index)
      //      arg(1) = new Struct(argList, index + 1)
    } // else build an empty list
  }
  
  /**
    * Builds a list specifying the elements
    */
  def this(argList: Array[Term]) {
    this(argList, 0)
  }
  
  def apply(index: scala.Int): Term = args(index)
  
  /**
    * Resolves variables inside the term
    *
    * If the variables has been already resolved, no renaming is done.
    */
  override def resolveVars(): Unit = {
    
  }
  
  override def isClause: Boolean = {
    name == ":-" && arity > 1 && args.head.getBinding.isInstanceOf[Struct]
  }
  
  override def isEmptyList: Boolean = name == "[]" && arity == 0
  
  override def isGround: Boolean = args.forall(_.isGround)
  
  override def isList: Boolean = (name == "." && arity == 2 && args(1).isList) || isEmptyList
  
  override def isEquals(other: Term): Boolean = {
    other match {
      case s: Struct =>
        name == s.name && arity == s.arity && args.sameElements(s.args)
      case _ => false
    }
  }
  
  /**
    * Gets the functor name of this structure
    *
    * @return the name of the struct.
    */
  def getName: String = name
  
  /**
    * Gets the number of arguments for this structure.
    *
    * @return the number of arguments or operands that the struct takes.
    */
  def getArity: scala.Int = arity
  
  /**
    * Gets the i-th element of this structure
    *
    * @note No bound check is done. It is equivalent to <code>getArg(index).getTerm()</code
    * @return nth term of the struct
    */
  def getTerm(index: scala.Int): Term = args(index).getBinding
  
  def getBindingIterator: Iterator[Term] = {
    new Iterator[Term] {
      private var index = 0
      
      override def hasNext: Boolean = index < arity
      
      override def next(): Term = {
        val res = Struct.this.getTerm(index)
        index += 1
        res
      }
    }
  }
  
  def getTermIterator: Iterator[Term] = {
    new Iterator[Term] {
      private var index = 0
      
      override def hasNext: Boolean = index < arity
      
      override def next(): Term = {
        val res = Struct.this.args(index)
        index += 1
        res
      }
    }
  }
  
  /**
    * The reference indicator of the Struct
    *
    * @return "name/arity" of the struct
    */
  def getPredicateIndicator: String = s"$name/$arity"
  
  private[core] def getPrimitive: Option[Primitive] = Option(primitive)
  
  def setPrimitive(primitive: Option[Primitive]): Unit = {
    primitive match {
      case None =>
      case Some(p) => this.primitive = p
    }
  }
  
  /**
    * Gets the internal string representation of this List
    *
    * @return string representation of this List's internals
    */
  private def internalListString: String = {
    val h = args(0).getBinding
    val t = args(1).getBinding
    if (t.isList) {
      val tl = t.asInstanceOf[Struct]
      if (tl.isEmptyList) {
        h.toString
      } else {
        h + "," + tl.internalListString
      }
    } else {
      h + "|" + t
    }
  }
  
  
  /**
    * Gets the internal string representation of this DSG
    *
    * @return string representation of this DSG's internals
    */
  private def internalDsgString: String = {
    arity match {
      case 0 => ""
      case 1 if args(0).isInstanceOf[Struct] && args(0).asInstanceOf[Struct].name != "," =>
        args(0).getBinding.toString
      case _ => // comma case
        var head = args(0).asInstanceOf[Struct].args(0)
        var tail = args(0).asInstanceOf[Struct].args(1)
        
        val buf = new StringBuilder(head.toString)
        
        while (tail.isInstanceOf[Struct] && tail.asInstanceOf[Struct].name == ",") {
          head = tail.asInstanceOf[Struct].args(0)
          buf.append("," + head)
          tail = tail.asInstanceOf[Struct].args(1)
        }
        
        buf.append("," + tail)
        buf.toString()
    }
  }
  
  /**
    * Gets the string representation of this structure
    *
    * Specific representations are provided for lists and atoms.
    * Names starting with upper case letter are enclosed in single quotes.
    *
    * @return string representation of this struct
    */
  override def toString: String = {
    if (isEmptyList) { // empty list case
      "[]"
    } else if (name == "." && arity == 2) { // list case
      "[" + internalListString + "]"
    } else if (name == "{}") {
      "{" + internalDsgString + "}"
    } else {
      val s = if (Parser.isAtom(name)) name else "'" + name + "'"
      s + (if (args.nonEmpty) args.map(_.getBinding).mkString("(", ",", ")") else "")
    }
  }
  
  /**
    * gets a copy (with renamed variables) of the term.
    *
    * The list argument passed contains the list of variables to be renamed (if empty list then no renaming).
    *
    * Used By The engine to initialise it's stack
    *
    * @param vMap      variables to rename
    * @param idExecCtx Execution Context identifier
    * @return Copy of Term
    */
  override def copy(vMap: util.AbstractMap[Var, Var], idExecCtx: scala.Int): Term = {
    val t = new Struct(name, arity,args.map(arg => arg.copy(vMap,idExecCtx)))
    t.resolved = resolved
    t.primitive = primitive
    t
  }
  
  /**
    * gets a copy for result.
    */
  override private[data] def copy(vMap: util.AbstractMap[Var, Var], substMap: util.AbstractMap[Term, Var]) = {
    val t = new Struct(name, arity,args.map(arg => arg.copy(vMap,substMap)))
    t.resolved = false
    t.primitive = null
    t
  }
  
  /**
    * Tries to unify two terms, given a demonstration context identified by the mark integer.
    *
    * Try the unification among the term and the term specified
    *
    * @param vl1 Vars unified in myself
    * @param vl2 Vars unified in term t
    * @param t
    * @param isOccursCheckEnabled
    * @return true if the term is unifiable with this one
    */
  override def unify(vl1: util.List[Var], vl2: util.List[Var], t: Term, isOccursCheckEnabled: Boolean): Boolean = {
    // During the unification phase it is necessary to note all the variables of the complete struct
    t.getBinding match {
      case struct : Struct =>
       arity == struct.arity && name.equals(struct.name) &&
         getTermIterator.zipWithIndex.forall{case (arg,i) => arg.unify(vl1, vl2,struct.args(i),isOccursCheckEnabled)}
      case v : Var =>
        v.unify(vl2, vl1, this, isOccursCheckEnabled)
      case _ => false
    }
  }
}
