package com.szadowsz.gospel.core.db.libs

import com.szadowsz.gospel.core.data.{Int, Struct, Var}
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Solution}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BasicLibrarySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Basic Library"

  it should "throws an instantiation error when set_theory(X) is called" in {
    val goal = "catch(set_theory(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_theory", new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when set_theory(1) is called" in {
    val goal = "catch(set_theory(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_theory", new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a syntax error when set_theory(a) is called" in {
    val goal = "catch(set_theory(a), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("set_theory", new Struct("a"))
    val line = info.getVar("Line").asInstanceOf[Int]
    line.intValue shouldBe 1
    val position = info.getVar("Line").asInstanceOf[Int]
    position.intValue shouldBe 1
    val message = info.getVar("Message").asInstanceOf[Struct]
    message shouldBe new Struct("The term 'a' is not ended with a period.")
  }


  it should "throws an instantiation error when add_theory(X) is called" in {
    val goal = "catch(add_theory(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("add_theory", new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when add_theory(1) is called" in {
    val goal = "catch(add_theory(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("add_theory", new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a syntax error when add_theory(a) is called" in {
    val goal = "catch(add_theory(a), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("add_theory", new Struct("a"))
    val line = info.getVar("Line").asInstanceOf[Int]
    line.intValue shouldBe 1
    val position = info.getVar("Line").asInstanceOf[Int]
    position.intValue shouldBe 1
    val message = info.getVar("Message").asInstanceOf[Struct]
    message shouldBe new Struct("The term 'a' is not ended with a period.")
  }

  it should "throws an instantiation error when agent(X) is called" in {
    val goal = "catch(agent(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("agent", new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when agent(1) is called" in {
    val goal = "catch(agent(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("agent", new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when agent(X, a) is called" in {
    val goal = "catch(agent(X, a), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("agent", new Var("X"), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when agent(a, X) is called" in {
    val goal = "catch(agent(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("agent", new Struct("a"), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when agent(1, a) is called" in {
    val goal = "catch(agent(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("agent", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when agent(a, 1) is called" in {
    val goal = "catch(agent(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("agent", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("struct")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when '=:='(X, 1) is called" in {
    val goal = "catch('=:='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Var("X"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when '=:='(1, X) is called" in {
    val goal = "catch('=:='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '=:='(a, 1) is called" in {
    val goal = "catch('=:='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when '=:='(1, a) is called" in {
    val goal = "catch('=:='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throws an instantiation error when '=\\='(X, 1) is called" in {
    val goal = "catch('=\\='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Var("X"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when '=\\='(1, X) is called" in {
    val goal = "catch('=\\='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '=\\='(a, 1) is called" in {
    val goal = "catch('=\\='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when '=\\='(1, a) is called" in {
    val goal = "catch('=\\='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throws an instantiation error when '>'(X, 1) is called" in {
    val goal = "catch('>'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_than", new Var("X"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when '>'(1, X) is called" in {
    val goal = "catch('>'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_than", new Int(1), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '>'(a, 1) is called" in {
    val goal = "catch('>'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_than", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when '>'(1, a) is called" in {
    val goal = "catch('>'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_than", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throws an instantiation error when '<'(X, 1) is called" in {
    val goal = "catch('<'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_than", new Var("X"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when '<'(1, X) is called" in {
    val goal = "catch('<'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_than", new Int(1), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '<'(a, 1) is called" in {
    val goal = "catch('<'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_than", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when '<'(1, a) is called" in {
    val goal = "catch('<'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_than", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throws an instantiation error when '>='(X, 1) is called" in {
    val goal = "catch('>='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_or_equal_than", new Var("X"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when '>='(1, X) is called" in {
    val goal = "catch('>='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_or_equal_than", new Int(1), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '>='(a, 1) is called" in {
    val goal = "catch('>='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_or_equal_than", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when '>='(1, a) is called" in {
    val goal = "catch('>='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_or_equal_than", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throws an instantiation error when '=<'(X, 1) is called" in {
    val goal = "catch('=<'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_or_equal_than", new Var("X"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when '=<'(1, X) is called" in {
    val goal = "catch('=<'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_or_equal_than", new Int(1), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when '=<'(a, 1) is called" in {
    val goal = "catch('=<'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_or_equal_than", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when '=<'(1, a) is called" in {
    val goal = "catch('=<'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_or_equal_than", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("evaluable")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a 'zero_divisor' evaluation error when '=:='(1, 1/0) is called" in {
    val goal = "catch('=:='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Struct("/", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=\\='(1, 1/0) is called" in {
    val goal = "catch('=\\='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Struct("/", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '>'(1, 1/0) is called" in {
    val goal = "catch('>'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_than", new Int(1), new Struct("/", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '<'(1, 1/0) is called" in {
    val goal = "catch('<'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_than", new Int(1), new Struct("/", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '>='(1, 1/0) is called" in {
    val goal = "catch('>='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_or_equal_than", new Int(1), new Struct("/", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=<'(1, 1/0) is called" in {
    val goal = "catch('=<'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_or_equal_than", new Int(1), new Struct("/", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=:='(1, 1//0) is called" in {
    val goal = "catch('=:='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Struct("//", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=\\='(1, 1//0) is called" in {
    val goal = "catch('=\\='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Int(1), new Struct("//", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '>'(1, 1//0) is called" in {
    val goal = "catch('>'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_than", new Int(1), new Struct("//", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '<'(1, 1//0) is called" in {
    val goal = "catch('<'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_than", new Int(1), new Struct("//", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '>='(1, 1//0) is called" in {
    val goal = "catch('>='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_or_equal_than", new Int(1), new Struct("//", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=<'(1, 1//0) is called" in {
    val goal = "catch('=<'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_or_equal_than", new Int(1), new Struct("//", new Int(1), new Int(0)))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=:='(1 div 0, 1) is called" in {
    val goal = "catch('=:='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Struct("div", new Int(1), new Int(0)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=\\='(1 div 0, 1) is called" in {
    val goal = "catch('=\\='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_equality", new Struct("div", new Int(1), new Int(0)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '>'(1 div 0, 1) is called" in {
    val goal = "catch('>'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_than", new Struct("div", new Int(1), new Int(0)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '<'(1 div 0, 1) is called" in {
    val goal = "catch('<'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_than", new Struct("div", new Int(1), new Int(0)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '>='(1 div 0, 1) is called" in {
    val goal = "catch('>='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_greater_or_equal_than", new Struct("div", new Int(1), new Int(0)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throw a 'zero_divisor' evaluation error when '=<'(1 div 0, 1) is called" in {
    val goal = "catch('=<'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("expression_less_or_equal_than", new Struct("div", new Int(1), new Int(0)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("Error").asInstanceOf[Struct]
    validType shouldBe new Struct("zero_divisor")
  }

  it should "throws an instantiation error when text_concat(X, a, b) is called" in {
    val goal = "catch(text_concat(X, a, b), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("text_concat", new Var("X"), new Struct("a"), new Struct("b"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when text_concat(a, X, b) is called" in {
    val goal = "catch(text_concat(a, X, b), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("text_concat", new Struct("a"), new Var("X"), new Struct("b"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when text_concat(1, a, b) is called" in {
    val goal = "catch(text_concat(1, a, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("text_concat", new Int(1), new Struct("a"), new Struct("b"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when text_concat(a, 1, b) is called" in {
    val goal = "catch(text_concat(a, 1, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("text_concat", new Struct("a"), new Int(1), new Struct("b"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when num_atom(a, X) is called" in {
    val goal = "catch(num_atom(a, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("num_atom", new Struct("a"), new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("number")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when num_atom(1, 1) is called" in {
    val goal = "catch(num_atom(1, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("num_atom", new Int(1), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a domain  error when num_atom(1, a) is called" in {
    val goal = "catch(num_atom(1, a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("num_atom", new Int(1), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validDomain = info.getVar("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("num_atom")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throws an instantiation error when arg(X, p(1), 1) is called" in {
    val goal = "catch(arg(X, p(1), 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("arg_guard", new Var("X"), new Struct("p", new Int(1)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when arg(1, X, 1) is called" in {
    val goal = "catch(arg(1, X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("arg_guard", new Int(1), new Var("X"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when arg(a, p(1), 1) is called" in {
    val goal = "catch(arg(a, p(1), 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("arg_guard", new Struct("a"), new Struct("p", new Int(1)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("integer")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when arg(1, p, 1) is called" in {
    val goal = "catch(arg(1, p, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("arg_guard", new Int(1), new Struct("p"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("compound")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("p")
  }

  it should "throw a domain  error when arg(0, p(0), 1) is called" in {
    val goal = "catch(arg(0, p(0), 1), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("arg_guard", new Int(0), new Struct("p", new Int(0)), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validDomain = info.getVar("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("greater_than_zero")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 0
  }

  it should "throws an instantiation error when clause(X, true) is called" in {
    val goal = "catch(clause(X, true), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("clause_guard", new Var("X"), new Struct("true"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throws an instantiation error when call(X) is called" in {
    val goal = "catch(call(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("call_guard", new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when call(1) is called" in {
    val goal = "catch(call(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("call_guard", new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("callable")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when findall(a, X, L) is called" in {
    val goal = "catch(findall(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("all_solutions_predicates_guard", new Struct("a"), new Var("X"), new Var("L"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when findall(a, 1, L) is called" in {
    val goal = "catch(findall(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("all_solutions_predicates_guard", new Struct("a"), new Int(1), new Var("L"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("callable")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when setof(a, X, L) is called" in {
    val goal = "catch(setof(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("all_solutions_predicates_guard", new Struct("a"), new Var("X"), new Var("L"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when setof(a, 1, L) is called" in {
    val goal = "catch(setof(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("all_solutions_predicates_guard", new Struct("a"), new Int(1), new Var("L"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("callable")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when bagof(a, X, L) is called" in {
    val goal = "catch(bagof(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("all_solutions_predicates_guard", new Struct("a"), new Var("X"), new Var("L"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when bagof(a, 1, L) is called" in {
    val goal = "catch(bagof(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("all_solutions_predicates_guard", new Struct("a"), new Int(1), new Var("L"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("callable")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when assert(X) is called" in {
    val goal = "catch(assert(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("assertz", new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when assert(1) is called" in {
    val goal = "catch(assert(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("assertz", new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("clause")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when retract(X) is called" in {
    val goal = "catch(retract(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("retract_guard", new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when retract(1) is called" in {
    val goal = "catch(retract(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("retract_guard", new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("clause")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throws an instantiation error when retractall(X) is called" in {
    val goal = "catch(retractall(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("retract_guard", new Var("X"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when retractall(1) is called" in {
    val goal = "catch(retractall(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("retract_guard", new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("clause")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when member(a, 1) is called" in {
    val goal = "catch(member(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("member_guard", new Struct("a"), new Int(1))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getVar("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when reverse(a, []) is called" in {
    val goal = "catch(reverse(a, []), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("reverse_guard", new Struct("a"), new Struct)
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when delete(a, a, []) is called" in {
    val goal = "catch(delete(a, a, []), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("delete_guard", new Struct("a"), new Struct("a"), new Struct)
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a type error when element(1, a, a) is called" in {
    val goal = "catch(element(1, a, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getVar("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("element_guard", new Int(1), new Struct("a"), new Struct("a"))
    val argNo = info.getVar("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getVar("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("list")
    val culprit = info.getVar("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }
}
