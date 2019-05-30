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
package com.szadowsz.gospel.core.db.primitives

import java.lang.reflect.InvocationTargetException

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.db.libraries.{Library, LibraryPredicateFilter}

import scala.collection.mutable
import scala.collection.concurrent

/**
  * Administrator of primitive predicate objects.
  *
  * Created on 16/02/2017.
  */
class PrimitivesManager(wam : Interpreter) extends Serializable {
  private val libs = mutable.HashMap[Library, List[Primitive]]()
  private val directives = concurrent.TrieMap[String,Primitive]()
  private val predicates = concurrent.TrieMap[String,Primitive]()
  private val functors = concurrent.TrieMap[String,Primitive]()

  private def identify(term: Term, typeOfPrimitive: PrimitiveType): Unit = {
    term.getBinding match {
          case s: Struct =>
            val arity = s.getArity
            val name = s.getName
            if ((name == ",") || (name == "':-'") || (name == ":-")) {
              for (c <- 0 until arity) {
                identify(s(c), PrimitiveType.PREDICATE)
              }
            } else {
              for (c <- 0 until arity) {
                identify(s(c), PrimitiveType.FUNCTOR)
              }
            }
            val key = s.getPredicateIndicator
            s.setPrimitive(typeOfPrimitive match {
              case PrimitiveType.DIRECTIVE => directives.get(key)
              case PrimitiveType.PREDICATE => predicates.get(key)
              case PrimitiveType.FUNCTOR => functors.get(key)
            })
          case _ =>
        }
  }


  def unbindLibrary(lib: Library): Unit = {
    synchronized {
      libs.remove(lib).foreach(prims => prims.foreach { p =>
        directives.remove(p.getKey).orElse(predicates.remove(p.getKey)).orElse(functors.remove(p.getKey))
      })
    }

  }

  

  def bindLibrary(lib: Library, filter : LibraryPredicateFilter):Unit = {
    synchronized {
      val prims = lib.getPrimitives
      val primOfLib = prims.values.flatten.toList
      val organise = (m : concurrent.TrieMap[String, Primitive], p : Primitive) => {
        if (filter.retainPredicate(p.getKey)) {
          m.put(filter.mapKey(p.getKey),p)
        }
      }

      prims.get(PrimitiveType.DIRECTIVE).foreach(dirs => dirs.foreach(p => organise(directives,p)))
      prims.get(PrimitiveType.PREDICATE).foreach(preds => preds.foreach(p => organise(predicates,p)))
      prims.get(PrimitiveType.FUNCTOR).foreach(funs => funs.foreach(p => organise(functors,p)))

      libs += (lib -> primOfLib)
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
  def identifyPredicate(term: Term): Unit = identify(term, PrimitiveType.PREDICATE)

  /**
    * Identifies the directive term passed as argument.
    *
    * This involves identifying structs representing built-in predicates and functors, and setting up related structures and links.
    *
    * @param term the term to be identified
    * @return term with the identified built-in directive
    */
  def identifyDirective(term: Term): Struct = {
    identify(term, PrimitiveType.DIRECTIVE)
    term.asInstanceOf[Struct]
  }

  /**
    * Executes Directive when picked up by Theory/Library Manager
    *
    * @param d the directive to execute
    * @return true if successful, false otherwise
    */
  def evalAsDirective(d: Struct): Boolean = {
    identifyDirective(d).getPrimitive match {
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
}
