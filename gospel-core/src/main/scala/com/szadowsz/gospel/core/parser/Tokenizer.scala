/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  *
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
import java.math.BigInteger
import java.util.{Arrays, LinkedList}

import com.szadowsz.gospel.core
import com.szadowsz.gospel.core.error.InvalidTermException
import com.szadowsz.gospel.core.parser


@SerialVersionUID(1L)
private[parser] object Tokenizer {
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
  val GRAPHIC_CHARS: Array[Char] = Array('\\', '$', '&', '?', '^', '@', '#', '.', ',', ':', ';', '=', '<', '>', '+', '-', '*', '/', '~')

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
      if (svalc.length == 1) return svalc.charAt(0).toInt
      if (svalc.length > 1) {
        return -1
      }
    }
    if (typec == ' ' || Arrays.binarySearch(GRAPHIC_CHARS, typec.toChar) >= 0) return typec
    -1
  }

  private def isWhite(`type`: Int): Boolean = {
    `type` == ' ' || `type` == '\r' || `type` == '\n' || `type` == '\t' || `type` == '\f'
  }

  /**
    * used to implement lookahead for two tokens, super.pushBack() only handles one pushBack..
    */
  private class PushBack {
    private[parser] var typea: Int = 0
    private[parser] var svala: String = _

    def this(i: Int, s: String) {
      this()
      typea = i
      svala = s
    }
  }

  try {
    Arrays.sort(Tokenizer.GRAPHIC_CHARS)
  }
}

