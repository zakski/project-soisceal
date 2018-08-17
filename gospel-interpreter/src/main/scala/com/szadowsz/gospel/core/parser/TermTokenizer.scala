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

import java.io.{BufferedReader, StreamTokenizer}
import java.math.BigInteger
import java.util

import com.szadowsz.gospel.core.exception.InvalidTermException
import org.slf4j.{Logger, LoggerFactory}

private[parser] object TermTokenizer {
  val TYPEMASK: Int = 0x00FF
  val ATTRMASK: Int = 0xFF00
  val LPAR: Int = 0x0001
  val RPAR: Int = 0x0002
  val LBRA: Int = 0x0003
  val RBRA: Int = 0x0004
  val BAR: Int = 0x0005
  val INTEGER: Int = 0x0006
  val FLOAT: Int = 0x0007
  val ATOM: Int = 0x0008
  val VARIABLE: Int = 0x0009
  val SQ_SEQUENCE: Int = 0x000A
  val DQ_SEQUENCE: Int = 0x000B
  val END: Int = 0x000D
  val LBRA2: Int = 0x000E
  val RBRA2: Int = 0x000F
  val FUNCTOR: Int = 0x0100
  val OPERATOR: Int = 0x0200
  val EOF: Int = 0x1000
  val GRAPHIC_CHARS: Array[Char] = Array('\\', '$', '&', '?', '^', '@', '#', '.', ',', ':', ';', '=', '<', '>', '+', '-', '*', '/', '~').sorted

  val WHITESPACE_CHARS: Array[Char] = Array('\r', '\n')

  /**
    *
    *
    * @param typec
    * @param svalc
    * @return the intValue of the next character token, -1 if invalid
    *         todo needs a lookahead if typec is \
    */
  private def isCharacterCodeConstantToken(typec: Int, svalc: String): Int = {
    if (svalc != null) {
      if (svalc.length == 1) {
        return svalc.charAt(0).toInt
      } else if (svalc.length > 1) {
        return -1
      }
    }
    if (typec == ' ' || GRAPHIC_CHARS.contains(typec)) {
      typec
    } else {
      -1
    }
  }

  /**
    * used to implement lookahead for two tokens, super.pushBack() only handles one pushBack..
    */
  private case class PushBack(typeA: Int, svalA: String)

}

