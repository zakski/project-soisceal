/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.core.lib

import java.lang.reflect.InvocationTargetException
import java.util._

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.{BuiltIn, Prolog}

/**
 * Administration of primitive predicates
 * @author Alex Benini
 */
class PrimitiveManager(vm: Prolog) {
  private val libHashMap = Collections.synchronizedMap(new IdentityHashMap[Library, List[PrimitiveInfo]])
  private val directiveHashMap = Collections.synchronizedMap(new HashMap[String, PrimitiveInfo])
  private val predicateHashMap = Collections.synchronizedMap(new HashMap[String, PrimitiveInfo])
  private val functorHashMap = Collections.synchronizedMap(new HashMap[String, PrimitiveInfo])
  initialize(vm)
  /**
   * Config this Manager
   */
  private def initialize(vm: Prolog) {
    createPrimitiveInfo(new BuiltIn(vm))
  }

  private[gospel] def createPrimitiveInfo(src: Library) {
    val prims: Map[Integer, List[PrimitiveInfo]] = src.getPrimitives
    var it: Iterator[PrimitiveInfo] = prims.get(PrimitiveInfo.DIRECTIVE).iterator
    while (it.hasNext) {
      val p: PrimitiveInfo = it.next
      directiveHashMap.put(p.getKey, p)
    }
    it = prims.get(PrimitiveInfo.PREDICATE).iterator
    while (it.hasNext) {
      val p: PrimitiveInfo = it.next
      predicateHashMap.put(p.getKey, p)
    }
    it = prims.get(PrimitiveInfo.FUNCTOR).iterator
    while (it.hasNext) {
      val p: PrimitiveInfo = it.next
      functorHashMap.put(p.getKey, p)
    }
    val primOfLib: List[PrimitiveInfo] = new LinkedList[PrimitiveInfo](prims.get(PrimitiveInfo.DIRECTIVE))
    primOfLib.addAll(prims.get(PrimitiveInfo.PREDICATE))
    primOfLib.addAll(prims.get(PrimitiveInfo.FUNCTOR))
    libHashMap.put(src, primOfLib)
  }

  private[gospel] def deletePrimitiveInfo(src: Library) {
    val it: Iterator[PrimitiveInfo] = libHashMap.remove(src).iterator
    while (it.hasNext) {
      val k: String = it.next.invalidate
      directiveHashMap.remove(k)
      predicateHashMap.remove(k)
      functorHashMap.remove(k)
    }
  }

  /**
   * Identifies the term passed as argument.
   *
   * This involves identifying structs representing builtin
   * predicates and functors, and setting up related structures and links
   *
   * @param term the term to be identified
   * @return term with the identified built-in directive
   */
  def identifyDirective(term: Term): Term = {
    identify(term, PrimitiveInfo.DIRECTIVE)
    return term
  }

  @throws(classOf[Throwable])
  def evalAsDirective(d: Struct): Boolean = {
    val pd: PrimitiveInfo = identifyDirective(d).asInstanceOf[Struct].getPrimitive
    if (pd != null) {
      try {
        pd.evalAsDirective(d)
        true
      }
      catch {
        case ite: InvocationTargetException => {
          throw ite.getTargetException
        }
      }
    }
    else {
      false
    }
  }

  def identifyPredicate(term: Term) {
    identify(term, PrimitiveInfo.PREDICATE)
  }

  def identifyFunctor(term: Term) {
    identify(term, PrimitiveInfo.FUNCTOR)
  }

  private def identify(theTerm: Term, typeOfPrimitive: Int) {
    if (theTerm == null) {
      return
    }
    val term  = theTerm.getTerm
    if (!term.isInstanceOf[Struct]) {
      return
    }
    val t: Struct = term.asInstanceOf[Struct]
    val arity: Int = t.getArity
    val name: String = t.getName
    if ((name == ",") || (name == "':-'") || (name == ":-")) {
      for (c <- 0 until arity) {
        identify(t.getArg(c), PrimitiveInfo.PREDICATE)
      }
    } else {
       for (c <- 0 until arity) {
             identify(t.getArg(c), PrimitiveInfo.FUNCTOR)
       }
    }
    var prim: PrimitiveInfo = null
    val key: String = name + "/" + arity
    typeOfPrimitive match {
      case PrimitiveInfo.DIRECTIVE =>
        prim = directiveHashMap.get(key)
      case PrimitiveInfo.PREDICATE =>
        prim = predicateHashMap.get(key)
      case PrimitiveInfo.FUNCTOR =>
        prim = functorHashMap.get(key)
    }
    t.setPrimitive(prim)
  }

  private[gospel] def getLibraryDirective(name: String, nArgs: Int): Library = {
    try {
      return directiveHashMap.get(name + "/" + nArgs).getSource.asInstanceOf[Library]
    }
    catch {
      case e: NullPointerException => {
        null
      }
    }
  }

  private[gospel] def getLibraryPredicate(name: String, nArgs: Int): Library = {
    try {
      predicateHashMap.get(name + "/" + nArgs).getSource.asInstanceOf[Library]
    }
    catch {
      case e: NullPointerException => {
        null
      }
    }
  }

  private[gospel] def getLibraryFunctor(name: String, nArgs: Int): Library = {
    try {
      functorHashMap.get(name + "/" + nArgs).getSource.asInstanceOf[Library]
    }
    catch {
      case e: NullPointerException => {
        null
      }
    }
  }

  def containsTerm(name: String, nArgs: Int): Boolean = {
    functorHashMap.containsKey(name + "/" + nArgs) || predicateHashMap.containsKey(name + "/" + nArgs)
  }
}