package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Solution, Theory}
import com.szadowsz.gospel.core.data.{Float, Int, Struct, Term, Var}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuiltinSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Builtin Library"

  it should "convert Var to Goal successfully" in {
    val t = new Var("T")
    val result = new Struct("call", t)
    BuiltIn.convertTermToGoal(t) should be(result)
    BuiltIn.convertTermToGoal(new Struct("call", t)) should be(result)
  }

  it should "not convert Int to Goal successfully" in {
    val t = new Int(2)
    BuiltIn.convertTermToGoal(t) should be(null)
  }

  it should "convert Struct to Goal successfully" in {
    val t = new Struct("p", new Struct("a"), new Var("B"), new Struct("c"))
    BuiltIn.convertTermToGoal(t) should be(t)
  }

  it should "convert linked Vars to Goal successfully" in {
    val linked = new Var("X")
    linked.setLink(new Struct("!"))
    val arguments: Array[Term] = Array[Term](linked, new Var("Y"))
    val results: Array[Term] = Array[Term](new Struct("!"), new Struct("call", new Var("Y")))

    BuiltIn.convertTermToGoal(new Struct(";", arguments)) shouldBe new Struct(";", results)
    BuiltIn.convertTermToGoal(new Struct(",", arguments)) shouldBe new Struct(",", results)
    BuiltIn.convertTermToGoal(new Struct("->", arguments)) shouldBe new Struct("->", results)
  }


  it should "group conjunctions successfully" in {
    prolog.setTheory(new Theory("g1. g2."))
    var info: Solution = prolog.solve("(g1, g2), (g3, g4).")
    info.isSuccess shouldBe false
    prolog.setTheory(new Theory("g1. g2. g3. g4."))
    info = prolog.solve("(g1, g2), (g3, g4).")
    info.isSuccess shouldBe true
  }

  it should "throw an instantiation error when asserta(X) is called" in {
    val goal = "catch(asserta(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("asserta", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when asserta(1) is called" in {
    val goal = "catch(asserta(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("asserta", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("clause")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an instantiation error when assertz(X) is called" in {
    val goal = "catch(assertz(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("assertz", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when assertz(1) is called" in {
    val goal = "catch(assertz(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("assertz", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("clause")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an instantiation error when '$retract'(X) is called" in {
    val goal = "catch('$retract'(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$retract", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when '$retract'(1) is called" in {
    val goal = "catch('$retract'(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$retract", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("clause")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an instantiation error when abolish(X) is called" in {
    val goal = "catch(abolish(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("abolish", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when abolish(1) is called" in {
    val goal = "catch(abolish(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("abolish", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("predicate_indicator")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when abolish(p(X)) is called" in {
    val goal = "catch(abolish(p(X)), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("abolish", new Struct("p", new Var("X")))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("predicate_indicator")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("p", new Var("X"))
  }

  it should "throw an instantiation error when halt(X) is called" in {
    val goal = "catch(halt(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("halt", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when halt(1.5) is called" in {
    val goal = "catch(halt(1.5), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("halt", new Float(1.5))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("integer")
    val culprit = info.getTerm("Culprit").asInstanceOf[Float]
    culprit.doubleValue shouldBe 1.5
  }

  it should "throw an instantiation error when load_library(X) is called" in {
    val goal = "catch(load_library(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("load_library", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when load_library(1) is called" in {
    val goal = "catch(load_library(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("load_library", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

   it should "throw an existence error a non existant library is loaded" in {
    val goal = "catch(load_library('a'), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("load_library", new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ObjectType").asInstanceOf[Struct]
    validType shouldBe new Struct("class")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
    val message = info.getTerm("Message")
    message shouldBe new Struct("InvalidLibraryException: a at -1:-1")
  }

  it should "throw an instantiation error when unload_library(X) is called" in {
    val goal = "catch(unload_library(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("unload_library", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when unload_library(1) is called" in {
    val goal = "catch(unload_library(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("unload_library", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an existence error a non existant library is unloaded" in {
    val goal = "catch(unload_library('a'), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("unload_library", new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ObjectType").asInstanceOf[Struct]
    validType shouldBe new Struct("class")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
    val message = info.getTerm("Message")
    message shouldBe new Struct("InvalidLibraryException: null at 0:0")
  }

  it should "throw an instantiation error when '$call'(X) is called" in {
    val goal = "catch('$call'(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$call", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when '$call'(1) is called" in {
    val goal = "catch('$call'(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$call", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("callable")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an instantiation error when is(X, Y) is called" in {
    val goal = "catch(is(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("is", new Var("X"), new Var("Y"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when is(X, a) is called" in {
    val goal = "catch(is(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("is", new Var("X"), new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a 'zero_divsor' evaluation error when is(X, 1/0) is called" in {
    val goal = "catch(is(X, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("is", new Var("X"), new Struct("/", new Int(1), new Int(0)))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val error = info.getTerm("Error").asInstanceOf[Struct]
    error shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divsor' evaluation error when is(X, 1//0) is called" in {
    val goal = "catch(is(X, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("is", new Var("X"), new Struct("//", new Int(1), new Int(0)))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val error = info.getTerm("Error").asInstanceOf[Struct]
    error shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divsor' evaluation error when is(X, 1 div 0) is called" in {
    val goal = "catch(is(X, 1 div 0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("is", new Var("X"), new Struct("div", new Int(1), new Int(0)))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val error = info.getTerm("Error").asInstanceOf[Struct]
    error shouldBe new Struct("zero_divisor")
  }

  it should "throw an instantiation error when '$tolist'(X, List) is called" in {
    val goal = "catch('$tolist'(X, List), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$tolist", new Var("X"), new Var("List"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when '$tolist'(1, List) is called" in {
    val goal = "catch('$tolist'(1, List), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$tolist", new Int(1), new Var("List"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("struct")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an instantiation error when '$fromlist'(Struct, X) is called" in {
    val goal = "catch('$fromlist'(Struct, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$fromlist", new Var("Struct"), new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '$fromlist'(Struct, a) is called" in {
    val goal = "catch('$fromlist'(Struct, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$fromlist", new Var("Struct"), new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw an instantiation error when '$append'(a, X) is called" in {
    val goal = "catch('$append'(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$append", new Struct("a"), new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '$append'(a, b) is called" in {
    val goal = "catch('$append'(a, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$append", new Struct("a"), new Struct("b"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("b")
  }

  it should "throw an instantiation error when '$find'(X, []) is called" in {
    val goal = "catch('$find'(X, []), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$find", new Var("X"), new Struct)
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when '$find'(p(X), a) is called" in {
    val goal = "catch('$find'(p(X), a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$find", new Struct("p", new Var("X")), new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw an instantiation error when set_prolog_flag(X, 1) is called" in {
    val goal = "catch(set_prolog_flag(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_prolog_flag", new Var("X"), new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw an instantiation error when set_prolog_flag(a, X) is called" in {
    val goal = "catch(set_prolog_flag(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_prolog_flag", new Struct("a"), new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when set_prolog_flag(1, 1) is called" in {
    val goal = "catch(set_prolog_flag(1, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_prolog_flag", new Int(1), new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("struct")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when set_prolog_flag(a, p(X)) is called" in {
    val goal = "catch(set_prolog_flag(a, p(X)), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_prolog_flag", new Struct("a"), new Struct("p", new Var("X")))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("ground")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("p", new Var("X"))
  }

  it should "throw a domain error when set_prolog_flag is called if the Flag is not defined in the engine" in {
    val goal = "catch(set_prolog_flag(a, 1), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_prolog_flag", new Struct("a"), new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validDomain = info.getTerm("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("prolog_flag")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }
  it should "throw a domain error when set_prolog_flag is called if the Flag value is not defined as valid in the engine" in {
    val goal = "catch(set_prolog_flag(bounded, a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_prolog_flag", new Struct("bounded"), new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validDomain = info.getTerm("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("flag_value")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a permission error when set_prolog_flag(bounded, false) is called" in {
    val goal = "catch(set_prolog_flag(bounded, false), error(permission_error(Operation, ObjectType, Culprit), permission_error(Goal, Operation, ObjectType, Culprit, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_prolog_flag", new Struct("bounded"), new Struct("false"))
    val operation = info.getTerm("Operation").asInstanceOf[Struct]
    operation shouldBe new Struct("modify")
    val objectType = info.getTerm("ObjectType").asInstanceOf[Struct]
    objectType shouldBe new Struct("flag")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("bounded")
    val message = info.getTerm("Message")
    message shouldBe new Int(0)
  }

  it should "throw an instantiation error when get_prolog_flag(X, Value) is called" in {
    val goal = "catch(get_prolog_flag(X, Value), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("get_prolog_flag", new Var("X"), new Var("Value"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when get_prolog_flag(1, Value) is called" in {
    val goal = "catch(get_prolog_flag(1, Value), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("get_prolog_flag", new Int(1), new Var("Value"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("struct")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a domain error when get_prolog_flag is called if the Flag is not defined in the engine" in {
    val goal = "catch(get_prolog_flag(a, Value), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("get_prolog_flag", new Struct("a"), new Var("Value"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validDomain = info.getTerm("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("prolog_flag")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw an instantiation error when '$op'(Priority, yfx, '+') is called" in {
    val goal = "catch('$op'(Priority, yfx, '+'), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Var("Priority"), new Struct("yfx"), new Struct("+"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw an instantiation error when '$op'(600, Specifier, '+') is called" in {
    val goal = "catch('$op'(600, Specifier, '+'), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Int(600), new Var("Specifier"), new Struct("+"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw an instantiation error when '$op'(600, yfx, Operator) is called" in {
    val goal = "catch('$op'(600, yfx, Operator), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Int(600), new Struct("yfx"), new Var("Operator"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 3
  }

  it should "throw a type error when '$op'(a, yfx, '+') is called" in {
    val goal = "catch('$op'(a, yfx, '+'), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Struct("a"), new Struct("yfx"), new Struct("+"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("integer")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when '$op'(600, 1, '+') is called" in {
    val goal = "catch('$op'(600, 1, '+'), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Int(600), new Int(1), new Struct("+"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when '$op'(600, yfx, 1) is called" in {
    val goal = "catch('$op'(600, yfx, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Int(600), new Struct("yfx"), new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 3
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom_or_atom_list")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a domain error when '$op'(1300, yfx, '+') is called" in {
    val goal = "catch('$op'(1300, yfx, '+'), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Int(1300), new Struct("yfx"), new Struct("+"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validDomain = info.getTerm("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("operator_priority")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1300
  }

  it should "throw a domain error when '$op'(600, a, '+') is called" in {
    val goal = "catch('$op'(600, a, '+'), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("$op", new Int(600), new Struct("a"), new Struct("+"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validDomain = info.getTerm("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("operator_specifier")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }
}