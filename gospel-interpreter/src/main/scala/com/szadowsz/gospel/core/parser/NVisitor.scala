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
package com.szadowsz.gospel.core.parser

import java.util.stream.Stream

import com.szadowsz.gospel.core.data.{Float, Int, Struct, Term, Var}
import com.szadowsz.gospel.core.parser.Associativity._
import com.szadowsz.gospel.core.parser.PrologParser.{ExpressionContext, IntegerContext, ListContext, OpContext, RealContext, StructureContext}
import org.antlr.v4.runtime.RuleContext

import scala.collection.mutable
import scala.compat.java8.StreamConverters._
import scala.jdk.CollectionConverters._

class NVisitor extends PrologParserBaseVisitor[Term] {
  
  private val variables = mutable.Map[String,Var]()
  
  override def visitClause(ctx: PrologParser.ClauseContext): Term = {
    ctx.expression.accept(this)
  }
  
  override def visitSingletonTerm(ctx: PrologParser.SingletonTermContext): Term = {
    visitTerm(ctx.term)
  }
  
  override def visitSingletonExpression(ctx: PrologParser.SingletonExpressionContext): Term = {
    visitExpression(ctx.expression)
  }
  
  override def visitTerm(ctx: PrologParser.TermContext): Term = {
    if (ctx.isExpr) {
      visitExpression(ctx.expression)
    } else {
      ctx.children.get(0).accept(this)
    }
  }
  
  override def visitSet(ctx: PrologParser.SetContext): Term = {
    Struct.set(ctx.items.stream.map(this.visitExpression).toScala(Seq):_*)
  }
  
  override def visitVariable(ctx: PrologParser.VariableContext): Term = {
    ctx.value.getText match {
      case "_" => new Var()
      case _ =>
        variables.get(ctx.value.getText) match {
          case None =>
            val v = new Var(ctx.value.getText)
            variables += (v.getName -> v)
            v
          case Some(v) => v
        }
    }
  }
  
  private def streamOfOperands(ctx: PrologParser.ExpressionContext) : Seq[Term] = {
    val sOp : LazyList[RuleContext] = Stream.concat[RuleContext](Stream.of(ctx.left), ctx.right.stream).toScala(LazyList)
    sOp.map(rc => rc.accept(this))
  }
  
  private def streamOfOperands(result : Term, ctx: PrologParser.OuterContext) : Seq[Term] = {
    val sOp : LazyList[ExpressionContext] = ctx.right.stream.toScala(LazyList)
    result +: sOp.map(rc => rc.accept(this))
  }
  
  private def streamOfOperators(ctx: PrologParser.ExpressionContext): Seq[String] = {
    val sOp : LazyList[OpContext] = ctx.operators.stream.toScala(LazyList)
    sOp.map(op => op.symbol.getText)
  }
  
  private def streamOfOperators(ctx: PrologParser.OuterContext): Seq[String] = {
    val sOp : LazyList[OpContext] = ctx.operators.stream.toScala(LazyList)
    sOp.map(op => op.symbol.getText)
  }
  
  private def infixRight(operands : Seq[Term], operators : Seq[String]) : Term = {
    var i = operators.length - 1
    operands.reduceRight[Term]{case (operand, result) =>
      val next = new Struct(operators(i),operand, result)
      i -= 1
      next
    }
  }
  
  private def visitInfixRightAssociativeExpression(ctx: PrologParser.ExpressionContext):Term = {
    infixRight(streamOfOperands(ctx), streamOfOperators(ctx))
  }
  
  private def infixLeft(operands : Seq[Term], operators : Seq[String]) : Term = {
    var i = 0
    operands.reduceLeft[Term]{case (result, operand) =>
      val next = new Struct(operators(i),result,operand)
      i += 1
      next
    }
  }

  private def visitInfixLeftAssociativeExpression(ctx: PrologParser.ExpressionContext): Term  = {
    infixLeft(streamOfOperands(ctx), streamOfOperators(ctx))
  }
  
  private def infixNonAssociative(operands : Seq[Term], operators : Seq[String]) : Term = {
    new Struct(operators(0), operands(0), operands(1))
  }

  private def visitInfixNonAssociativeExpression(ctx: PrologParser.ExpressionContext): Term = {
   infixNonAssociative(streamOfOperands(ctx), streamOfOperators(ctx))
  }
  

  private def visitInfixExpression(ctx: PrologParser.ExpressionContext): Term = {
    ctx.associativity match {
      case XFY => visitInfixRightAssociativeExpression(ctx)
      case YFX => visitInfixLeftAssociativeExpression(ctx)
      case XFX => visitInfixNonAssociativeExpression(ctx)
      case _ => throw new IllegalStateException
    }
  }
  