private[parser] final class TermTokenizer(reader: BufferedReader) {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val tokenStream: StreamTokenizer = buildTokenizer(reader) // TODO replace with something that can handle unicode

  private var tokenQueue: List[TermToken] = List()

  private var positionQueue: List[(Int,Int,Int)] = List()

  private var tokenLine : Int = 1

  private var tokenStart: Int = 1

  private var tokenEnd: Int = 1

  private var tokenLength: Int = 0

  private var pushBack2: TermTokenizer.PushBack = _

  private def sval = tokenStream.sval

  private def buildTokenizer(reader: BufferedReader): StreamTokenizer = {
    val st = new StreamTokenizer(reader)
    st.resetSyntax()
    st.wordChars('a', 'z')
    st.wordChars('A', 'Z')
    st.wordChars('_', '_')
    st.wordChars('0', '9')
    TermTokenizer.WHITESPACE_CHARS.foreach(c => st.whitespaceChars(c, c))
    st.commentChar('%')
    st.slashStarComments(true)
    st.eolIsSignificant(true)
    st
  }

  /**
    * Gets the underlying token from the stream, and increase the Offset
    *
    * @return the stream token
    */
  private def tokenConsume(): Int = {
    positionQueue = (tokenLine,tokenStart,tokenEnd) +: positionQueue
    val token: Int = tokenStream.nextToken
    tokenLine = tokenStream.lineno()
    tokenStart = tokenEnd
    tokenLength = if (token == StreamTokenizer.TT_EOF || token == StreamTokenizer.TT_EOL) 0 else if (sval == null) 1 else sval.length
    if (token == StreamTokenizer.TT_EOL) {
      tokenEnd = 1
    } else {
      tokenEnd += tokenLength
    }
    token
  }

  /**
    * Push back the last token from the stream, and decreases the Offset
    */
  private def tokenPushBack(): Unit = {
    tokenStream.pushBack()
    val (l, s, e) = positionQueue.head
    positionQueue = positionQueue.tail
    tokenLine = l
    tokenStart = s
    tokenEnd = e
    tokenLength = e - s
  }

  /**
    *
    * @param qType
    * @param quote
    * @throws InvalidTermException
    * @return
    */
  @throws[InvalidTermException]
  private def findEndOfQuote(qType: Int, quote: StringBuffer): (Boolean, Int, String) = {
    val typeA = tokenConsume()
    val svalA = sval
    if (typeA == '\\') {
      val typeB: Int = tokenConsume()
      if (typeB == '\n') return (true, typeA, svalA) //todo: continue is not supported
      if (typeB == '\r') {
        val typec: Int = tokenConsume()
        if (typec == '\n') return (true, typeA, svalA) //todo: continue is not supported
        tokenPushBack()
        return (true, typeA, svalA) //todo: continue is not supported
      }
      tokenPushBack()
    }
    if (typeA == qType) {
      return foundQuoteContents(qType, quote, typeA, svalA)
    }
    if (typeA == '\n' || typeA == '\r') {
      throw new InvalidTermException("Line break in quote not allowed", quote.toString + svalA)
    }
    if (svalA != null) {
      quote.append(svalA)
    } else {
      if (typeA < 0) {
        throw new InvalidTermException("Invalid string", quote.toString)
      }
      quote.append(typeA.toChar)
    }
    (true, typeA, svalA)
  }

  private def foundQuoteContents(qType: Int, quote: StringBuffer, typeA: Int, svalA: String) = {
    val typeB: Int = tokenConsume()
    if (typeB == qType) {
      quote.append(qType.toChar)
      (true, typeA, svalA) //todo: continue is not supported
    } else {
      tokenPushBack()
      (false, typeA, svalA) //todo: break is not supported
    }
  }

  /**
    * Method to classify the context of a quotation identified as some sort of prolog term
    *
    * @param typeA the int value of the quote character read from the stream
    * @throws InvalidTermException if it is unable to tokenise a potential quoted term
    * @return the next prolog term token
    */
  @throws[InvalidTermException]
  private def classifyQuoteContents(typeA: Int): TermToken = {
    var qType: Int = typeA
    val quote: StringBuffer = new StringBuffer
    var continue = true
    while (continue) {
      val temp = findEndOfQuote(qType, quote)
      continue = temp._1
    }
    val quoteBody: String = quote.toString
    qType = if (qType == '\'') TermTokenizer.SQ_SEQUENCE else if (qType == '\"') TermTokenizer.DQ_SEQUENCE else TermTokenizer.SQ_SEQUENCE
    if (qType == TermTokenizer.SQ_SEQUENCE) {
      if (Parser.isAtom(quoteBody)) qType = TermTokenizer.ATOM
      val typeB: Int = tokenConsume()
      tokenPushBack()
      if (typeB == '(') return TermToken(quoteBody, qType | TermTokenizer.FUNCTOR)
    }
    TermToken(quoteBody, qType)
  }


  /**
    * Builds a non-decimal based integer
    *
    * @param svalA string representing the non base-10 integer
    * @throws InvalidTermException if it is unable to tokenise a potential number term
    * @return base-10 Integer token
    */
  @throws[InvalidTermException]
  private def buildNondecimalNumberToken(svalA: String): TermToken = {
    svalA.charAt(1) match {
      case 'b' => TermToken(new BigInteger(svalA.substring(2), 2).toString, TermTokenizer.INTEGER) // binary
      case 'o' => TermToken(new BigInteger(svalA.substring(2), 8).toString, TermTokenizer.INTEGER) // octal
      case 'x' => TermToken(new BigInteger(svalA.substring(2), 16).toString, TermTokenizer.INTEGER) // hexadecimal
      case unknown => throw new InvalidTermException(s"Unknown number base $unknown", svalA, lineNo, colNo)
    }
  }

  @throws[InvalidTermException]
  private def readCharConstantToken(): TermToken = {
    val typec: Int = tokenConsume()
    val svalc: String = sval
    val intVal: Int = TermTokenizer.isCharacterCodeConstantToken(typec, svalc)
    if (intVal != -1) {
      TermToken("" + intVal, TermTokenizer.INTEGER)
    } else {
      val (l,s,_) = positionQueue.tail.head
      throw new InvalidTermException("Character code constant starting with 0'<X> cannot be recognized.",
        svalc,
        l,
        s
      )
    }
  }

  /**
    * Private Method to build a float token
    *
    * @param svalA the collected string value of the number token
    * @param typeB the int value of the current character read from the stream
    * @return the next operator term
    */
  private def buildFloatToken(svalA: String, typeB: Int, svalB: String): TermToken = {
    val typeC: Int = tokenConsume()
    val svalC: String = sval
    if (typeC != StreamTokenizer.TT_WORD) {
      tokenPushBack()
      pushBack2 = TermTokenizer.PushBack(typeB, svalB)
      TermToken(svalA, TermTokenizer.INTEGER)
    } else {
      var exponent: Int = svalC.indexOf("E")
      if (exponent == -1) exponent = svalC.indexOf("e")
      if (exponent >= 1 && exponent == svalC.length - 1) {
        val typeB2: Int = tokenConsume()
        if (typeB2 == '+' || typeB2 == '-') {
          val typec2: Int = tokenConsume()
          val svalc2: String = sval
          if (typec2 == StreamTokenizer.TT_WORD) {
            java.lang.Long.parseLong(svalC.substring(0, exponent))
            svalc2.toInt
            return TermToken(svalA + "." + svalC + typeB2.toChar + svalc2, TermTokenizer.FLOAT)
          }
        }
      }
      svalA + "." + svalC.toDouble
      TermToken(svalA + "." + svalC, TermTokenizer.FLOAT)
    }
  }

  /**
    * Private Method to classify the next token as a float or integer.
    *
    * @note This is quite a complex part of the tokeniser as we need to handle Long.MIN_VALUE that comes in as
    *       Long.MAX_VALUE+1 due to the unary minus operator being regarded as a separate token.
    *
    * @param svalA the collected string value of the number token
    * @throws InvalidTermException if it is unable to tokenise a potential number term
    * @return a Token Representing a Number.
    */
  @throws[InvalidTermException]
  private def classifyNumberToken(svalA: String): TermToken = {
    try {
      if (svalA.matches("0[bxo][A-F\\d]*")) {
        buildNondecimalNumberToken(svalA)
      } else {
        val typeB: Int = tokenConsume()
        val svalB: String = sval
        if (typeB != '.' && typeB != '\'') {
          tokenPushBack()
          TermToken(new BigInteger(svalA).toString, TermTokenizer.INTEGER) // we use big decimal here to avoid the long situation about as mentioned.
        } else if (typeB == '\'' && ("0" == svalA)) {
          readCharConstantToken()
        } else if (typeB != '.') {
          throw new InvalidTermException("A number starting with 0-9 cannot be recognized as an int and does not have a fraction '.'", svalA, lineNo, tokenStartColNo)
        } else {
          buildFloatToken(svalA, typeB, svalB)
        }
      }
    } catch {
      case e: NumberFormatException =>
        throw new InvalidTermException("A term starting with 0-9 cannot be parsed as a number", svalA, lineNo, tokenStartColNo)
    }
  }

  /**
    * Private Method to classify a token identified as some sort of prolog term
    *
    * @param svalA the collected string value of the word/number token
    * @throws InvalidTermException if it is unable to tokenise a potential term
    * @return the next prolog term token
    */
  @throws[InvalidTermException]
  private def classifyWordToken(svalA : String) : TermToken = {
    val firstChar: Char = svalA.charAt(0)
    if (Character.isUpperCase(firstChar) || '_' == firstChar) {
       TermToken(svalA, TermTokenizer.VARIABLE)
    } else if (firstChar >= '0' && firstChar <= '9') {
       classifyNumberToken(svalA)
    } else {
      val typeB: Int = tokenConsume()
      tokenPushBack()
      if (typeB == '(') {
         TermToken(svalA, TermTokenizer.ATOM | TermTokenizer.FUNCTOR)
      } else if (WhitespaceClassifier(typeB)) {
         TermToken(svalA, TermTokenizer.ATOM | TermTokenizer.OPERATOR)
      } else {
         TermToken(svalA, TermTokenizer.ATOM)
      }
    }
  }

  /**
    * Method to build an operator token made up of one or more graphic characters
    *
    * @param typeA the int value of the current character read from the stream
    * @return the next operator term
    */
  private def buildOperatorToken(typeA : Int) : TermToken ={
    val symbols: StringBuffer = new StringBuffer
    var typeB: Int = typeA
    while (util.Arrays.binarySearch(TermTokenizer.GRAPHIC_CHARS, typeB.toChar) >= 0) {
      symbols.append(typeB.toChar)
      typeB = tokenConsume()
    }
    tokenPushBack()
    TermToken(symbols.toString, TermTokenizer.OPERATOR)
  }

  /**
    * Private Method to classify a general token
    *
    * @param typeA the int value of the current character read from the stream
    * @param svalA the collect string value if a word/number token
    * @throws InvalidTermException if it is unable to tokenise a potential term
    * @return the next general classified term
    */
  @throws[InvalidTermException]
  private def classifyGeneralToken(typeA: Int, svalA: String) :TermToken = {
    if (typeA == '.') {
      val typeB: Int = tokenConsume()
      if (WhitespaceClassifier(typeB) || typeB == StreamTokenizer.TT_EOL || typeB == StreamTokenizer.TT_EOF) {
        logger.debug("Term End Detected")
        return TermToken(".", TermTokenizer.END)
      } else {
        tokenPushBack()
      }
    }
    if (typeA == StreamTokenizer.TT_WORD) {
      classifyWordToken(svalA)
    } else if (typeA == '\'' || typeA == '\"' || typeA == '`') {
      classifyQuoteContents(typeA)
    } else if (util.Arrays.binarySearch(TermTokenizer.GRAPHIC_CHARS, typeA.toChar) >= 0) {
      buildOperatorToken(typeA)
    } else {
      throw new InvalidTermException(s"Unknown Unicode character: $typeA  ($svalA)", typeA + svalA)
    }
  }

  /**
    * Private Method reads the next character from the stream, or from our second push back storage
    *
    * @throws InvalidTermException if it is unable to tokenise a potential term
    * @return the next raw character value and built up string
    */
  @throws[InvalidTermException]
  private def readTokenFromStream(): (Int, String) = {
    if (pushBack2 != null) {
      val r = (pushBack2.typeA, pushBack2.svalA)
      pushBack2 = null
      r
    } else {
      (tokenConsume(), sval)
    }
  }

  /**
    * Private Method gets the next token from the input stream and classifies it
    *
    * @throws InvalidTermException if it is unable to tokenise a potential term
    * @return the next classified term from the stream
    */
  @throws[InvalidTermException]
  private def readNextToken(): TermToken = {
    var (typeA, svalA) = readTokenFromStream()
    typeA match {
      case StreamTokenizer.TT_EOF =>
        logger.debug("End of File Detected")
        TermToken("", TermTokenizer.EOF)

      case StreamTokenizer.TT_EOL =>
        logger.debug("End of Line Detected")
        readNextToken()

      case ControlClassifier(sig) => ControlClassifier.classify(sig)

      case WhitespaceClassifier(_) => readNextToken()

      case _ => classifyGeneralToken(typeA, svalA)
    }
  }

  /**
    * Method returns the next available token
    *
    * @throws InvalidTermException if it is unable to tokenise a potential term
    * @return a token to be read or null if it has reached the end of stream.
    */
  @throws[InvalidTermException]
  def readToken: TermToken = {
    if (tokenQueue.nonEmpty) {
      val dequeued = tokenQueue.head
      tokenQueue = tokenQueue.tail
      logger.debug("Re-reading token {}", dequeued)
      dequeued
    } else {
      val next = readNextToken()
      logger.debug("Reading token {}", next)
      next
    }
  }

  /**
    * unreads a token so it can be read again.
    *
    * @param token token to read again
    */
  def unreadToken(token: TermToken): Unit = {
    logger.debug("Unreading token {}", token)
    tokenQueue = token +: tokenQueue
  }

  /**
    * Gets the current line being read by the tokeniser
    *
    * @return the current line.
    */
  def lineNo: Int = tokenLine

  /**
    * Gets the current column being read by the tokeniser
    *
    * @return the current col.
    */
  def colNo: Int = tokenEnd

  /**
    * Gets the start column of the mostly recently read token
    *
    * @return the current col.
    */
  def tokenStartColNo : Int = tokenEnd - tokenLength
}
