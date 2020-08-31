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
    new NParser().parseTerm("hello.") shouldBe new Struct("hello")
  }

  /**
    * Final Indexes Should be as follows:
    *
    * Line: 1 - no new lines in the text
    *
    * Column: 1 - no characters in the text
    */
  it should "not parse end of file" in {
    intercept[InvalidTermException] {
      new NParser().parseTerm("")
    }
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
    val result = new Struct("=..", new Struct("p"), new Struct("q"))

    new NParser().parseTerm("p =.. q.") shouldBe result
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
    val result = new Struct(new Struct("p"), new Var("Y"))
    result.resolveVars()

    new NParser().parseTerm("[p|Y]") shouldBe result
  }

//  /**
//    * SICStus PrologEngine interprets "n(+100)" as "n(100)"
//    * GNU PrologEngine interprets "n(+100)" as "n(+(100))"
//    * The ISO Standard says + is not a unary operator
//    *
//    * Final Indexes Should be as follows:
//    *
//    * Line: 1 - no new lines in the text
//    *
//    *            1 2 3 4
//    * Column: 3 - n ( +
//    */
//  it should "not parse unary plus operators" in {
//
//   val caught = intercept[InvalidTermException] {
//      new NParser().parseTerm("n(+100).\n")
//    }
//
//    caught.getMessage shouldBe "Unexpected Token '+'"
//    caught.getTerm shouldBe "+"
//    caught.getLine shouldBe 1
//    caught.getCol shouldBe 3
//  }
  
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
  it should "parse unary plus operators" in {
  
    val result = new Struct("n", Int(100))
    result.resolveVars()
  
    new NParser().parseTerm("n(+100).\n") shouldBe result
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
    val result = new Struct("n", Int(-100))
    result.resolveVars()

    new NParser().parseTerm("n(-100).\n") shouldBe result
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
    val result = new Struct("abs", new Struct("-", Int(3), Int(11)))

    new NParser().parseTerm("abs(3-11)") shouldBe result
  }
  
  it should "parse simple mathematical equations" in {
    val result = new Struct("=:=", Int(3),Int(3))
    
    new NParser().parseTerm("3 =:= 3.") shouldBe result
  }
  
   it should "parse complex mathematical equations with Leading Symbol" in {
    val result = new Struct("=:=", new Struct("*", Int(3), Int(2)), new Struct("-", Int(7), Int(1)))
    
    new NParser().parseTerm("'=:='(3 * 2, 7 - 1).") shouldBe result
  }
  
  it should "parse complex mathematical equations without Leading Symbol" in {
   val result = new Struct("=:=", new Struct("*", Int(3), Int(2)), new Struct("-", Int(7), Int(1)))
    
    new NParser().parseTerm("3 * 2 =:= 7 - 1.") shouldBe result
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
    val it = new NParser().parseTerms("hello.\n%single line comment\ngoodbye.").iterator
  
    it.next() shouldBe new Struct("hello")
  
    it.next() shouldBe new Struct("goodbye")
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
    val it = new NParser().parseTerms("hello.%single line comment\ngoodbye.").iterator
  
    it.next() shouldBe new Struct("hello")
   
    it.next() shouldBe new Struct("goodbye")
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
    val it = new NParser().parseTerms(theory).iterator
  
    it.next() shouldBe new Struct("t1")
   
    it.next() shouldBe new Struct("t3")
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
    val result = new Struct("{}")

    new NParser().parseTerm(s) shouldBe result
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
   val result = new Struct("{}", new Struct("hello"))

    new NParser().parseTerm(s) shouldBe result
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
   
    new NParser().parseTerm(input) shouldBe result
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
    val result = new Struct("{}",
      new Struct(",",
        new Struct("a"),
        new Struct(",",
          new Struct("b"),
          new Struct("c")
        )
      )
    )

    new NParser().parseTerm(s) shouldBe result
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
    val result =  new NParser().parseTerm(s)
  
  
    result.toString shouldBe s
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

    val caught = intercept[InvalidTermException] {
      new NParser().parseTerm(s)
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
   
    val caught = intercept[InvalidTermException] {
      new NParser().parseTerm(s)
    }

    caught.getMessage shouldBe "mismatched input '@' expecting {'}', ','}"
    caught.getTerm shouldBe "@"
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
    val s = "{1, 2,}"
  
    val caught = intercept[InvalidTermException] {
      new NParser().parseTerm(s)
    }

    caught.getMessage should startWith ("mismatched input '}'")
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
  
    val caught = intercept[InvalidTermException] {
      new NParser().parseTerm(s)
    }

    caught.getMessage shouldBe "extraneous input '<EOF>' expecting {'}', ','}"
    caught.getTerm shouldBe "<EOF>"
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
  
    val result = new Struct(".", new Struct("class", new Struct("java.lang.Integer")), new Struct("MAX_VALUE"))
  
    new NParser().parseTerm(s)(om) shouldBe result
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
  
    val result = new Struct("b2", new Struct("u", new Struct("b1")), new Struct("b3"))
  
    new NParser().parseTerm(s)(om) shouldBe result
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
   
    val result = new Struct("b1", new Struct("u"), new Struct("b3", new Struct("b2"), new Struct("a")))
  
    new NParser().parseTerm(s)(om) shouldBe result
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
    val result = Int(45)
  
    new NParser().parseTerm(n) shouldBe result
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
      new NParser().parseTerm(invalid)
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
    val result = Int(32489)
    new NParser().parseTerm(n) shouldBe result
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
      new NParser().parseTerm(invalid)
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
    val result = Int(912559)
    new NParser().parseTerm(n) shouldBe result
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
      new NParser().parseTerm(invalid)
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
  it should "not parse single line comments with invalid line breaks" in {
    val s = "out('" + "can_do(X).\n" + "can_do(Y).\n" + "')."
    intercept[InvalidTermException] {
      new NParser().parseTerm(s)
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
    val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    new NParser().parseTerm(s"n(${scala.Long.MinValue}).\n") shouldBe result
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
    val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    new NParser().parseTerm(s"n(-0b$binary).\n") shouldBe result
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
    val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    new NParser().parseTerm(s"n(-0o$octal).\n") shouldBe result
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
     val result = new Struct("n", Int(scala.Long.MinValue))
    result.resolveVars()

    new NParser().parseTerm(s"n(-0x$hex).\n") shouldBe result
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
    val result = new Struct("n", Int(scala.Long.MaxValue))
    result.resolveVars()

    new NParser().parseTerm(s"n(${scala.Long.MaxValue}).\n") shouldBe result
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
    val result = new Struct("n", Float(scala.Double.MinValue))
    result.resolveVars()

    new NParser().parseTerm(s"n(${scala.Double.MinValue}).\n") shouldBe result
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
    val result = new Struct("n", Float(scala.Double.MaxValue))
    result.resolveVars()

    new NParser().parseTerm(s"n(${scala.Double.MaxValue}).\n") shouldBe result
  }
}