  private def postfix(term : Term, operators : Seq[String]) : Term = {
    operators.foldLeft(term){case (result, operator) => new Struct(operator,result)}
  }
  private def visitPostfixExpression(ctx: PrologParser.ExpressionContext): Term = {
    postfix(ctx.left.accept(this), streamOfOperators(ctx))
  }
  
  private def prefix(term : Term, operators : Seq[String]) : Term = {
    operators.foldRight(term){case (operator, result) => new Struct(operator,result)}
  }

  private def visitPrefixExpression(ctx: PrologParser.ExpressionContext): Term = {
    prefix(ctx.right.get(0).accept(this), streamOfOperators(ctx))
  }

  private def handleOuters(expression: Term, outers: Seq[PrologParser.OuterContext]): Term = {
    var result: Term = expression

    for (outer : PrologParser.OuterContext <- outers){
      val operands = streamOfOperands(result,outer)
      val operators = streamOfOperators(outer)
      outer.associativity match {
        case XFY =>
          result = infixRight(operands, operators)
        case YFX =>
          result = infixLeft(operands, operators)
        case XFX =>
          result = infixNonAssociative(operands, operators)
        case XF | YF =>
           result = postfix(result, operators)
        case _ =>
          throw new IllegalStateException
      }
    }
    result
  }
  
  private def flatten(outers: Stream[PrologParser.OuterContext]) : Stream[PrologParser.OuterContext] = {
    outers.flatMap((o: PrologParser.OuterContext) => Stream.concat(Stream.of(o), flatten(o.outers.stream)))
  }
  
  override def visitExpression(ctx: PrologParser.ExpressionContext): Term = {
    val result = ctx match {
      case term: Any if term.isTerm => visitTerm(ctx.left)
      case _ =>
        ctx.associativity match {
          case infix: Any if Associativity.INFIX.contains(infix) =>
            visitInfixExpression(ctx)
          case postfix: Any if Associativity.POSTFIX.contains(postfix) =>
            visitPostfixExpression(ctx)
          case prefix: Any if Associativity.PREFIX.contains(prefix) =>
            visitPrefixExpression(ctx)
          case _ =>
            if (ctx.exception != null) {
              throw ctx.exception
            } else {
              throw new IllegalArgumentException
            }
        }
    }
    val outers : Seq[PrologParser.OuterContext] = flatten(ctx.outers.stream()).toScala(LazyList);
    handleOuters(result, outers)
  }
  
  private def parseInteger(ctx : IntegerContext) = {
    val str = ctx.value.getText
    var base = 10
    var clean = ""
   
    if (ctx.isBin) {
      base = 2
      clean = str.substring(2)
    } else if (ctx.isOct) {
      base = 8
      clean = str.substring(2)
    } else if (ctx.isHex) {
      base = 16
      clean = str.substring(2)
    } else if (ctx.isChar) {
      clean = str.substring(2)
      if (clean.length() != 1) {
        throw new ParseException(
          null,
          ctx.getText,
          ctx.value.getLine,
          ctx.value.getCharPositionInLine,
          "Invalid character literal: " + ctx.getText,
          null
        )
      }
      clean = clean.charAt(0).toInt.toString
    } else {
      base = 10
      clean = str
    }
    
    if (ctx.sign != null) {
      clean = ctx.sign.getText + clean
    }
    
    try {
      java.lang.Long.parseLong(clean, base)
    } catch {
      case notEvenLong : NumberFormatException =>
        throw new ParseException(ctx.value, notEvenLong)
    }
  }
  
  override def visitInteger(ctx: IntegerContext): Term = {
    Int(parseInteger(ctx))
  }
  
  override def visitReal(ctx: RealContext): Term = {
    var raw = ctx.value.getText
    if (ctx.sign != null) {
      raw = ctx.sign.getText + raw
    }
    
    try {
      Float(raw.toDouble)
    } catch {
      case notAFloating: NumberFormatException =>
        throw new ParseException(ctx.value, notAFloating)
    }
  }
  
  override def visitStructure(ctx :StructureContext) : Term = {
    if (ctx.isList) {
      new Struct()
    } else if (ctx.isSet) {
      new Struct("{}")
    } else if (ctx.arity == 0) {
      new Struct(ctx.functor.getText)
    } else {
      new Struct(ctx.functor.getText, ctx.args.asScala.map(e => visitExpression(e)).toArray)
    }
  }
  
  override def visitList(ctx : ListContext)  : Term ={
    var terms = ctx.items.asScala.map(e => visitExpression(e)).toList
    
    if (ctx.hasTail) {
      terms = terms :+ visitExpression(ctx.tail)
      terms.reduceRight[Term]{ case (head, tail) => new Struct(head,tail)}
    } else {
      new Struct(terms.toArray)
    }
  }
}
