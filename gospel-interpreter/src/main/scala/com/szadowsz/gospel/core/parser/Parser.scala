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


import java.io._
import java.util
import java.util.regex.Pattern

import com.szadowsz.gospel.core.data
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.exception.InvalidTermException
import org.springframework.core.io.Resource

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.control.NonFatal

object Parser {

  private val atom: Pattern = Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)")

  /**
    * @return true if the String could be a prolog atom
    */
  def isAtom(s: String): Boolean = atom.matcher(s).matches

  private[core] def parseNumber(s: String): data.Number = Try(parseInteger(s)).getOrElse(parseFloat(s))

  private[core] def parseInteger(s: String): data.Int = data.Int(s.toLong)

  private[core] def parseFloat(s: String): data.Float = data.Float(s.toDouble)
}

class Parser(reader: BufferedReader)(implicit opManager: OperatorManager) extends Iterable[Term] {
  private val tokenizer: TermTokenizer = new TermTokenizer(reader)


  def this(in: InputStream)(implicit opManager: OperatorManager) {
    this(new BufferedReader(new InputStreamReader(in)))
  }

  def this(res: Resource)(implicit opManager: OperatorManager) {
    this(res.getInputStream)
  }

  def this(file: File)(implicit opManager: OperatorManager) {
    this(new FileInputStream(file))
  }

  def this(str: String)(implicit opManager: OperatorManager) {
    this(new BufferedReader(new StringReader(str)))
  }

  private def identifyTerm(priority: scala.Int, term: Term, offset: scala.Int): (scala.Int, Term) = {
    map(term, offset)
    (priority, term)
  }

  private def map(term: Term, offset: scala.Int) {
    //  if (offsetsMap != null) offsetsMap.put(term, offset)
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
    val t1: TermToken = tokenizer.readToken
    val tempStart: scala.Int = tokenizer.colNo

    t1.getType match {
      case TermTokenizer.INTEGER => Parser.parseInteger(t1.seq)

      case TermTokenizer.FLOAT => Parser.parseFloat(t1.seq)

      case TermTokenizer.VARIABLE => new Var(t1.seq)

      case s if s == TermTokenizer.ATOM || s == TermTokenizer.SQ_SEQUENCE || s == TermTokenizer.DQ_SEQUENCE =>
        parseToStruct(t1, tempStart)

      case TermTokenizer.LPAR => parseLPAR()

      case TermTokenizer.LBRA => parseLBRA()

      case TermTokenizer.LBRA2 => parseLBRA2()
      case _ =>
        throw new InvalidTermException(
          s"Unexpected Token '${t1.seq}'",
          t1.seq,
          lineNo,
          startColNo
        )
    }
  }

  def parseLBRA2():Term = {
    var t2: TermToken = tokenizer.readToken
    if (t2.isType(TermTokenizer.RBRA2)) {
      new Struct("{}")
    } else {
      /**/ tokenizer.unreadToken(t2)
      val arg: Term = expr(false)
      t2 = tokenizer.readToken
      if (t2.isType(TermTokenizer.RBRA2)) {
        new Struct("{}", arg)
      } else {
        throw new InvalidTermException(
          s"Missing right braces '{$arg' -> here <-",
          arg,
          lineNo,
          startColNo
        )
      }
    }
  }


  private def parseLBRA(): Term = {
    val t2: TermToken = tokenizer.readToken
    if (t2.isType(TermTokenizer.RBRA)) {
      new Struct
    } else {
      tokenizer.unreadToken(t2)
      val term: Term = expr0List
      if (tokenizer.readToken.isType(TermTokenizer.RBRA)) {
       term
      } else {
        throw new InvalidTermException(
          "Missing right bracket '[" + term + " ->' here <-" //        ,
          //        term,
          //        tokenizer.offsetToRowColumn(getCurrentOffset)(0),
          //        tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1
        )
      }
    }
  }

  private def parseLPAR(): Term = {
    val term: Term = expr(false)
    if (tokenizer.readToken.isType(TermTokenizer.RPAR)) {
      term
    } else {
      throw new InvalidTermException(
        "Missing right parenthesis '(" + term + "' -> here <-" //        ,
        //        term,
        //        tokenizer.offsetToRowColumn(getCurrentOffset)(0),
        //        tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1
      )
    }
  }

  private def parseToStruct(t1: TermToken, tempStart: Int): Term = {
    if (!t1.isFunctor) {
      val f: Term = new Struct(t1.seq)
      map(f, tokenizer.colNo)
      f
    } else {
      val functor: String = t1.seq
      val t2: TermToken = tokenizer.readToken //reading left par
      if (!t2.isType(TermTokenizer.LPAR)) {
        throw new InvalidTermException(
          "Something identified as functor misses its first left parenthesis" //,
          //          functor,
          //          tokenizer.offsetToRowColumn(getCurrentOffset)(0),
          //          tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1
        )
      } //todo check can be skipped
      val a: List[Term] = expr0ArgList() //reading arguments
      val t3: TermToken = tokenizer.readToken
      if (t3.isType(TermTokenizer.RPAR)) {
        //reading right par
        val c: Term = new Struct(functor, a)
        map(c, tempStart)
        c
      } else {
        throw new InvalidTermException(s"Missing right parenthesis '($a' -> here <-" //,
          //        functor,
          //        tokenizer.offsetToRowColumn(getCurrentOffset)(0),
          //        tokenizer.offsetToRowColumn(getCurrentOffset)(1) - 1
        )
      }
    }
  }

