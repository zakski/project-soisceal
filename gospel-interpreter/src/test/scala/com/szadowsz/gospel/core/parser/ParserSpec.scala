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

import java.math.BigInteger

import com.szadowsz.gospel.core.data.{Int, Float, Struct, Var}
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.exception.InvalidTermException
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ParserSpec extends FlatSpec with Matchers with BeforeAndAfter {

  behavior of "Parser"

  implicit val opManager: OperatorManager = new OperatorManager

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6 7
    * Column: 7 - h e l l 0 .
    */
  it should "parse a simple term" in {
    val p: Parser = new Parser("hello.")
    p.nextTerm(true) shouldBe new Struct("hello")
    p.lineNo shouldBe 1
    p.colNo shouldBe 7
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    * Column: 1 - no characters in the text
    */
  it should "parse end of file" in {
    val p = new Parser("")
    p.nextTerm(false) shouldBe null
    p.lineNo shouldBe 1
    p.colNo shouldBe 1
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6 7 8 9
    * Column: 9 - p   = . .   q .
    */
  it should "parse universal operator" in {
    val p = new Parser("p =.. q.")
    val result = new Struct("=..", new Struct("p"), new Struct("q"))

    p.nextTerm(true) shouldBe result
    p.lineNo shouldBe 1
    p.colNo shouldBe 9
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6
    * Column: 6 - [ p | Y ]
    */
  it should "parse list with tail" in {
    val p = new Parser("[p|Y]")
    val result = new Struct(new Struct("p"), new Var("Y"))
    result.resolveVars()

    p.nextTerm(false) shouldBe result
    p.lineNo shouldBe 1
    p.colNo shouldBe 6
  }

  /**
    * SICStus PrologEngine interprets "n(+100)" as "n(100)"
    * GNU PrologEngine interprets "n(+100)" as "n(+(100))"
    * The ISO Standard says + is not a unary operator
    *
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4
    * Column: 3 - n ( +
    */
  it should "not parse unary plus operators" in {

    val p = new Parser("n(+100).\n")

    val caught = intercept[InvalidTermException] {
      p.nextTerm(true)
    }

    caught.getMessage shouldBe "Unexpected Token '+'"
    caught.getTerm shouldBe "+"
    caught.getLine shouldBe 1
    caught.getCol shouldBe 3
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 2 - new line at end of the text
    *
    *            1 2 3 4 5 6 7 8 9  1
    * Column: 1 - n ( - 1 0 0 ) . \n
    */
  it should "parse unary minus operators" in {
    val p = new Parser("n(-100).\n")
    val result = new Struct("n", Int(-100))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
    p.lineNo shouldBe 2
    p.colNo shouldBe 1
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10
    * Column: 10 - a b s ( 3 - 1 1 )
    */
  it should "parse binary minus operators" in {
    val p = new Parser("abs(3-11)")
    val result = new Struct("abs", new Struct("-", Int(3), Int(11)))

    p.nextTerm(false) shouldBe result
    p.lineNo shouldBe 1
    p.colNo shouldBe 10
  }
  
  it should "parse simple mathematical equations" in {
    val p = new Parser("3 =:= 3).")
    val result = new Struct("=:=", Int(3),Int(3))
    
    p.nextTerm(false) shouldBe result
  }
  
   it should "parse complex mathematical equations with Leading Symbol" in {
    val p = new Parser("'=:='(3 * 2, 7 - 1).")
    val result = new Struct("=:=", new Struct("*", Int(3), Int(2)), new Struct("-", Int(7), Int(1)))
    
    p.nextTerm(false) shouldBe result
  }
  
  it should "parse complex mathematical equations without Leading Symbol" in {
    val p = new Parser("3 * 2 =:= 7 - 1.")
    val result = new Struct("=:=", new Struct("*", Int(3), Int(2)), new Struct("-", Int(7), Int(1)))
    
    p.nextTerm(false) shouldBe result
  }
  /**
    * First Indexes Should be as follows:
    *
    * Line: 2 - new line at end of the first term
    *
    *            1 2 3 4 5 6  1
    * Column: 1 - h e l l o \n
    *
    * Final Indexes Should be as follows:
    *
    * Line: 3 - new line at end of comment
    *
    *            1 2 3 4 5 6 7 8 9
    * Column: 1 - g o o d b y e .
    */
  it should "parse single line comment on separate line" in {
    val p: Parser = new Parser("hello.\n%single line comment\ngoodbye.")

    p.nextTerm(true) shouldBe new Struct("hello")
    p.lineNo shouldBe 2
    p.colNo shouldBe 1

    p.nextTerm(true) shouldBe new Struct("goodbye")
    p.lineNo shouldBe 3
    p.colNo shouldBe 9
  }

  /**
    * First Indexes Should be as follows:
    *
    * Line: 2 - new line at end of comment
    *
    *            1 2 3 4 5 6  1
    * Column: 1 - h e l l o \n
    *
    * Final Indexes Should be as follows:
    *
    * Line: 2 - new line at end of comment
    *
    *            1 2 3 4 5 6 7 8 9
    * Column: 1 - g o o d b y e .
    */
  it should "parse single line comment on same line" in {
    val p: Parser = new Parser("hello.%single line comment\ngoodbye.")

    p.nextTerm(true) shouldBe new Struct("hello")
    p.lineNo shouldBe 2
    p.colNo shouldBe 1

    p.nextTerm(true) shouldBe new Struct("goodbye")
    p.lineNo shouldBe 2
    p.colNo shouldBe 9
  }

  /**
    * First Indexes Should be as follows:
    *
    * Line: 2 - new line at end of the first term
    *
    *            1 2 3 4  1
    * Column: 1 - t 1 . \n
    *
    * Final Indexes Should be as follows:
    *
    * Line: 6 - new line at end of last term
    *
    *            1 2 3 4  1
    * Column: 1 - t 3 . \n
    */
  it should "parse multi-line comments" in {
    val theory = """t1.
                   |/*
                   |t2.
                   |*/
                   |t3.
                 """.stripMargin
    val p = new Parser(theory)

    p.nextTerm(true) shouldBe new Struct("t1")
    p.lineNo shouldBe 2
    p.colNo shouldBe 1

    p.nextTerm(true) shouldBe new Struct("t3")
    p.lineNo shouldBe 6
    p.colNo shouldBe 1
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1- no new lines
    *
    *            1 2 3
    * Column: 3 - { }
    */
  it should "parse empty DCG" in {
    val s = "{}"
    val p = new Parser(s)
    val result = new Struct("{}")

    p.nextTerm(false) shouldBe result
    p.lineNo shouldBe 1
    p.colNo shouldBe 3
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6 7 8
    * Column: 8 - { h e l l O }
    */
  it should "parse a single DCG action" in {
    val s = "{hello}"
    val p = new Parser(s)
    val result = new Struct("{}", new Struct("hello"))

    p.nextTerm(false) shouldBe result
    p.lineNo shouldBe 1
    p.colNo shouldBe 8
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20
    * Column: 20 - { A   = . .   B ,     h  o  t  e  l  ,     2  }
    */
  it should "parse a DCG with operators" in {
    val input = "{A =.. B, hotel, 2}"
    val result = new Struct("{}",
      new Struct(",",
        new Struct("=..",
          new Var("A"),
          new Var("B")),
        new Struct(",",
          new Struct("hotel"),
          Int(2)
        )
      )
    )
    result.resolveVars()
    val p = new Parser(input)

    p.nextTerm(false) shouldBe result
    p.lineNo shouldBe 1
    p.colNo shouldBe 20
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10
    * Column: 10 - { a ,   b ,   c }
    */
  it should "parse a multiple DCG action" in {
    val s = "{a, b, c}"
    val p = new Parser(s)
    val result = new Struct("{}",
      new Struct(",",
        new Struct("a"),
        new Struct(",",
          new Struct("b"),
          new Struct("c")
        )
      )
    )

    p.nextTerm(false) shouldBe result
    p.lineNo shouldBe 1
    p.colNo shouldBe 10
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse braces" in {
    val s = "{a,b,[3,{4,c},5],{a,b}}"
    val parser = new Parser(s)
    val result = parser.nextTerm(false)

    result.toString shouldBe s
    parser.lineNo shouldBe 1
    parser.colNo shouldBe 24
  }

  /**
     * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6 7 8 9
    * Column: 8 - { 1 ,   2 ,   ,
    */
  it should "not parse a DCG with a missing element" in {
    val s = "{1, 2, , 4}"
    val p = new Parser(s)

    val caught = intercept[InvalidTermException] {
      p.nextTerm(false)
    }

    caught.getMessage shouldBe "Unexpected Token ','"
    caught.getTerm shouldBe ","
    caught.getLine shouldBe 1
    caught.getCol shouldBe 8
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4
    * Column: 3 - { 1 @
    */
  it should "not parse a DCG with a comma as another symbol" in {
    val s = "{1 @ 2 @ 4}"
    val p = new Parser(s)

    val caught = intercept[InvalidTermException] {
      p.nextTerm(false)
    }

    caught.getMessage shouldBe "Missing right braces '{1' -> here <-"
    caught.getTerm shouldBe "1"
    caught.getLine shouldBe 1
    caught.getCol shouldBe  4 // TODO should be 3 ideally but not currently smart enough
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6 7 8
    * Column: 7 - { 1 ,   2 , }
    */
  it should "not parse an uncomplete DCG action" in {
    var s = "{1, 2,}"
    val p = new Parser(s)

    val caught = intercept[InvalidTermException] {
      p.nextTerm(false)
    }

    caught.getMessage shouldBe "Unexpected Token '}'"
    caught.getTerm shouldBe "}"
    caught.getLine shouldBe 1
    caught.getCol shouldBe  7
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6
    * Column: 7 - { 1 ,   2
    */
  it should "not parse an uncomplete DCG action #2" in {
    var s = "{1, 2"
    val p = new Parser(s)

    val caught = intercept[InvalidTermException] {
      p.nextTerm(false)
    }

    caught.getMessage shouldBe "Missing right braces '{','(1,2)' -> here <-"
    caught.getTerm shouldBe "','(1,2)"
    caught.getLine shouldBe 1
    caught.getCol shouldBe 6
  }

/**
  * Final Indexes Should be as follows:
  *
  * Line: 1 - no new lines in the text
  *
  *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38
  * Column: 39 - c l a s s ( ' j a  v  a  .  l  a  n  g  .  I  n  t  e  g  e  r  '  )  .  '  M  A  X  _  V  A  L  U  E
  *              39
  *             '
  */
  it should "parse dot operator" in {
    val s = "class('java.lang.Integer').'MAX_VALUE'"
    val om = new OperatorManager
    om.opNew(".", "xfx", 600)
    val parser = new Parser(s)(om)

    val result = new Struct(".", new Struct("class", new Struct("java.lang.Integer")), new Struct("MAX_VALUE"))

    parser.nextTerm(false) shouldBe result
    parser.lineNo shouldBe 1
    parser.colNo shouldBe 39
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
    * Column: 15 - u   ( b 1 )   b 2     (  b  3  )
    */
  it should "parse bracketed operator as term" in {
    val s = "u (b1) b2 (b3)"
    val om = new OperatorManager()
    om.opNew("u", "fx", 200)
    om.opNew("b1", "yfx", 400)
    om.opNew("b2", "yfx", 500)
    om.opNew("b3", "yfx", 300)
    val parser = new Parser(s)(om)

    val result = new Struct("b2", new Struct("u", new Struct("b1")), new Struct("b3"))

    parser.nextTerm(false) shouldBe result
    parser.lineNo shouldBe 1
    parser.colNo shouldBe 15
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17
    * Column: 17 - ( u )   b 1   ( b  2  )     b  3     a
    */
  it should "parse bracketed operator as term #2" in {
    val s = "(u) b1 (b2) b3 a"
    val om = new OperatorManager
    om.opNew("u", "fx", 200)
    om.opNew("b1", "yfx", 400)
    om.opNew("b2", "yfx", 500)
    om.opNew("b3", "yfx", 300)
    val parser = new Parser(s)(om)

    val result = new Struct("b1", new Struct("u"), new Struct("b3", new Struct("b2"), new Struct("a")))

    parser.nextTerm(false) shouldBe result
    parser.lineNo shouldBe 1
    parser.colNo shouldBe 17
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *            1 2 3 4 5 6 7 8 9
    * Column: 9 - 0 b 1 0 1 1 0 1
    */
  it should "parse valid integer binary representation" in {
    val n = "0b101101"
    val parser = new Parser(n)

    val result = Int(45)

    parser.nextTerm(false) shouldBe result
    parser.lineNo shouldBe 1
    parser.colNo shouldBe 9
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "not parse invalid integer binary representation" in {
    val invalid = "0b101201"
    intercept[InvalidTermException] {
      new Parser(invalid).nextTerm(false)
    }
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse valid integer octal representation" in {
    val n = "0o77351"
    val p = new Parser(n)
    val result = new Int(32489)
    p.nextTerm(false) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "not parse invalid integer octal representation" in {
    val invalid = "0o78351"
    intercept[InvalidTermException] {
      new Parser(invalid).nextTerm(false)
    }
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse valid integer hexadecimal representation" in {
    val n = "0xDECAF"
    val p = new Parser(n)
    val result = new Int(912559)
    p.nextTerm(false) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "not parse invalid integer hexadecimal representation" in {
    val invalid = "0xG"
    intercept[InvalidTermException] {
      new Parser(invalid).nextTerm(false)
    }
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "not parse singe line comments with invalid line breaks" in {
    val s = "out('" + "can_do(X).\n" + "can_do(Y).\n" + "')."
    val p = new Parser(s)
    intercept[InvalidTermException] {
      p.nextTerm(true)
    }
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse minimum long value successfully" in {
    val p = new Parser(s"n(${scala.Long.MinValue}).\n")
    val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse minimum binary long value successfully" in {
    val binary = new BigInteger(scala.Long.MinValue.toString).toString(2).substring(1)
    val p = new Parser(s"n(-0b$binary).\n")
    val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse minimum octal long value successfully" in {
    val octal = new BigInteger(scala.Long.MinValue.toString).toString(8).substring(1)
    val p = new Parser(s"n(-0o$octal).\n")
    val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse minimum hexadecimal long value successfully" in {
    val hex = new BigInteger(scala.Long.MinValue.toString).toString(16).substring(1)
    val p = new Parser(s"n(-0x$hex).\n")
    val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse maximum long value successfully" in {
    val p = new Parser(s"n(${scala.Long.MaxValue}).\n")
    val result = new Struct("n", Int(scala.Long.MaxValue))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse minimum double value successfully" in {
    val p = new Parser(s"n(${scala.Double.MinValue}).\n")
    val result = new Struct("n", Float(scala.Double.MinValue))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    *             1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
    * Column: 24 - { a , b , [ 3 , {  4  ,  c  }  ,  5  ]  ,  {  a  ,  b  }  }
    */
  it should "parse maximum double value successfully" in {
    val p = new Parser(s"n(${scala.Double.MaxValue}).\n")
    val result = new Struct("n", Float(scala.Double.MaxValue))
    result.resolveVars()

    p.nextTerm(true) shouldBe result
  }
}
