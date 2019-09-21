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
import java.util.regex.Pattern

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.parser.PrologParser.{OptClauseContext, SingletonExpressionContext, SingletonTermContext}
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime._
import org.springframework.core.io.Resource

import scala.util.{Failure, Try}
object NParser {
  
  private val atom: Pattern = Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)")
  
  /**
    * @return true if the String could be a prolog atom
    */
  def isAtom(s: String): Boolean = atom.matcher(s).matches
}

class NParser {
  
  private def parseClause(parser: PrologParser, source: Any): OptClauseContext = {
    var mark = -1
    var index = -1
    try {
      mark = parser.getTokenStream.mark()
      index = parser.getTokenStream.index()
      parser.optClause()
    } catch {
      case _: ParseCancellationException if parser.getInterpreter.getPredictionMode == PredictionMode.SLL =>
        parser.getTokenStream.seek(index)
        parser.getInterpreter.setPredictionMode(PredictionMode.LL)
        parser.setErrorHandler(new DefaultErrorStrategy())
        parser.addErrorListener(new ErrorListener(source));
        parser.optClause()
      case pce: ParseCancellationException if pce.getCause.isInstanceOf[RecognitionException] => throw pce.getCause
     
      case p : ParseException => throw p.toInvalidTermException
  
      case default => throw default
        
    } finally {
      parser.getTokenStream.release(mark)
    }
  }
  
  private def parseClause(index: Int, parser: PrologParser, source: Any): OptClauseContext = {
    Try(parseClause(parser, source))
      .transform(c => Try(c), t => Failure(t match {
        case p: ParseException =>
          p.setClauseIndex(index)
          p
        case e: Throwable => e
      }))
    }.get
  
  private def parseClauses(parser: PrologParser, source: Any): Seq[OptClauseContext] = {
    def loop(v: Int): Stream[(Int, OptClauseContext)] = (v, parseClause(v, parser, source)) #:: loop(v + 1)
    
    loop(0).takeWhile(!_._2.isOver).map(_._2)
  }
  
  private def addOperators(prologParser: PrologParser, opManager: OperatorManager) = {
    for (op <- opManager.getOperators()) {
      prologParser.addOperator(op._1, op._2, op._3)
    }
  //  prologParser.addParseListener(DynamicOpListener.of(prologParser, operators.add))
    prologParser
  }
  
  private def initParser(source: BufferedReader) = {
    val stream = CharStreams.fromReader(source)
    val lexer = new PrologLexer(stream)
    lexer.removeErrorListeners()
    val tokenStream = new BufferedTokenStream(lexer)
    val parser = new PrologParser(tokenStream)
    parser.removeErrorListeners()
    parser.setErrorHandler(new BailErrorStrategy)
    parser.getInterpreter.setPredictionMode(PredictionMode.SLL)
    parser
  }
  
  def parseTerms(reader: BufferedReader)(implicit opManager: OperatorManager): Seq[Term] =  {
    val parser = addOperators(initParser(reader),opManager)
    parseClauses(parser, reader).map(it => it.accept(new NVisitor))
  }
  
  
  def parseTerms(in: InputStream)(implicit opManager: OperatorManager): Seq[Term] = {
    parseTerms(new BufferedReader(new InputStreamReader(in)))
  }
  
  def parseTerms(res: Resource)(implicit opManager: OperatorManager): Seq[Term] = {
    parseTerms(res.getInputStream)
  }
  
  def parseTerms(file: File)(implicit opManager: OperatorManager): Seq[Term] = {
    parseTerms(new FileInputStream(file))
  }
  
  def parseTerms(str: String)(implicit opManager: OperatorManager): Seq[Term] = {
    parseTerms(new BufferedReader(new StringReader(str)))
  }
  
  private def parseTerm(parser: PrologParser, source: Any) : SingletonExpressionContext = {
    try {
      parser.singletonExpression
    } catch {
      case _: ParseCancellationException if parser.getInterpreter.getPredictionMode == PredictionMode.SLL =>
        parser.getTokenStream.seek(0)
        parser.getInterpreter.setPredictionMode(PredictionMode.LL)
        parser.setErrorHandler(new DefaultErrorStrategy())
        parser.addErrorListener(new ErrorListener(source));
        parseTerm(parser, source)
      case pce: ParseCancellationException if pce.getCause.isInstanceOf[RecognitionException] => throw pce.getCause
     
      case p : ParseException => throw p.toInvalidTermException
     
      case default => throw default
    }
  }
  
  def parseTerm(reader: BufferedReader)(implicit opManager: OperatorManager): Term = {
    val parser = addOperators(initParser(reader),opManager)
    parseTerm(parser, reader).accept(new NVisitor())
  }
  
  def parseTerm(str: String)(implicit opManager: OperatorManager): Term = {
    parseTerm(new BufferedReader(new StringReader(str)))
  }
}