  //todo make non-recursive?
  @throws[InvalidTermException]
  @throws[IOException]
  private def expr0List: Term = {
    val head: Term = expr(true)
    tokenizer.readToken match {
      case TermToken(",", _) => new Struct(head, expr0List)
      case TermToken("|", _) => new Struct(head, expr(true))
      case t: TermToken if t.seq == "]" =>
        tokenizer.unreadToken(t)
        new Struct(head, new Struct)
      case _ =>
        throw new InvalidTermException(
          s"The expression '$head' is not followed by either a ',' or '|'  or ']'.",
          head,
          lineNo,
          colNo
        )
    }
  }

  //todo make non-recursive
  @throws[InvalidTermException]
  @throws[IOException]
  private def expr0ArgList(): List[Term] = {
    val head: Term = expr(true)
    tokenizer.readToken match {
      case TermToken(",", _) => head +: expr0ArgList()
      case t: TermToken if t.seq == ")" =>
        tokenizer.unreadToken (t)
        List(head)
      case _ =>
        throw new InvalidTermException (
          s"The argument '$head ' is not followed by either a ',' or ')'.",
          head,
          lineNo,
          colNo
        )
    }
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
  private def parseLeftSide(commaIsEndMarker: Boolean, maxPriority: scala.Int): (scala.Int, Term) = {
    //1. prefix expression
    val f: TermToken = tokenizer.readToken
    if (f.isOperator(commaIsEndMarker)) {
      var FX: scala.Int = opManager.opPrio(f.seq, "fx")
      var FY: scala.Int = opManager.opPrio(f.seq, "fy")
      if (f.seq == "-") {
        val t: TermToken = tokenizer.readToken
        if (t.isNumber) {
          return identifyTerm(0, Parser.parseNumber("-" + t.seq), tokenizer.colNo)
        } else {
          tokenizer.unreadToken(t)
        }
      }
      //check that no operator has a priority higher than permitted
      if (FY > maxPriority) FY = -1
      if (FX > maxPriority) FX = -1
      //FX has priority over FY
      var haveAttemptedFX: Boolean = false
      if (FX >= FY && FX >= OperatorManager.OP_LOW) {
        val found = exprA(FX - 1, commaIsEndMarker) //op(fx, n) exprA(n - 1)
        if (found != null)
          return identifyTerm(FX, new Struct(f.seq, found._2), tokenizer.colNo)
      }
      //FY has priority over FX, or FX has failed
      if (FY >= OperatorManager.OP_LOW) {
        val found = exprA(FY, commaIsEndMarker) //op(fy,n) exprA(1200)  or   op(fy,n) exprA(n)
        if (found != null)
          return identifyTerm(FY, new Struct(f.seq, found._2), tokenizer.colNo)
      }
      //FY has priority over FX, but FY failed
      if (!haveAttemptedFX && FX >= OperatorManager.OP_LOW) {
        val found = exprA(FX - 1, commaIsEndMarker) //op(fx, n) exprA(n - 1)
        if (found != null)
          return identifyTerm(FX, new Struct(f.seq, found._2), tokenizer.colNo)
      }
    }
    tokenizer.unreadToken(f)

    //2. expr0
    (0, expr0)
  }

  private def exprB(maxPriority: scala.Int, commaIsEndMarker: Boolean): (scala.Int, Term) = {
    //1. op(fx,n) exprA(n-1) | op(fy,n) exprA(n) | expr0
    var left = parseLeftSide(commaIsEndMarker, maxPriority)

    //2.left is followed by either xfx, xfy or xf operators, parse these
    var operator: TermToken = tokenizer.readToken
    var cont = true
    while (cont & operator.isOperator(commaIsEndMarker)) {
      cont = false

      //check that no operator has a priority higher than permitted
      //or a lower priority than the left side expression
      val XFX: scala.Int = rangeSanitation(opManager.opPrio(operator.seq, "xfx"), OperatorManager.OP_LOW, maxPriority)
      val XFY: scala.Int = rangeSanitation(opManager.opPrio(operator.seq, "xfy"), OperatorManager.OP_LOW, maxPriority)
      val XF: scala.Int = rangeSanitation(opManager.opPrio(operator.seq, "xf"), OperatorManager.OP_LOW, maxPriority)
      //XFX
      var haveAttemptedXFX: Boolean = false
      if (XFX >= XFY && XFX >= XF && XFX >= left._1) {
        //XFX has priority
        val found = exprA(XFX - 1, commaIsEndMarker)
        if (found != null) {
          left = identifyTerm(XFX, new Struct(operator.seq, left._2, found._2), tokenizer.colNo)
          cont = true
        }
        else haveAttemptedXFX = true
      }
      //XFY
      if (!cont && XFY >= XF && XFY >= left._1) {
        //XFY has priority, or XFX has failed
        val found = exprA(XFY, commaIsEndMarker)
        if (found != null) {
          left = identifyTerm(XFY, new Struct(operator.seq, left._2, found._2), tokenizer.colNo)
          cont = true //todo: continue is not supported
        }
      }
      //XF
      if (!cont && XF >= left._1) //XF has priority, or XFX and/or XFY has failed
        return identifyTerm(XF, new Struct(operator.seq, left._2), tokenizer.colNo)
      //XFX did not have top priority, but XFY failed
      if (!cont && !haveAttemptedXFX && XFX >= left._1) {
        val found = exprA(XFX - 1, commaIsEndMarker)
        if (found != null) {
          left = identifyTerm(XFX, new Struct(operator.seq, left._2, found._2), tokenizer.colNo)
          cont = true
        }
      }
      if (cont) {
        operator = tokenizer.readToken
      }
    }
    tokenizer.unreadToken(operator)
    left
  }

  /**
    * Private Method to sanitise the priority if it is outside the expected range
    *
    * @param prio the operator priority
    * @param low  the lower bound
    * @param high the upper bound
    * @return -1 if prio is outside low <= prio <= high or prio otherwise
    */
  private def rangeSanitation(prio: Int, low: Int, high: Int): Int = {
    if (prio < low || prio > high) -1 else prio
  }

  private def exprA(maxPriority: scala.Int, commaIsEndMarker: Boolean): (scala.Int, Term) = {
    var leftSide = exprB(maxPriority, commaIsEndMarker)
    var t: TermToken = tokenizer.readToken
    var cont = true
    while (cont && t.isOperator(commaIsEndMarker)) {
      cont = false
      val YFX: scala.Int = rangeSanitation(opManager.opPrio(t.seq, "yfx"), leftSide._1, maxPriority)
      val YF: scala.Int = rangeSanitation(opManager.opPrio(t.seq, "yf"), leftSide._1, maxPriority)
      //YFX has priority over YF
      if (YFX >= YF && YFX >= OperatorManager.OP_LOW) {
        val ta = exprA(YFX - 1, commaIsEndMarker)
        if (ta != null) {
          leftSide = identifyTerm(YFX, new Struct(t.seq, leftSide._2, ta._2), tokenizer.colNo)
          cont = true
        }
      }
      //either YF has priority over YFX or YFX failed
      if (!cont && YF >= OperatorManager.OP_LOW) {
        leftSide = identifyTerm(YF, new Struct(t.seq, leftSide._2), tokenizer.colNo)
        cont = true
      }
      if (cont) {
        t = tokenizer.readToken
      }
    }
    tokenizer.unreadToken(t)
    leftSide
  }


  /**
    * Parses and evaluates the term expression based on the priority of operators specified in the operator manager.
    *
    * @param commaIsEndMarker whether a comma ends the expression
    * @return the expression as a Term object.
    */
  private def expr(commaIsEndMarker: Boolean): Term = exprA(OperatorManager.OP_HIGH, commaIsEndMarker)._2

  /**
    * Parses next term from the stream.
    *
    * @param endNeeded true if it needs to parse the end TermToken (a period), false otherwise.
    * @return the nex term
    */
  @throws[InvalidTermException]
  def nextTerm(endNeeded: Boolean): Term = {
    try {
      val t: TermToken = tokenizer.readToken
      if (t.isEOF) {
        return null
      }
      tokenizer.unreadToken(t)
      val term: Term = expr(false)
      if (term == null) {
        throw new InvalidTermException("The parser is unable to finish.", lineNo, colNo)
      }
      if (endNeeded && tokenizer.readToken.getType != TermTokenizer.END) {
        throw new InvalidTermException(s"The term '$term' is not ended with a period.", term, lineNo, colNo)
      }
      term.resolveVars()
      term
    } catch {
      case ex: IOException =>
        throw new InvalidTermException("An I/O error occured.", ex, lineNo, colNo)
      case npe: NullPointerException =>
        throw new InvalidTermException("Unable to Parse Term.", npe, lineNo, colNo)
    }
  }

  /** Creates a new iterator over all Terms that can be identified by this parser.
    *
    * @return the new iterator
    */
  override def iterator: Iterator[Term] = new Iterator[Term] {

    private val parser : Parser = Parser.this
    private var nextOpt : Option[Term] = None

    override def hasNext: Boolean = {
      nextOpt = nextOpt.orElse(Option(parser.nextTerm(true)))
      nextOpt.nonEmpty
    }

    override def next(): Term = {
      val result = nextOpt.orElse(Option(parser.nextTerm(true))).get
      nextOpt = None
      result
    }
  }



  /**
    * The current line number being read by the Parser, 1-Aligned
    *
    * @return
    */
  def lineNo: Int = tokenizer.lineNo

  private def startColNo = {
    tokenizer.tokenStartColNo
  }

  /**
    * The last column read by the Parser, 1-aligned
    *
    * @return
    */
  def colNo: Int = tokenizer.colNo
}
