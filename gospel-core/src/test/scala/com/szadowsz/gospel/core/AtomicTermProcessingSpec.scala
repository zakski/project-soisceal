package com.szadowsz.gospel.core

import alice.tuprolog.{Number, Struct}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class AtomicTermProcessingSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("atom_length/2") {
    it("should pass negative test #1") {
      val solution = prolog.solve("atom_length('scarlet', 5).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("atom_length('enchanted evening', N).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("N").asInstanceOf[Number]
      replaceUnderscore(result.toString) shouldBe "17"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("atom_length('enchanted\\evening', N).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("N").asInstanceOf[Number]
      replaceUnderscore(result.toString) shouldBe "17"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("atom_length('', N).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("N").asInstanceOf[Number]
      replaceUnderscore(result.toString) shouldBe "0"
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("atom_length(Atom, 4).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Instantiation error in argument 1 of atom_length(Atom_e0,4)"
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("atom_length(1.23, 4).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 1 of atom_length(1.23,4)"
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("atom_length(atom, '4').")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of all_solutions_predicates_guard(Template_e1,4,Instances_e1)"
    }
  }

  describe("atom_concat/3") {
    it("should pass negative test #1") {
      val solution = prolog.solve("atom_concat('hello', 'world', 'small world').")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("atom_concat('small', T, 'smallworld').")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("T").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "world"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("atom_concat(T, 'world', 'smallworld').")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("T").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "small"
    }

    it("should pass variable test #3") {
      var solution = prolog.solve("atom_concat(T1, T2, 'hello').")
      solution.isSuccess shouldBe true

      var result1 = solution.getVarValue("T1").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "''"

      var result2 = solution.getVarValue("T2").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "hello"


      solution = prolog.solveNext()
      result1 = solution.getVarValue("T1").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "h"

      result2 = solution.getVarValue("T2").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "ello"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("T1").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "he"

      result2 = solution.getVarValue("T2").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "llo"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("T1").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "hel"

      result2 = solution.getVarValue("T2").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "lo"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("T1").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "hell"

      result2 = solution.getVarValue("T2").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "o"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("T1").asInstanceOf[Struct]
      replaceUnderscore(result1.toString) shouldBe "hello"

      result2 = solution.getVarValue("T2").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "''"

    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("atom_concat(small, V2, V4).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of atom_chars(S_e6,SL_e6)"
    }
  }

  describe("sub_atom/5") {
    it("should pass variable test #1") {
      val solution = prolog.solve("sub_atom(abracadabra, 0, 5, _, S2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "abrac"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("sub_atom(abracadabra, _, 5, 0, S2).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "dabra"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("sub_atom(abracadabra, 3, L, 3, S2).")
      solution.isSuccess shouldBe true

      val result1 = solution.getVarValue("L").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "5"

      val result2 = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result2.toString) shouldBe "acada"
    }

    it("should pass variable test #4") {
      var solution = prolog.solve("sub_atom(abracadabra, B, 2, A, ab).")
      solution.isSuccess shouldBe true

      var result1 = solution.getVarValue("B").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "0"

      var result2 = solution.getVarValue("A").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "9"


      solution = prolog.solveNext()
      result1 = solution.getVarValue("B").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "7"

      result2 = solution.getVarValue("A").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "2"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("sub_atom('Banana', 3, 2, _, S2).")
      solution.isSuccess shouldBe true

      val xResult = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(xResult.toString) shouldBe "an"
    }

    it("should pass variable test #6") {
      var solution = prolog.solve("sub_atom('charity', _, 3, _, S2).")
      solution.isSuccess shouldBe true

      var result = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "cha"

      solution = prolog.solveNext()
      result = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "har"

      solution = prolog.solveNext()
      result = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "ari"

      solution = prolog.solveNext()
      result = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "rit"

      solution = prolog.solveNext()
      result = solution.getVarValue("S2").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "ity"
    }

    it("should pass variable test #7") {
      var solution = prolog.solve("sub_atom('ab', Start, Length, _, Sub_atom).")
      solution.isSuccess shouldBe true

      var result1 = solution.getVarValue("Start").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "0"

      var result2 = solution.getVarValue("Length").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "0"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("Start").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "0"

      result2 = solution.getVarValue("Length").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "1"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("Start").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "0"

      result2 = solution.getVarValue("Length").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "2"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("Start").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "1"

      result2 = solution.getVarValue("Length").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "0"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("Start").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "1"

      result2 = solution.getVarValue("Length").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "1"

      solution = prolog.solveNext()
      result1 = solution.getVarValue("Start").asInstanceOf[Number]
      replaceUnderscore(result1.toString) shouldBe "2"

      result2 = solution.getVarValue("Length").asInstanceOf[Number]
      replaceUnderscore(result2.toString) shouldBe "0"
    }
  }

  describe("atom_chars/2") {
    it("should pass negative test #1") {
      val solution = prolog.solve("atom_chars('soap', ['s', 'o', 'p']).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("atom_chars('', L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[]"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("atom_chars([], L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "['[',']']"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("atom_chars('''', L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[''']"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("atom_chars('ant', L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[a,n,t]"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("atom_chars(Str, ['s', 'o', 'p']).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Str").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "sop"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("atom_chars('North', ['N' | X]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[o,r,t,h]"
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("atom_chars(X, Y).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of atom_chars(X_e0,Y_e0)"
    }
  }

  describe("atom_codes/2") {
    it("should pass negative test #1") {
      val solution = prolog.solve("atom_codes('soap', [0's, 0'o, 0'p]).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("atom_codes('', L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[]"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("atom_codes([], L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[91,93]"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("atom_codes('''', L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[39]"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("atom_codes('ant', L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[97,110,116]"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("atom_codes(Str, [0's, 0'o, 0'p]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Str").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "sop"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("atom_codes('North', [0'N | X]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[111,114,116,104]"
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("atom_codes(X, Y).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of atom_codes(X_e0,Y_e0)"
    }
  }


  describe("char_code/2") {
    it("should pass negative test #1") {
      val solution = prolog.solve("char_code('b', 84).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("char_code('a', Code).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Code").asInstanceOf[Number]
      replaceUnderscore(result.toString) shouldBe "97"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("char_code(Str, 99).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Str").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "c"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("char_code(Str, 0'c).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Str").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "c"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("char_code(Str, 163).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("Str").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "'£'"
    }

    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("char_code('ab', Int).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 1 of char_code(ab,Int_e0)"
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("char_code(C, I).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 1 of char_code(C_e0,I_e0)"
    }
  }

  describe("number_chars/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("number_chars(33, ['3', '3']).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("number_chars(3.3, ['3', '.', '3']). ")
      solution.isSuccess shouldBe true
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("number_chars(33, L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "['3','3']"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("number_chars(33.0, L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "['3','3','.','0']"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("number_chars(X, ['3', '.', '3', 'E', '+', '0']).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("X")
      replaceUnderscore(result.toString) shouldBe "3.3"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("number_chars(A, ['-', '2', '5']).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      replaceUnderscore(result.toString) shouldBe "-25"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("number_chars(A, ['\\t','\\n', ' ', '3']).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      result.toString shouldBe "3"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("number_chars(A, ['0', x, f]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      result.toString shouldBe "15"
    }

    it("should pass variable test #7") {
      val solution = prolog.solve("number_chars(A, ['0', '''''', a]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      result.toString shouldBe "97"
    }

    it("should pass variable test #8") {
      val solution = prolog.solve("number_chars(A, ['4', '.', '2']).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      replaceUnderscore(result.toString) shouldBe "4.2"
    }

    it("should pass variable test #9") {
      val solution = prolog.solve("number_chars(A, ['4', '2', '.', '0', 'e', '-', '1']).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      replaceUnderscore(result.toString) shouldBe "4.2"
    }


    it("should pass exception test #1") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("number_chars(A, ['\\t','3', 'g',' ']).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Domain error in argument 2 of num_atom(Number_e8,'\\t3g ')"
    }

    it("should pass exception test #2") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("number_chars(A, ['\\t','3', 'g','4']).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of all_solutions_predicates_guard(Template_e1,4,Instances_e1)"
    }

    it("should pass exception test #3") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("number_chars(A, ['\\t','3', '4','g', '5']).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of all_solutions_predicates_guard(Template_e1,4,Instances_e1)"
    }

    it("should pass exception test #4") {
      val ex = getExceptionListener
      prolog.addExceptionListener(ex)
      val solution = prolog.solve("number_chars(A, ['\\t', '3', '5', '-', '6']).")
      ex.exFound shouldBe true
      ex.exMsg shouldBe "Type error in argument 2 of all_solutions_predicates_guard(Template_e1,4,Instances_e1)"
    }
  }

  describe("number_codes/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("number_codes(33, [0'3, 0'3]).")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("number_codes(3.3, [0'3, 0'., 0'3]).")
      solution.isSuccess shouldBe true
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("number_codes(33, L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[51,51]"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("number_codes(33.0, L).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "[51,51,46,48]"
    }

    it("should pass variable test #3") {
      val solution = prolog.solve("number_codes(A, [0'-, 0'2, 0'5]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      replaceUnderscore(result.toString) shouldBe "-25"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("number_codes(A, [0' , 0'3]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      result.toString shouldBe "3"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("number_codes(A, [0'0, 0'x, 0'f]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      result.toString shouldBe "15"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("number_codes(A, [0'4, 0'., 0'2]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      replaceUnderscore(result.toString) shouldBe "4.2"
    }

    it("should pass variable test #7") {
      val solution = prolog.solve("number_codes(A, [0'4, 0'2, 0'., 0'0, 0'e, 0'-, 0'1]).")
      solution.isSuccess shouldBe true

      val result = solution.getVarValue("A")
      replaceUnderscore(result.toString) shouldBe "4.2"
    }
  }
}
