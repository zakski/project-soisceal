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

import com.szadowsz.gospel.core.parser.Parser

class Struct(val name: String, val arity: scala.Int, val args: List[Term] = Nil) extends Term {

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
    //    for (i <- argList.indices) {
    //      if (argList(i) == null) {
    //        throw new InvalidTermException("Arguments of a Struct cannot be null",f)
    //      } else {
    //        arg(i) = argList(i)
    //      }
    //    }
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

  /**
    * Resolves variables inside the term
    *
    * If the variables has been already resolved, no renaming is done.
    */
  override def resolveTerm(): Unit = {

  }


  override def isEmptyList: Boolean = name == "[]" && arity == 0

  override def isList: Boolean = (name == "." && arity == 2 && args(1).isList) || isEmptyList

  override def isEquals(other: Term): Boolean = {
    other match {
      case s: Struct =>
        name == s.name && arity == s.arity && args.sameElements(s.args)
      case _ => false
    }
  }

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
}
