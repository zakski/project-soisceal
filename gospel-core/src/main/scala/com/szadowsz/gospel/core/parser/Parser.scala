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
 *//*
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
package com.szadowsz.gospel.core.parser

import java.io._
import java.util
import java.util.regex.Pattern

import com.szadowsz.gospel.core.data
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.ops.OperatorManager
import com.szadowsz.gospel.core.error.InvalidTermException

/**
  * This class defines a parser of prolog terms and sentences.
  *
  * Created on 19/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
object Parser {

  private val atom: Pattern = Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)")

  /**
    * Static service to get a term from its string representation,
    * providing a specific operator manager
    */
  @throws[InvalidTermException]
  def parseSingleTerm(st: String, op: OperatorManager): Term = {
    try {
      val p: Parser = new Parser(op, st)
      val t: Token = p.tokenizer.readToken
      if (t.isEOF) throw new InvalidTermException("Term starts with EOF")
      p.tokenizer.unreadToken(t)
      val term: Term = p.expr(false)
      if (term == null) throw new InvalidTermException("Term is null")
      if (!p.tokenizer.readToken.isEOF) throw new InvalidTermException("The entire string could not be read as one term")
      term.resolveTerm()
      term
    } catch {
      case ex: IOException => throw new InvalidTermException("An I/O error occured")
    }
  }

  /**
    * @return true if the String could be a prolog atom
    */
  def isAtom(s: String): Boolean = atom.matcher(s).matches

  private[core] def createNumber(s: String): data.Number = {
    try {
      parseInteger(s)
    } catch {
      case e: Exception => parseFloat(s)
    }
  }

  private[core] def parseInteger(s: String): data.Number = {
    val num: scala.Long = java.lang.Long.parseLong(s)
    if (num >= Integer.MIN_VALUE && num <= Integer.MAX_VALUE) data.Int(num.toInt) else data.Long(num)
  }

  private[core] def parseFloat(s: String): data.Double = data.Double(s.toDouble)
}


