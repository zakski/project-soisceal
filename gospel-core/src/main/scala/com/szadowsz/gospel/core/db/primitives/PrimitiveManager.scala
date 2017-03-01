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
package com.szadowsz.gospel.core.db.primitives

import java.lang.reflect.InvocationTargetException
import java.{util => ju}

import alice.tuprolog.{IPrimitives, Library, PrimitiveInfo, Struct, Term}
import com.szadowsz.gospel.core.PrologEngine
import com.szadowsz.gospel.core.db.libs.BuiltIn

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Administration of primitive predicate objects.
  *
  * Created on 16/02/2017.
  *
  * @version Gospel 2.0.0
  */

final case class PrimitiveManager(vm: PrologEngine) extends java.io.Serializable {
  private val libs = new mutable.HashMap[IPrimitives, List[PrimitiveInfo]]()
  private val directives = ju.Collections.synchronizedMap(new ju.HashMap[String, PrimitiveInfo])
  private val predicates = ju.Collections.synchronizedMap(new ju.HashMap[String, PrimitiveInfo])
  private val functors = ju.Collections.synchronizedMap(new ju.HashMap[String, PrimitiveInfo])
  createPrimitiveInfo(new BuiltIn(vm))

  private def identify(term: Term, typeOfPrimitive: Int): Unit = {
    Option(term) match {
      case None =>
      case Some(t) =>
        t.getTerm match {
          case s: Struct =>
            val arity = s.getArity
            val name = s.getName
            if ((name == ",") || (name == "':-'") || (name == ":-")) for (c <- 0 until arity) {
              identify(s.getArg(c), PrimitiveInfo.PREDICATE)
            } else for (c <- 0 until arity) {
              identify(s.getArg(c), PrimitiveInfo.FUNCTOR)
            }
            var prim : PrimitiveInfo = null
            val key = name + "/" + arity
            typeOfPrimitive match {
              case PrimitiveInfo.DIRECTIVE =>
                prim = directives.get(key)
              case PrimitiveInfo.PREDICATE =>
                prim = predicates.get(key)
              case PrimitiveInfo.FUNCTOR =>
                prim = functors.get(key)
            }
            s.setPrimitive(prim)
          case _ =>
        }
    }
  }

  def getLibraryDirective(name: String, nArgs: Int): Library = Try(directives.get(name + "/" + nArgs).getSource.asInstanceOf[Library]).toOption.orNull

  def getLibraryPredicate(name: String, nArgs: Int): Library = Try(predicates.get(name + "/" + nArgs).getSource.asInstanceOf[Library]).toOption.orNull

  def getLibraryFunctor(name: String, nArgs: Int): Library = Try(functors.get(name + "/" + nArgs).getSource.asInstanceOf[Library]).toOption.orNull

  def createPrimitiveInfo(src: IPrimitives): Unit = {
    synchronized {
      val prims = src.getPrimitives.asScala.map { case (i: Integer, l: ju.List[PrimitiveInfo]) => i -> l.asScala.toList }
      val primOfLib = prims.values.flatten.toList
      prims(PrimitiveInfo.DIRECTIVE).foreach(p => directives.put(p.getKey, p))
      prims(PrimitiveInfo.PREDICATE).foreach(p => predicates.put(p.getKey, p))
      prims(PrimitiveInfo.FUNCTOR).foreach(p => functors.put(p.getKey, p))
      libs += (src -> primOfLib)
    }
  }

  def deletePrimitiveInfo(src: IPrimitives): Unit = {
    synchronized {
      libs.remove(src).foreach(prims => prims.foreach { p =>
        directives.remove(p.getKey)
        predicates.remove(p.getKey)
        functors.remove(p.getKey)
      })
    }
  }

  def containsTerm(name: String, nArgs: Int): Boolean = functors.containsKey(name + "/" + nArgs) || predicates.containsKey(name + "/" + nArgs)

  @throws(classOf[Throwable])
  def evalAsDirective(d: Struct): Boolean = {
    Option(identifyDirective(d).getPrimitive) match {
      case Some(pd) =>
        try {
          pd.evalAsDirective(d)
          true
        } catch {
          case ite: InvocationTargetException => throw ite.getTargetException
        }
      case None => false
    }
  }

  /**
    * Identifies the predicate term passed as argument.
    *
    * This involves identifying structs representing built-in predicates and functors, and setting up related structures and links.
    *
    * @param term the term to be identified
    * @return term with the identified built-in predicate
    */
  def identifyPredicate(term: Term): Unit = identify(term, PrimitiveInfo.PREDICATE)

  /**
    * Identifies the directive term passed as argument.
    *
    * This involves identifying structs representing built-in predicates and functors, and setting up related structures and links.
    *
    * @param term the term to be identified
    * @return term with the identified built-in directive
    */
  def identifyDirective(term: Term): Struct = {
    identify(term, PrimitiveInfo.DIRECTIVE)
    term.asInstanceOf[Struct]
  }

  /**
    * Identifies the functor term passed as argument.
    *
    * This involves identifying structs representing built-in predicates and functors, and setting up related structures and links.
    *
    * @param term the term to be identified
    * @return term with the identified built-in functor
    */
  def identifyFunctor(term: Term): Unit = identify(term, PrimitiveInfo.FUNCTOR)
}