package com.szadowsz.gospel.core.db.libs

import java.util

import com.szadowsz.gospel.core.data.{Int, Struct, Var}
import com.szadowsz.gospel.core.db.JLibrary
import com.szadowsz.gospel.core.db.primitives.JPrimitive
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ISOLibrarySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "ISO Library"

 it should "throw an instantiation error when atom_length (X, Y)  is called" in {
    val goal = "catch(atom_length(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("atom_length", new Var("X"), new Var("Y"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when atom_length(1, Y) is called" in {
    val goal = "catch(atom_length(1, Y), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("atom_length", new Int(1), new Var("Y"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

 it should "throw a type error when atom_chars(1, X) is called" in {
    val goal = "catch(atom_chars(1, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("atom_chars", new Int(1), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

   it should "throw a type error when atom_chars(X, a) is called" in {
    val goal = "catch(atom_chars(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("atom_chars", new Var("X"), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when char_code(ab, X) is called" in {
    val goal = "catch(char_code(ab, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("char_code", new Struct("ab"), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("character")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("ab")
  }

  it should "throw a type error when char_code(X, a) is called" in {
    val goal = "catch(char_code(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("char_code", new Var("X"), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("integer")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when sub_atom(1, B, C, D, E) is called" in {
    val goal = "catch(sub_atom(1, B, C, D, E), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("sub_atom_guard", new Int(1), new Var("B"), new Var("C"), new Var("D"), new Var("E"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }
}