package com.szadowsz.gospel.core.parser

import java.math.BigInteger

import com.szadowsz.gospel.core.data.{Float, Int, Long, Struct, Var}
import com.szadowsz.gospel.core.db.ops.OperatorManager
import com.szadowsz.gospel.core.exception.InvalidTermException
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParserSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Parser"

  it should "parse terms successfully" in {
    val p: Parser = new Parser(new OperatorManager, "hello.")
    val result: Struct = new Struct("hello")
    p.nextTerm(true) shouldBe result
  }

  it should "parse end of file successfully" in {
    val p = new Parser(new OperatorManager, "")
    p.nextTerm(false) shouldBe null
  }

  it should "not parse unary plus operators" in {
    // SICStus PrologEngine interprets "n(+100)" as "n(100)"
    // GNU PrologEngine interprets "n(+100)" as "n(+(100))"
    // The ISO Standard says + is not a unary operator
    val p = new Parser(new OperatorManager, "n(+100).\n")

    intercept[InvalidTermException] {
      p.nextTerm(true)
    }
  }

  it should "parse unary minus operators" in {
    // TODO Check the interpretation by other engines
    // SICStus PrologEngine interprets "n(+100)" as "n(100)"
    // GNU PrologEngine interprets "n(+100)" as "n(+(100))"
    // What does the ISO Standard say about that?
    val p = new Parser(new OperatorManager, "n(-100).\n")
    val result = new Struct("n", new Int(-100))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }

  it should "parse binary minus operators" in {
    val p = new Parser(new OperatorManager, "abs(3-11)")
    val result = new Struct("abs", new Struct("-", new Int(3), new Int(11)))
    p.nextTerm(false) shouldBe result
  }

  it should "parse list with tail" in {
    val p = new Parser(new OperatorManager, "[p|Y]")
    val result = new Struct(new Struct("p"), new Var("Y"))
    result.resolveTerm()
    p.nextTerm(false) shouldBe result
  }

  it should "parse braces" in {
    val s = "{a,b,[3,{4,c},5],{a,b}}"
    val parser = new Parser(new OperatorManager, s)
    parser.nextTerm(false).toString shouldBe s
  }

  it should "parse universal operator" in {
    val p = new Parser(new OperatorManager, "p =.. q.")
    val result = new Struct("=..", new Struct("p"), new Struct("q"))
    p.nextTerm(true) shouldBe result
  }

  it should "parse dot operator" in {
    val s = "class('java.lang.Integer').'MAX_VALUE'"
    val om = new OperatorManager
    om.opNew(".", "xfx", 600)
    val p = new Parser(om, s)
    val result = new Struct(".", new Struct("class", new Struct("java.lang.Integer")), new Struct("MAX_VALUE"))
    p.nextTerm(false) shouldBe result
  }

  it should "parse bracketed operator as term" in {
    val s = "u (b1) b2 (b3)"
    val om = new OperatorManager()
    om.opNew("u", "fx", 200)
    om.opNew("b1", "yfx", 400)
    om.opNew("b2", "yfx", 500)
    om.opNew("b3", "yfx", 300)
    val p = new Parser(om, s)
    val result = new Struct("b2", new Struct("u", new Struct("b1")), new Struct("b3"))
    p.nextTerm(false) shouldBe result
  }

  it should "parse bracketed operator as term #2" in {
    val s = "(u) b1 (b2) b3 a"
    val om = new OperatorManager
    om.opNew("u", "fx", 200)
    om.opNew("b1", "yfx", 400)
    om.opNew("b2", "yfx", 500)
    om.opNew("b3", "yfx", 300)
    val p = new Parser(om, s)
    val result = new Struct("b1", new Struct("u"), new Struct("b3", new Struct("b2"), new Struct("a")))
    p.nextTerm(false) shouldBe result
  }

  it should "parse valid integer binary representation" in {
    val n = "0b101101"
    val p = new Parser(new OperatorManager, n)
    val result = new Int(45)
    p.nextTerm(false) shouldBe result
  }

  it should "not parse invalid integer binary representation" in {
    val invalid = "0b101201"
    intercept[InvalidTermException] {
      new Parser(new OperatorManager, invalid).nextTerm(false)
    }
  }

  it should "parse valid integer octal representation" in {
    val n = "0o77351"
    val p = new Parser(new OperatorManager, n)
    val result = new Int(32489)
    p.nextTerm(false) shouldBe result
  }

  it should "not parse invalid integer octal representation" in {
    val invalid = "0o78351"
    intercept[InvalidTermException] {
      new Parser(new OperatorManager, invalid).nextTerm(false)
    }
  }

  it should "parse valid integer hexadecimal representation" in {
    val n = "0xDECAF"
    val p = new Parser(new OperatorManager, n)
    val result = new Int(912559)
    p.nextTerm(false) shouldBe result
  }

  it should "not parse invalid integer hexadecimal representation" in {
    val invalid = "0xG"
    intercept[InvalidTermException] {
      new Parser(new OperatorManager, invalid).nextTerm(false)
    }
  }

  it should "parse empty DCG" in {
    val s = "{}"
    val p = new Parser(new OperatorManager, s)
    val result = new Struct("{}")
    p.nextTerm(false) shouldBe result
  }

  it should "parse a single DCG action" in {
    val s = "{hello}"
    val p = new Parser(new OperatorManager, s)
    val result = new Struct("{}", new Struct("hello"))
    p.nextTerm(false) shouldBe result
  }

  it should "parse a multiple DCG action" in {
    val s = "{a, b, c}"
    val p = new Parser(new OperatorManager, s)
    val result = new Struct("{}", new Struct(",", new Struct("a"), new Struct(",", new Struct("b"), new Struct("c"))))
    p.nextTerm(false) shouldBe result
  }

  it should "parse a DCG with operators" in {
    val input = "{A =.. B, hotel, 2}"
    val result = new Struct("{}", new Struct(",", new Struct("=..", new Var("A"), new Var("B")), new Struct(",", new Struct("hotel"), new Int(2))))
    result.resolveTerm()
    val p = new Parser(new OperatorManager, input)
    p.nextTerm(false) shouldBe result
  }

  it should "not parse a DCG with a missing element" in {
    val s = "{1, 2, , 4}"
    val p = new Parser(new OperatorManager, s)
    intercept[InvalidTermException] {
      p.nextTerm(false)
    }
  }

  it should "not parse a DCG with a comma as another symbol" in {
    val s = "{1 @ 2 @ 4}"
    val p = new Parser(new OperatorManager, s)
    intercept[InvalidTermException] {
      p.nextTerm(false)
    }
  }

  it should "not parse an uncomplete DCG action" in {
    var s = "{1, 2,}"
    val p = new Parser(new OperatorManager, s)
    intercept[InvalidTermException] {
      p.nextTerm(false)
    }
  }

  it should "not parse an uncomplete DCG action #2" in {
    var s = "{1, 2"
    val p = new Parser(new OperatorManager, s)
    intercept[InvalidTermException] {
      p.nextTerm(false)
    }
  }

  it should "parse mutliline comments" in {
    val theory = "t1." + "\n" + "/*" + "\n" + "t2" + "\n" + "*/" + "\n" + "t3." + "\n"
    val p = new Parser(new OperatorManager, theory)
    p.nextTerm(true) shouldBe new Struct("t1")
    p.nextTerm(true) shouldBe new Struct("t3")
  }


  it should "not parse singe line comments with invalid line breaks" in {
    val s = "out('" + "can_do(X).\n" + "can_do(Y).\n" + "')."
    val p = new Parser(new OperatorManager, s)
    intercept[InvalidTermException] {
      p.nextTerm(true)
    }
  }

  it should "parse minimum long value successfully" in {
    val p = new Parser(new OperatorManager, s"n(${scala.Long.MinValue}).\n")
    val result = new Struct("n", Long(scala.Long.MinValue))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }

  it should "parse minimum binary long value successfully" in {
    val binary = new BigInteger(scala.Long.MinValue.toString).toString(2).substring(1)
    val p = new Parser(new OperatorManager, s"n(-0b$binary).\n")
    val result = new Struct("n", Long(scala.Long.MinValue))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }

  it should "parse minimum octal long value successfully" in {
    val octal = new BigInteger(scala.Long.MinValue.toString).toString(8).substring(1)
    val p = new Parser(new OperatorManager, s"n(-0o$octal).\n")
    val result = new Struct("n", Long(scala.Long.MinValue))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }

  it should "parse minimum hexadecimal long value successfully" in {
    val hex = new BigInteger(scala.Long.MinValue.toString).toString(16).substring(1)
    val p = new Parser(new OperatorManager, s"n(-0x$hex).\n")
    val result = new Struct("n", Long(scala.Long.MinValue))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }

  it should "parse maximum long value successfully" in {
    val p = new Parser(new OperatorManager, s"n(${scala.Long.MaxValue}).\n")
    val result = new Struct("n", Long(scala.Long.MaxValue))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }

  it should "parse minimum double value successfully" in {
    val p = new Parser(new OperatorManager, s"n(${scala.Double.MinValue}).\n")
    val result = new Struct("n", Float(scala.Double.MinValue))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }

  it should "parse maximum double value successfully" in {
    val p = new Parser(new OperatorManager, s"n(${scala.Double.MaxValue}).\n")
    val result = new Struct("n", Float(scala.Double.MaxValue))
    result.resolveTerm()

    p.nextTerm(true) shouldBe result
  }
  /** TODO More tests on Parser
    * Character code for Integer representation
    * :-op(500, yfx, v). 3v2 NOT CORRECT, 3 v 2 CORRECT
    * 3+2 CORRECT, 3 + 2 CORRECT
    * +(2, 3) is now acceptable
    * what about f(+)
    */
}