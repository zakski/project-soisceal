/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  * <p>
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * <p>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * <p>
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
package com.szadowsz.gospel.core.db

import java.lang.reflect.Method
import java.util

import alice.tuprolog.IPrimitives
import com.szadowsz.gospel.core.PrologEngine
import com.szadowsz.gospel.core.data.{Number, Struct, Term}
import com.szadowsz.gospel.core.db.primitives.{JPrimitive, PrimitiveInfo, SPrimitive}

import scala.collection.JavaConverters._

sealed abstract class Library extends IPrimitives {

  /**
    * prolog core which loaded the library
    */
  protected var engine: PrologEngine = _

  /**
    * Gets the engine to which the library is bound
    *
    * @return the engine
    */
  protected def getEngine: PrologEngine = engine

  /**
    * @param en
    */
  protected[db] def setEngine(en: PrologEngine): Unit = {
    engine = en
  }

  /**
    * tries to unify two terms
    *
    * The runtime (demonstration) context currently used by the engine
    * is deployed and altered.
    */
  protected def unify(a0: Term, a1: Term): Boolean = engine.unify(a0, a1)

  protected def `match`(a0: Term, a1: Term): Boolean = engine.`match`(a0, a1)

  /**
    * Gets the theory provided with the library
    *
    * Empty theory is provided by default.
    */
  def getTheory = ""

  /**
    * Gets the name of the library.
    *
    * By default the name is the class name.
    *
    * @return the library name
    */
  def getName: String = getClass.getName

  /**
    * method invoked when the engine is going to demonstrate a goal
    *
    * @param goal the goal to be demonstrated
    */
  def onSolveBegin(goal: Term): Unit = {}

  /**
    * method invoked when the engine has finished a demonstration
    */
  def onSolveHalt(): Unit = {}

  def onSolveEnd(): Unit = {}

  /**
    * method invoked by prolog engine when library is
    * going to be removed
    */
  def dismiss(): Unit = {}

  /**
    * Evaluates an expression. Returns null value if the argument
    * is not an evaluable expression
    *
    * The runtime (demo) context currently used by the engine
    * is deployed and altered.
    *
    * @throws Throwable
    */
  @throws[Throwable]
  protected def evalExpression(term: Term): Term = {
    if (term == null) {
      null
    } else {
      term.getTerm match {
        case struct: Struct =>
          if (!struct.isPrimitive) {
              engine.identifyFunctor(struct)
          }
          if (struct.isPrimitive) {
            val bt = struct.getPrimitive
            // check for library functors
            if (bt.isFunctor) {
              return bt.evalAsFunctor(struct)
            }
          }
        case n: Number =>
          return n
      }
      null
    }
  }
}

abstract class JLibrary extends Library {

  /**
    * operator mapping
    */
  private var opMappingCached: Array[Array[String]] = getSynonymMap


  /**
    * Gets the synonym mapping, as array of
    * elements like  { synonym, original name}
    */
  def getSynonymMap: Array[Array[String]] = null


  private def getPrimitiveType(m: Method): Int = {
    m.getReturnType.getName match {
      case "boolean" => PrimitiveInfo.PREDICATE
      case "com.szadowsz.gospel.core.data.Term" => PrimitiveInfo.FUNCTOR
      case "void" => PrimitiveInfo.DIRECTIVE
      case default => -1
    }
  }


  /**
    * gets the list of primitives defined in the library
    */
  override final def getPrimitives: util.Map[Integer, util.List[PrimitiveInfo]] = try {
    val mlist = this.getClass.getMethods

    val mapPrimitives = new util.HashMap[Integer, util.List[PrimitiveInfo]]
    mapPrimitives.put(PrimitiveInfo.DIRECTIVE, new util.ArrayList[PrimitiveInfo])
    mapPrimitives.put(PrimitiveInfo.FUNCTOR, new util.ArrayList[PrimitiveInfo])
    mapPrimitives.put(PrimitiveInfo.PREDICATE, new util.ArrayList[PrimitiveInfo])

    //{new ArrayList<PrimitiveInfo>(), new ArrayList<PrimitiveInfo>(), new ArrayList<PrimitiveInfo>()};
    for (aMlist <- mlist) {
      val name = aMlist.getName
      val clist = aMlist.getParameterTypes
      val primType = getPrimitiveType(aMlist)
      if (primType >= 0) {
        val index = name.lastIndexOf('_')
        if (index != -1) {
          try {
            val arity = name.substring(index + 1, name.length).toInt
            // check arg number
            if (clist.length == arity && clist.forall(classOf[Term].isAssignableFrom(_))) {
              val rawName = name.substring(0, index)
              var key = rawName + "/" + arity
              var prim = new JPrimitive(primType, key, this, aMlist, arity)
              mapPrimitives.get(primType).add(prim)
              //
              // adding also or synonims
              //
              val stringFormat = Array("directive", "predicate", "functor")
              if (opMappingCached != null) for (map <- opMappingCached) {
                if (map(2) == stringFormat(primType) && map(1) == rawName) {
                  key = map(0) + "/" + arity
                  prim = new JPrimitive(primType, key, this, aMlist, arity)
                  mapPrimitives.get(primType).add(prim)
                }
              }
            }
          } catch {
            case ex: Exception =>

          }
        }
      }
    }
    mapPrimitives
  }

}

abstract class SLibrary extends Library {

  protected def getDirectives : List[(String,AnyRef)]

  protected def getFunctors : List[(String,AnyRef)]

  protected def getPredicates  : List[(String,AnyRef)]

  /**
    * gets the list of primitives defined in the library
    */
  override def getPrimitives: util.Map[Integer, util.List[PrimitiveInfo]] = {
    val primitives = new util.HashMap[Integer, util.List[PrimitiveInfo]]()

    primitives.put(PrimitiveInfo.DIRECTIVE,getDirectives.map{ case (k : String, f : AnyRef) =>
      SPrimitive(PrimitiveInfo.DIRECTIVE,k,this,f).asInstanceOf[PrimitiveInfo]}.asJava
    )

    primitives.put(PrimitiveInfo.FUNCTOR,getFunctors.map{ case (k : String, f : AnyRef) =>
      SPrimitive(PrimitiveInfo.FUNCTOR,k,this,f).asInstanceOf[PrimitiveInfo]}.asJava
    )

    primitives.put(PrimitiveInfo.PREDICATE,getPredicates.map{ case (k : String, f : AnyRef) =>
      SPrimitive(PrimitiveInfo.PREDICATE,k,this,f).asInstanceOf[PrimitiveInfo]}.asJava
    )

    primitives
  }
}