/**
  * creating a tokenizer for the source stream
  *
  * Created on 19/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
private[parser] class Tokenizer(theReader: Reader) extends StreamTokenizer(theReader) with Serializable {
  private val tokenList: LinkedList[Token] = new LinkedList[Token]
  private var _tokenOffset: Int = 0
  private var _tokenStart: Int = 0
  private var tokenLength: Int = 0
  private var text: String = _
  private var pushBack2: Tokenizer.PushBack = _

  {
    resetSyntax
    wordChars('a', 'z')
    wordChars('A', 'Z')
    wordChars('_', '_')
    wordChars('0', '9')
    ordinaryChar('!')
    ordinaryChar('\\')
    ordinaryChar('$')
    ordinaryChar('&')
    ordinaryChar('^')
    ordinaryChar('@')
    ordinaryChar('#')
    ordinaryChar(',')
    ordinaryChar('.')
    ordinaryChar(':')
    ordinaryChar(';')
    ordinaryChar('=')
    ordinaryChar('<')
    ordinaryChar('>')
    ordinaryChar('+')
    ordinaryChar('-')
    ordinaryChar('*')
    ordinaryChar('/')
    ordinaryChar('~')
    ordinaryChar('\'')
    ordinaryChar('\"')
    ordinaryChar('%')
  }

  def this(theText: String) {
    this(new StringReader(theText))
    text = theText
    _tokenOffset = -1
  }


  /**
    * reads next available token
    */
  @throws(classOf[InvalidTermException])
  @throws(classOf[IOException])
  def readToken: Token = {
    if (!tokenList.isEmpty) tokenList.removeFirst else readNextToken
  }

  override def lineno: Int = {
    offsetToRowColumn(_tokenOffset)(0)
  }

  def offsetToRowColumn(offset: Int): Array[Int] = {
    if (text == null || text.length <= 0) return Array[Int](super.lineno, -1)
    val newText: String = removeTrailing(text, _tokenOffset)
    var lno: Int = 0
    var lastNewline: Int = -1

    var i: Int = 0
    while (i < newText.length && i < offset) {
      if (newText.charAt(i) == '\n') {
        lno += 1
        lastNewline = i
      }
      i += 1
    }

    Array[Int](lno + 1, offset - lastNewline)
  }

  /**
    * Marco Prati
    * 19/04/11
    *
    * remove Trailing spaces from last token, where
    * tokenizer stopped itself to correct the offset
    *
    */
  private[parser] def removeTrailing(input: String, tokenOffset: Int): String = {
    var i: Int = tokenOffset
    var out: String = input
    try {
      var c: Char = input.charAt(tokenOffset - 1)
      while (c == '\n') {
        out = input.substring(0, i)
        i -= 1
        c = input.charAt(i)
      }
      out = out.concat(input.substring(tokenOffset))
      out
    }
    catch {
      case e: Exception => {
        input
      }
    }
  }

  def tokenOffset(): Int = _tokenOffset

  def tokenStart(): Int = _tokenStart

  /**
    * puts back token to be read again
    */
  private[parser] def unreadToken(token: Token) {
    tokenList.addFirst(token)
  }

  private def evaluateTokens(qType: Int, quote: StringBuffer): (Boolean, Int, String) = {
    val typea = tokenConsume
    val svala = sval
    if (typea == '\\') {
      val typeb: Int = tokenConsume
      if (typeb == '\n') return (true, typea, svala) //todo: continue is not supported
      if (typeb == '\r') {
        val typec: Int = tokenConsume
        if (typec == '\n') return (true, typea, svala) //todo: continue is not supported
        tokenPushBack
        return (true, typea, svala) //todo: continue is not supported
      }
      tokenPushBack
    }
    if (typea == qType) {
      val typeb: Int = tokenConsume
      if (typeb == qType) {
        quote.append(qType.toChar)
        return (true, typea, svala) //todo: continue is not supported
      }
      else {
        tokenPushBack
        return (false, typea, svala) //todo: break is not supported
      }
    }
    if (typea == '\n' || typea == '\r') throw new InvalidTermException("Line break in quote not allowed")
    if (svala != null) quote.append(svala)
    else {
      if (typea < 0) throw new InvalidTermException("Invalid string")
      quote.append(typea.toChar)
    }
    (true, typea, svala)
  }

  private def readNondecimalNumberToken(svala: String): Token = {
    svala.charAt(1) match {
      case 'b' => new Token(new BigInteger(svala.substring(2), 2).toString, Tokenizer.INTEGER) // binary
      case 'o' => new Token(new BigInteger(svala.substring(2), 8).toString, Tokenizer.INTEGER) // octal
      case 'x' => new Token(new BigInteger(svala.substring(2), 16).toString, Tokenizer.INTEGER) // hexadecimal
    }
  }

  private def readCharConstantToken(): Token = {
    val typec: Int = tokenConsume()
    val svalc: String = sval
    val intVal: Int = Tokenizer.isCharacterCodeConstantToken(typec, svalc)
    if (intVal != -1) {
      new Token("" + intVal, Tokenizer.INTEGER)
    } else {
      throw new InvalidTermException("Character code constant starting with 0'<X> cannot be recognized.")
    }
  }

  private def readFloatToken(svala: String,typeb: Int, svalb: String): Token = {
    val typec: Int = tokenConsume()
    val svalc: String = sval
    if (typec != StreamTokenizer.TT_WORD) {
      tokenPushBack()
      pushBack2 = new core.parser.Tokenizer.PushBack(typeb, svalb)
      new Token(svala, Tokenizer.INTEGER)
    } else {
      var exponent: Int = svalc.indexOf("E")
      if (exponent == -1) exponent = svalc.indexOf("e")
      if (exponent >= 1) {
        if (exponent == svalc.length - 1) {
          val typeb2: Int = tokenConsume()
          if (typeb2 == '+' || typeb2 == '-') {
            val typec2: Int = tokenConsume()
            val svalc2: String = sval
            if (typec2 == StreamTokenizer.TT_WORD) {
              java.lang.Long.parseLong(svalc.substring(0, exponent))
              svalc2.toInt
              return new Token(svala + "." + svalc + typeb2.toChar + svalc2, Tokenizer.FLOAT)
            }
          }
        }
      }
      svala + "." + svalc.toDouble
      new Token(svala + "." + svalc, Tokenizer.FLOAT)
    }
  }


  /**
    * Method to parse the next token as a number.
    *
    * @note This is quite a complex part of the tokeniser as we need to handle Long.MIN_VALUE that comes in as Long.MAX_VALUE+1 due to the unary minus operator
    * being regarded as a separate token.
    *
    * @param svala
    * @return a Token Representing a Number.
    */
  def readNextNumberToken(svala: String): Token = {
    try {
      if (svala.matches("0[bxo][A-F\\d]*")) {
        readNondecimalNumberToken(svala)
      } else {
        val typeb: Int = tokenConsume()
        val svalb: String = sval
        if (typeb != '.' && typeb != '\'') {
          tokenPushBack()
          new Token(new BigInteger(svala).toString, Tokenizer.INTEGER) // we use big decimal here to avoid the long situation about as mentioned.
        } else if (typeb == '\'' && ("0" == svala)) {
          readCharConstantToken()
        } else if (typeb != '.') {
          throw new InvalidTermException("A number starting with 0-9 cannot be rcognized as an int and does not have a fraction '.'")
        } else {
          readFloatToken(svala,typeb,svalb)
        }
      }
    } catch {
      case e: NumberFormatException =>
        throw new InvalidTermException("A term starting with 0-9 cannot be parsed as a number")
    }
  }

  @throws(classOf[IOException])
  @throws(classOf[InvalidTermException])
  private[parser] def readNextToken: Token = {
    var typea: Int = 0
    var svala: String = null
    if (pushBack2 != null) {
      typea = pushBack2.typea
      svala = pushBack2.svala
      pushBack2 = null
    } else {
      typea = tokenConsume()
      svala = sval
    }
    while (Tokenizer.isWhite(typea)) {
      typea = tokenConsume()
      svala = sval
    }
    if (typea == '%') {
      do {
        typea = tokenConsume
      } while (typea != '\r' && typea != '\n' && typea != StreamTokenizer.TT_EOF)
      tokenPushBack
      return readNextToken
    }
    if (typea == '/') {
      var typeb: Int = tokenConsume
      if (typeb == '*') {
        do {
          typea = typeb
          typeb = tokenConsume
          if (typea == -1 && typeb == -1) throw new InvalidTermException("Invalid multi-line comment statement")
        } while (typea != '*' || typeb != '/')
        return readNextToken
      }
      else {
        tokenPushBack
      }
    }
    _tokenStart = _tokenOffset - tokenLength + 1
    if (typea == StreamTokenizer.TT_EOF) return new Token("", Tokenizer.EOF)
    if (typea == '(') return new Token("(", Tokenizer.LPAR)
    if (typea == ')') return new Token(")", Tokenizer.RPAR)
    if (typea == '{') return new Token("{", Tokenizer.LBRA2)
    if (typea == '}') return new Token("}", Tokenizer.RBRA2)
    if (typea == '[') return new Token("[", Tokenizer.LBRA)
    if (typea == ']') return new Token("]", Tokenizer.RBRA)
    if (typea == '|') return new Token("|", Tokenizer.BAR)
    if (typea == '!') return new Token("!", Tokenizer.ATOM)
    if (typea == ',') return new Token(",", Tokenizer.OPERATOR)
    if (typea == '.') {
      val typeb: Int = tokenConsume
      if (Tokenizer.isWhite(typeb) || typeb == '%' || typeb == StreamTokenizer.TT_EOF) return new Token(".", Tokenizer.END)
      else tokenPushBack
    }
    var isNumber: Boolean = false
    if (typea == StreamTokenizer.TT_WORD) {
      val firstChar: Char = svala.charAt(0)
      if (Character.isUpperCase(firstChar) || '_' == firstChar) return new Token(svala, Tokenizer.VARIABLE)
      else if (firstChar >= '0' && firstChar <= '9') isNumber = true
      else {
        val typeb: Int = tokenConsume
        tokenPushBack
        if (typeb == '(') return new Token(svala, Tokenizer.ATOM | Tokenizer.FUNCTOR)
        if (Tokenizer.isWhite(typeb)) return new Token(svala, Tokenizer.ATOM | Tokenizer.OPERATOR)
        return new Token(svala, Tokenizer.ATOM)
      }
    }
    if (typea == '\'' || typea == '\"' || typea == '`') {
      var qType: Int = typea
      val quote: StringBuffer = new StringBuffer
      var continue = true
      while (continue) {
        val temp = evaluateTokens(qType, quote)
        typea = temp._2
        svala = temp._3
        continue = temp._1
      }
      val quoteBody: String = quote.toString
      qType = if (qType == '\'') Tokenizer.SQ_SEQUENCE else if (qType == '\"') Tokenizer.DQ_SEQUENCE else Tokenizer.SQ_SEQUENCE
      if (qType == Tokenizer.SQ_SEQUENCE) {
        if (Parser.isAtom(quoteBody)) qType = Tokenizer.ATOM
        val typeb: Int = tokenConsume
        tokenPushBack
        if (typeb == '(') return new Token(quoteBody, qType | Tokenizer.FUNCTOR)
      }
      return new Token(quoteBody, qType)
    }
    if (Arrays.binarySearch(Tokenizer.GRAPHIC_CHARS, typea.toChar) >= 0) {
      val symbols: StringBuffer = new StringBuffer
      var typeb: Int = typea
      while (Arrays.binarySearch(Tokenizer.GRAPHIC_CHARS, typeb.toChar) >= 0) {
        symbols.append(typeb.toChar)
        typeb = tokenConsume
      }
      tokenPushBack
      return new Token(symbols.toString, Tokenizer.OPERATOR)
    }
    if (isNumber) {
      return readNextNumberToken(svala)
    }
    throw new InvalidTermException("Unknown Unicode character: " + typea + "  (" + svala + ")")
  }

  /**
    * Read a token from the stream, and increase tokenOffset
    *
    * @return the readed token
    * @throws IOException
    */
  @throws(classOf[IOException])
  private def tokenConsume(): Int = {
    val t: Int = super.nextToken
    tokenLength = if (sval == null) 1 else sval.length
    _tokenOffset += tokenLength
    t
  }

  /**
    * Push back the last readed token
    */
  private def tokenPushBack(): Unit = {
    super.pushBack()
    _tokenOffset -= tokenLength
  }
}