@SerialVersionUID(1L)
private[core] class Parser(op: OperatorManager) extends Serializable {
  private val tokenStart: scala.Int = 0
  private var tokenizer: Tokenizer = _
  private var opManager: OperatorManager = op
  /*Castagna 06/2011*/ private var offsetsMap: util.HashMap[Term, Integer] = _

  /**
    * creating a Parser specifing how to handle operators and what text to parse
    */
  def this(op: OperatorManager, theoryText: String) {
    this(op)
    tokenizer = new Tokenizer(theoryText)
  }


  /**
    * creating a Parser specifing how to handle operators and what text to parse
    */
  def this(op: OperatorManager, theoryText: InputStream) {
    this(op)
    tokenizer = new Tokenizer(new BufferedReader(new InputStreamReader(theoryText)))

  }

  /**
    * creating a Parser specifing how to handle operators and what text to parse
    */
  def this(op: OperatorManager, theoryText: String, mapping: util.HashMap[Term, Integer]) {
    this(op)
    tokenizer = new Tokenizer(theoryText)
    offsetsMap = mapping
  }

  def iterator: util.Iterator[Term] = new TermIterator(this)

  /**
    * Parses next term from the stream built on string.
    *
    * @param endNeeded <tt>true</tt> if it is required to parse the end token
    *                  (a period), <tt>false</tt> otherwise.
    * @throws InvalidTermException if a syntax error is found.
    */
  @throws[InvalidTermException]
  def nextTerm(endNeeded: Boolean): Term = {
    try {
      val t: Token = tokenizer.readToken
      if (t.isEOF) return null
      tokenizer.unreadToken(t)
      val term: Term = expr(false)
      if (term == null) /*Castagna 06/2011*/
      //throw new InvalidTermException("The parser is unable to finish");
        throw new InvalidTermException("The parser is unable to finish.", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
      /**/ if (endNeeded && tokenizer.readToken.getType != Tokenizer.END) /*Castagna 06/2011*/
      //throw new InvalidTermException("The term " + term + " is not ended with a period.");
        throw new InvalidTermException("The term '" + term + "' is not ended with a period.", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
      /**/ term.resolveTerm()
      term
    }
    catch {
      case ex: IOException => {
        /*Castagna 06/2011*/
        //throw new InvalidTermException("An I/O error occured.");
        throw new InvalidTermException("An I/O error occured.", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
        /**/
      }
    }
  }

  def getTextMapping: util.HashMap[Term, Integer] = offsetsMap

  def getCurrentLine: scala.Int = tokenizer.lineno

  def getCurrentOffset: scala.Int = tokenizer.tokenOffset

  def offsetToRowColumn(offset: scala.Int): Array[scala.Int] = tokenizer.offsetToRowColumn(offset)

  @throws[InvalidTermException]
  @throws[IOException]
  private def expr(commaIsEndMarker: Boolean): Term = exprA(OperatorManager.OP_HIGH, commaIsEndMarker).result

  @throws[InvalidTermException]
  @throws[IOException]
  private def exprA(maxPriority: scala.Int, commaIsEndMarker: Boolean): IdentifiedTerm = {
    var leftSide: IdentifiedTerm = exprB(maxPriority, commaIsEndMarker)
    //if (leftSide == null)
    //return null;
    //{op(yfx,n) exprA(n-1) | op(yf,n)}*
    var t: Token = tokenizer.readToken
    var break = false
    while (!break && t.isOperator(commaIsEndMarker)) {
      var cont = false
      var YFX: scala.Int = opManager.opPrio(t.seq, "yfx")
      var YF: scala.Int = opManager.opPrio(t.seq, "yf")
      //YF and YFX has a higher priority than the left side expr and less then top limit
      // if (YF < leftSide.priority && YF > OperatorManager.OP_HIGH) YF = -1;
      if (YF < leftSide.priority || YF > maxPriority) YF = -1
      // if (YFX < leftSide.priority && YFX > OperatorManager.OP_HIGH) YFX = -1;
      if (YFX < leftSide.priority || YFX > maxPriority) YFX = -1
      //YFX has priority over YF
      if (YFX >= YF && YFX >= OperatorManager.OP_LOW) {
        val ta: IdentifiedTerm = exprA(YFX - 1, commaIsEndMarker)
        if (ta != null) {
          /*Castagna 06/2011*/
          //leftSide = new IdentifiedTerm(YFX, new Struct(t.seq, leftSide.result, ta.result));
          leftSide = identifyTerm(YFX, new Struct(t.seq, leftSide.result, ta.result), tokenStart)
          cont = true
        }
      }
      //either YF has priority over YFX or YFX failed
      if (!cont && YF >= OperatorManager.OP_LOW) {
        /*Castagna 06/2011*/
        //leftSide = new IdentifiedTerm(YF, new Struct(t.seq, leftSide.result));
        leftSide = identifyTerm(YF, new Struct(t.seq, leftSide.result), tokenStart)
        cont = true
      }
      if (!cont) {
        break = true
      } else {
        t = tokenizer.readToken
      }
    }
    tokenizer.unreadToken(t)
    leftSide
  }

  @throws[InvalidTermException]
  @throws[IOException]
  private def exprB(maxPriority: scala.Int, commaIsEndMarker: Boolean): IdentifiedTerm = {
    //1. op(fx,n) exprA(n-1) | op(fy,n) exprA(n) | expr0
    var left: IdentifiedTerm = parseLeftSide(commaIsEndMarker, maxPriority)
    //2.left is followed by either xfx, xfy or xf operators, parse these
    var operator: Token = tokenizer.readToken
    var break = false
    while (!break & operator.isOperator(commaIsEndMarker)) {
      var cont = false
      var XFX: scala.Int = opManager.opPrio(operator.seq, "xfx")
      var XFY: scala.Int = opManager.opPrio(operator.seq, "xfy")
      var XF: scala.Int = opManager.opPrio(operator.seq, "xf")
      //check that no operator has a priority higher than permitted
      //or a lower priority than the left side expression
      if (XFX > maxPriority || XFX < OperatorManager.OP_LOW) XFX = -1
      if (XFY > maxPriority || XFY < OperatorManager.OP_LOW) XFY = -1
      if (XF > maxPriority || XF < OperatorManager.OP_LOW) XF = -1
      //XFX
      var haveAttemptedXFX: Boolean = false
      if (XFX >= XFY && XFX >= XF && XFX >= left.priority) {
        //XFX has priority
        val found: IdentifiedTerm = exprA(XFX - 1, commaIsEndMarker)
        if (found != null) {
          /*Castagna 06/2011*/
          //Struct xfx = new Struct(operator.seq, left.result, found.result);
          //left = new IdentifiedTerm(XFX, xfx);
          left = identifyTerm(XFX, new Struct(operator.seq, left.result, found.result), tokenStart)
          cont = true
        }
        else haveAttemptedXFX = true
      }
      //XFY
      if (!cont && XFY >= XF && XFY >= left.priority) {
        //XFY has priority, or XFX has failed
        val found: IdentifiedTerm = exprA(XFY, commaIsEndMarker)
        if (found != null) {
          /*Castagna 06/2011*/
          //Struct xfy = new Struct(operator.seq, left.result, found.result);
          //left = new IdentifiedTerm(XFY, xfy);
          left = identifyTerm(XFY, new Struct(operator.seq, left.result, found.result), tokenStart)
          cont = true //todo: continue is not supported
        }
      }
      //XF
      if (!cont && XF >= left.priority) //XF has priority, or XFX and/or XFY has failed
      /*Castagna 06/2011*/
      //return new IdentifiedTerm(XF, new Struct(operator.seq, left.result));
        return identifyTerm(XF, new Struct(operator.seq, left.result), tokenStart)
      /**/
      //XFX did not have top priority, but XFY failed
      if (!cont && !haveAttemptedXFX && XFX >= left.priority) {
        val found: IdentifiedTerm = exprA(XFX - 1, commaIsEndMarker)
        if (found != null) {
          /*Castagna 06/2011*/
          //Struct xfx = new Struct(operator.seq, left.result, found.result);
          //left = new IdentifiedTerm(XFX, xfx);
          left = identifyTerm(XFX, new Struct(operator.seq, left.result, found.result), tokenStart)
          cont = true
        }
      }
      if (!cont) {
        break = true
      } else {
        operator = tokenizer.readToken
      }
    }
    tokenizer.unreadToken(operator)
    left
  }

  /**
    * Parses and returns a valid 'leftside' of an expression.
    * If the left side starts with a prefix, it consumes other expressions with a lower priority than itself.
    * If the left side does not have a prefix it must be an expr0.
    *
    * @param commaIsEndMarker used when the leftside is part of and argument list of expressions
    * @param maxPriority      operators with a higher priority than this will effectivly end the expression
    * @return a wrapper of: 1. term correctly structured and 2. the priority of its root operator
    * @throws InvalidTermException
    */
  @throws[InvalidTermException]
  @throws[IOException]
  private def parseLeftSide(commaIsEndMarker: Boolean, maxPriority: scala.Int): IdentifiedTerm = {
    //1. prefix expression
    val f: Token = tokenizer.readToken
    if (f.isOperator(commaIsEndMarker)) {
      var FX: scala.Int = opManager.opPrio(f.seq, "fx")
      var FY: scala.Int = opManager.opPrio(f.seq, "fy")
      if (f.seq == "-") {
        val t: Token = tokenizer.readToken
        if (t.isNumber) /*Michele Castagna 06/2011*/
        //return new IdentifiedTerm(0, Parser.createNumber("-" + t.seq));
          return identifyTerm(0, Parser.createNumber("-" + t.seq), tokenStart)
        else /**/ tokenizer.unreadToken(t)
      }
      //check that no operator has a priority higher than permitted
      if (FY > maxPriority) FY = -1
      if (FX > maxPriority) FX = -1
      //FX has priority over FY
      var haveAttemptedFX: Boolean = false
      if (FX >= FY && FX >= OperatorManager.OP_LOW) {
        val found: IdentifiedTerm = exprA(FX - 1, commaIsEndMarker) //op(fx, n) exprA(n - 1)
        if (found != null) /*Castagna 06/2011*/
        //return new IdentifiedTerm(FX, new Struct(f.seq, found.result));
          return identifyTerm(FX, new Struct(f.seq, found.result), tokenStart)
        else /**/ haveAttemptedFX = true
      }
      //FY has priority over FX, or FX has failed
      if (FY >= OperatorManager.OP_LOW) {
        val found: IdentifiedTerm = exprA(FY, commaIsEndMarker) //op(fy,n) exprA(1200)  or   op(fy,n) exprA(n)
        if (found != null) /*Castagna 06/2011*/
        //return new IdentifiedTerm(FY, new Struct(f.seq, found.result));
          return identifyTerm(FY, new Struct(f.seq, found.result), tokenStart)
        /**/
      }
      //FY has priority over FX, but FY failed
      if (!haveAttemptedFX && FX >= OperatorManager.OP_LOW) {
        val found: IdentifiedTerm = exprA(FX - 1, commaIsEndMarker) //op(fx, n) exprA(n - 1)
        if (found != null) /*Castagna 06/2011*/
        //return new IdentifiedTerm(FX, new Struct(f.seq, found.result));
          return identifyTerm(FX, new Struct(f.seq, found.result), tokenStart)
        /**/
      }
    }
    tokenizer.unreadToken(f)
    //2. expr0
    IdentifiedTerm(0, expr0)
  }

  /**
    * exprA(0) ::= integer |
    * float |
    * variable |
    * atom |
    * atom( exprA(1200) { , exprA(1200) }* ) |
    * '[' exprA(1200) { , exprA(1200) }* [ | exprA(1200) ] ']' |
    * '{' [ exprA(1200) ] '}' |
    * '(' exprA(1200) ')'
    */
  @throws[InvalidTermException]
  @throws[IOException]
  private def expr0: Term = {
    val t1: Token = tokenizer.readToken
    val tempStart: scala.Int = tokenizer.tokenStart
    if (t1.isType(Tokenizer.INTEGER)) {
      val i: Term = Parser.parseInteger(t1.seq)
      map(i, tokenizer.tokenStart)
      return i //todo moved method to Number
    }
    if (t1.isType(Tokenizer.FLOAT)) {
      val f: Term = Parser.parseFloat(t1.seq)
      map(f, tokenizer.tokenStart)
      return f
    }
    if (t1.isType(Tokenizer.VARIABLE)) {
      val v: Term = new Var(t1.seq)
      map(v, tokenizer.tokenStart)
      return v //todo switched to use the internal check for "_" in Var(String)
    }
    /**/ if (t1.isType(Tokenizer.ATOM) || t1.isType(Tokenizer.SQ_SEQUENCE) || t1.isType(Tokenizer.DQ_SEQUENCE)) {
      if (!t1.isFunctor) /*Castagna 06/2011*/ {
        //return new Struct(t1.seq);
        val f: Term = new Struct(t1.seq)
        map(f, tokenizer.tokenStart)
        return f
      }
      /**/ val functor: String = t1.seq
      val t2: Token = tokenizer.readToken //reading left par
      if (!t2.isType(Tokenizer.LPAR)) throw new InvalidTermException("Something identified as functor misses its first left parenthesis") //todo check can be skipped
      val a: util.LinkedList[Term] = expr0_arglist //reading arguments
      val t3: Token = tokenizer.readToken
      if (t3.isType(Tokenizer.RPAR)) //reading right par
      /*Castagna 06/2011*/ {
        //return new Struct(functor, a);
        val c: Term = new Struct(functor, a)
        map(c, tempStart)
        return c
      }
      /**//*Castagna 06/2011*/
      //throw new InvalidTermException("Missing right parenthesis: ("+a + " -> here <-");
      throw new InvalidTermException("Missing right parenthesis '(" + a + "' -> here <-", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
      /**/
    }
    if (t1.isType(Tokenizer.LPAR)) {
      val term: Term = expr(false)
      if (tokenizer.readToken.isType(Tokenizer.RPAR)) return term
      /*Castagna 06/2011*/
      //throw new InvalidTermException("Missing right parenthesis: ("+term + " -> here <-");
      throw new InvalidTermException("Missing right parenthesis '(" + term + "' -> here <-", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
      /**/
    }
    if (t1.isType(Tokenizer.LBRA)) {
      val t2: Token = tokenizer.readToken
      if (t2.isType(Tokenizer.RBRA)) return new Struct
      tokenizer.unreadToken(t2)
      val term: Term = expr0_list
      if (tokenizer.readToken.isType(Tokenizer.RBRA)) return term
      /*Castagna 06/2011*/
      //throw new InvalidTermException("Missing right bracket: ["+term + " -> here <-");
      throw new InvalidTermException("Missing right bracket '[" + term + " ->' here <-", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
      /**/
    }
    if (t1.isType(Tokenizer.LBRA2)) {
      var t2: Token = tokenizer.readToken
      if (t2.isType(Tokenizer.RBRA2)) /*Castagna 06/2011*/ {
        //return new Struct("{}");
        val b: Term = new Struct("{}")
        map(b, tempStart)
        return b
      }
      /**/ tokenizer.unreadToken(t2)
      val arg: Term = expr(false)
      t2 = tokenizer.readToken
      if (t2.isType(Tokenizer.RBRA2)) /*Castagna 06/2011*/ {
        //return new Struct("{}", arg);
        val b: Term = new Struct("{}", arg)
        map(b, tempStart)
        return b
      }
      /*Castagna 06/2011*/
      //throw new InvalidTermException("Missing right braces: {"+arg + " -> here <-");
      throw new InvalidTermException("Missing right braces '{" + arg + "' -> here <-", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
      /**/
    }
    /*Castagna 06/2011*/
    //throw new InvalidTermException("The following token could not be identified: "+t1.seq);
    throw new InvalidTermException("Unexpected token '" + t1.seq + "'", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
    /**/
  }

  //todo make non-recursive?
  @throws[InvalidTermException]
  @throws[IOException]
  private def expr0_list: Term = {
    val head: Term = expr(true)
    val t: Token = tokenizer.readToken
    if ("," == t.seq) return new Struct(head, expr0_list)
    if ("|" == t.seq) return new Struct(head, expr(true))
    if ("]" == t.seq) {
      tokenizer.unreadToken(t)
      return new Struct(head, new Struct)
    }
    /*Castagna 06/2011*/
    //throw new InvalidTermException("The expression: " + head + " is not followed by either a ',' or '|'  or ']'.");
    throw new InvalidTermException("The expression '" + head + "' is not followed by either a ',' or '|'  or ']'.", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
    /**/
  }

  //todo make non-recursive
  @throws[InvalidTermException]
  @throws[IOException]
  private def expr0_arglist: util.LinkedList[Term] = {
    val head: Term = expr(true)
    val t: Token = tokenizer.readToken
    if ("," == t.seq) {
      val l: util.LinkedList[Term] = expr0_arglist
      l.addFirst(head)
      return l
    }
    if (")" == t.seq) {
      tokenizer.unreadToken(t)
      val l: util.LinkedList[Term] = new util.LinkedList[Term]
      l.add(head)
      return l
    }
    /*Castagna 06/2011*/
    //throw new InvalidTermException("The argument: " + head + " is not followed by either a ',' or ')'.\nline: " + tokenizer.lineno());
    /*Castagna 06/2011*/
    //throw new InvalidTermException("The argument: " + head + " is not followed by either a ',' or ')'.");
    throw new InvalidTermException("The argument '" + head + "' is not followed by either a ',' or ')'.", tokenizer.offsetToRowColumn(getCurrentOffset)(0), tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1)
    /**/
  }

  private def identifyTerm(priority: scala.Int, term: Term, offset: scala.Int): IdentifiedTerm = {
    map(term, offset)
    IdentifiedTerm(priority, term)
  }

  private def map(term: Term, offset: scala.Int) {
    if (offsetsMap != null) offsetsMap.put(term, offset)
  }
}