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
package com.szadowsz.gospel.core.db.libraries

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.{Number, Struct, Term}
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.db.primitives.{Primitive, PrimitiveType, PrimitivesManager}
import com.szadowsz.gospel.core.db.theory.{Theory, TheoryManager}
import com.szadowsz.gospel.core.engine.flags.FlagManager
import com.szadowsz.gospel.core.exception.library.LibraryInstantiationException
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.runtime.universe._
import scala.util.control.NonFatal

object Library {
  
  private val annotations = List("directive", "functor", "predicate")
  
  def extractLibNameFromTheory(th: Theory)(implicit opManager: OperatorManager): String = {
    try {
      val directive = th.iterator().next().asInstanceOf[Struct]
      val hasDirName = "':-'" == directive.getName || ":-" == directive.getName
      
      if (hasDirName && directive.getArity == 1 && directive.getTerm(0).isInstanceOf[Struct]) {
        val moduleDir: Struct = directive(0).asInstanceOf[Struct]
        
        if (moduleDir.getName == "module" && moduleDir.getArity >= 1) {
          moduleDir(0).asInstanceOf[Struct].getName
        } else {
          throw new LibraryInstantiationException(th.getResourceName.orNull, "Cannot find library name defined in Theory")
        }
      } else {
        throw new LibraryInstantiationException(th.getResourceName.orNull, "Cannot find library name defined in Theory")
      }
    } catch {
      case ile: LibraryInstantiationException => throw ile
      case NonFatal(ex) => throw new LibraryInstantiationException(th.getResourceName.orNull, "Cannot find library name defined in Theory")
    }
  }
}

abstract class Library(wam: Interpreter) {
  protected lazy implicit val opManager: OperatorManager = wam.getOperatorManager
  protected lazy implicit val libManager: LibraryManager = wam.getLibraryManager
  protected lazy implicit val primManager: PrimitivesManager = wam.getPrimitiveManager
  protected lazy implicit val thManager: TheoryManager = wam.getTheoryManager
  protected lazy implicit val flagManager : FlagManager = wam.getFlagManager
  
  protected lazy val logger : Logger = LoggerFactory.getLogger(getClass)
  
  def getName: String = getClass.getSimpleName
  
  /**
    * Method returns the theory provided by the library
    *
    * Empty theory is provided by default.
    *
    * @return An Empty Theory object if the library is all primitives, otherwise an Instantiated Theory.
    */
  def getTheory: Option[Theory] = None
  
  private def findMethods(mirror: Mirror, c: Class[_]): Iterable[MethodSymbol] = {
    val sym = mirror.staticClass(c.getName)
    sym.typeSignature.members.collect { case m: MethodSymbol => m }
    //sym.selfType.decls.collect { case m: MethodSymbol => m }
  }
  
  private def findClauses: Seq[Primitive] = {
    val mirror = runtimeMirror(getClass.getClassLoader) // obtain runtime mirror
    val inst = mirror.reflect(this)
    
    val methods = findMethods(mirror, getClass)
    logger.debug(s"Found ${methods.size} Methods")
    
    val clauses = methods.filter(m => m.annotations.exists(a =>
      Library.annotations.contains(a.tree.tpe.typeSymbol.name.toString))
    )
    logger.debug(s"Found ${clauses.size} Annotated Methods")
  
  
    val funcs = clauses.flatMap { m =>
      val methodName = m.name.toString.reverse.dropWhile(c => c.isDigit || c == '_').reverse
      logger.debug(s"Mapping $methodName")
      
      val methodFunction = inst.reflectMethod(m).apply()
      
      val pClauseAnnotations = m.annotations.filter(a => a.tree.tpe <:< typeOf[clause])
      val pClauses = pClauseAnnotations.zip(
        pClauseAnnotations.flatMap(a => a.tree.children.tail.collect { case Literal(Constant(args: Int)) => args })
      )
      pClauses.flatMap { case (a, args) =>
        val annotationName = a.tree.tpe.typeSymbol.name.toString
        val aliases = a.tree.children.tail.collect { case Literal(Constant(aliases: Array[String])) => args }
        (annotationName, s"$methodName/$args", methodFunction) +: aliases.map(alt => (annotationName, s"$alt/$args", methodFunction))
      }
    }
    logger.debug(s"Mapped ${funcs.size} Functions")
    funcs.map { case (t, id, f) => Primitive(PrimitiveType.forName(t), id, this, f) }.toSeq
  }
  
  /**
    * gets the list of primitives defined in the library
    */
  private[core] def getPrimitives: Map[PrimitiveType, Seq[Primitive]] = {
    findClauses.groupBy(_.getType)
  }
  
  
  /**
    * Method invoked when the engine is going to demonstrate a goal
    *
    * @param goal the goal to be demonstrated
    */
  def onSolveBegin(goal: Term): Unit = {}
  
  /**
    * Method invoked when the engine has finished a demonstration due to an exception
    */
  def onSolveHalt(): Unit = {}
  
  /**
    * Method invoked when the engine has finished a demonstration normally
    */
  def onSolveEnd(): Unit = {}
  
  /**
    * Method invoked by prolog engine when library is going to be removed
    */
  def dismiss(): Unit = {}
  
  /**
    * Evaluates an expression. Returns null value if the argument is not an evaluable expression
    *
    * The runtime (demo) context currently used by the engine is deployed and altered.
    *
    * @throws Throwable
    */
  @throws[Throwable]
  protected def evalExpression(term: Term): Option[Term] = {
    Option(term) match {
      case None => None
      case _ =>
        term.getBinding match {
          case struct: Struct =>
            if (!struct.isPrimitive) {
              primManager.identifyFunctor(struct)
            }
            
            if (struct.isPrimitive) {
              struct.getPrimitive match {
                case Some(bt) if bt.isFunctor => // check for library functors
                  Option(bt.evalAsFunctor(struct))
                case None => None
              }
            } else {
              None
            }
          case n: Number => Option(n)
        }
    }
  }
}
