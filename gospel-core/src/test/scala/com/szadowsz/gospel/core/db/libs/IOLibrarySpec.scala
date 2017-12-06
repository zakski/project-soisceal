package com.szadowsz.gospel.core.db.libs

import java.io.{File, PrintWriter}
import java.util
import java.util.{List, Map}

import com.szadowsz.gospel.core.data.{Int, Struct, Term, Var}
import com.szadowsz.gospel.core.db.Library
import com.szadowsz.gospel.core.db.primitives.PrimitiveInfo
import com.szadowsz.gospel.core.listener.TestOutputListener
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Solution, Theory}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IOLibrarySpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "IO Library"

  it should "only have predicate primitives" in {
    val library: Library = new IOLibrary
    val primitives: util.Map[Integer, util.List[PrimitiveInfo]] = library.getPrimitives

    primitives.size shouldBe 3
    primitives.get(PrimitiveInfo.DIRECTIVE).size shouldBe 0
    primitives.get(PrimitiveInfo.PREDICATE).size should be > 0
    primitives.get(PrimitiveInfo.FUNCTOR).size shouldBe 0
  }

  it should "output tabs successfully" in {
    val l = new TestOutputListener()
    prolog.addOutputListener(l)
    prolog.solve("tab(5).")
    l.output should be ("     ")
  }

  it should "throw an instantiation error when see(X) is called" in {
    val goal = "catch(see(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("see", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when see(1) is called" in {
    val goal = "catch(see(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("see", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

   it should "throw a domain error if the input stream can not be accessed" in {
    val goal = "catch(see(a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("see", new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validDomain = info.getTerm("ValidDomain").asInstanceOf[Struct]
    validDomain shouldBe new Struct("stream")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw an instantiation error when tell(X) is called" in {
    val goal = "catch(tell(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("tell", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when tell(1) is called" in {
    val goal = "catch(tell(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("tell", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an instantiation error when put(X) is called" in {
    val goal = "catch(put(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("put", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when put(1) is called" in {
    val goal = "catch(put(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("put", new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("character")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw a type error when put(aa) is called" in {
    val goal = "catch(put(aa), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("put", new Struct("aa"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("character")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("aa")
  }

  it should "throw an instantiation error when tab(X) is called" in {
    val goal = "catch(tab(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("tab", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when tab(a) is called" in {
    val goal = "catch(tab(a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("tab", new Struct("a"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("integer")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("a")
  }

  it should "throw a syntax error if one occurs when reading from the input stream" in {
    val pw = new PrintWriter("read")
    pw.print("@term.")
    pw.close()
    val goal = "see(read), catch(read(X), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true), seen."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("read", new Var("X"))
    val line = info.getTerm("Line").asInstanceOf[Int]
    line.intValue shouldBe 1
    val position = info.getTerm("Line").asInstanceOf[Int]
    position.intValue shouldBe 1
    val message = info.getTerm("Message").asInstanceOf[Struct]
    message shouldBe new Struct("@term")
    val f = new File("read")
    f.delete
  }

  it should "throw an instantiation error when write(X) is called" in {
    val goal = "catch(write(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("write", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw an instantiation error when print(X) is called" in {
    val goal = "catch(print(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("print", new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw an instantiation error when text_from_file(X, Y) is called" in {
    val goal = "catch(text_from_file(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("text_from_file", new Var("X"), new Var("Y"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when text_from_file(1, Y) is called" in {
    val goal = "catch(text_from_file(1, Y), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("text_from_file", new Int(1), new Var("Y"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an existence error if the file does not exist" in {
    val goal = "catch(text_from_file(text, Y), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("text_from_file", new Struct("text"), new Var("Y"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ObjectType").asInstanceOf[Struct]
    validType shouldBe new Struct("stream")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("text")
    val message = info.getTerm("Message")
    message shouldBe new Struct("File not found.")
  }

  it should "throw an instantiation error when agent_file(X) is called" in {
    val goal = "catch(agent_file(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Var("X"), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when agent_file(1) is called" in {
    val goal = "catch(agent_file(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Int(1), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

   it should "throw an existence error if the agent theory file does not exist" in {
    val goal = "catch(agent_file(text), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Struct("text"), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ObjectType").asInstanceOf[Struct]
    validType shouldBe new Struct("stream")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("text")
    val message = info.getTerm("Message")
    message shouldBe new Struct("File not found.")
  }

  it should "throw an instantiation error when solve_file(X, g) is called" in {
    val goal = "catch(solve_file(X, g), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Var("X"), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when solve_file(1, g) is called" in {
    val goal = "catch(solve_file(1, g), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Int(1), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an existence error if the solve file does not exist" in {
    val goal = "catch(solve_file(text, g), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Struct("text"), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ObjectType").asInstanceOf[Struct]
    validType shouldBe new Struct("stream")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("text")
    val message = info.getTerm("Message")
    message shouldBe new Struct("File not found.")
  }

  it should "throw an instantiation error when solve_file(text, X) is called" in {
    val goal = "catch(solve_file(text, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("solve_file_goal_guard", new Struct("text"), new Var("X"))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
  }

  it should "throw a type error when solve_file(text, 1) is called" in {
    val goal = "catch(solve_file(text, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g shouldBe new Struct("solve_file_goal_guard", new Struct("text"), new Int(1))
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 2
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("callable")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an instantiation error when consult(X) is called" in {
    val goal = "catch(consult(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Var("X"), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
  }

  it should "throw a type error when consult(1) is called" in {
    val goal = "catch(consult(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Int(1), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ValidType").asInstanceOf[Struct]
    validType shouldBe new Struct("atom")
    val culprit = info.getTerm("Culprit").asInstanceOf[Int]
    culprit.intValue shouldBe 1
  }

  it should "throw an existence error if the consult file does not exist" in {
    val goal = "catch(consult(text), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true)."
    val info = prolog.solve(goal)
    info.isSuccess shouldBe true
    val g = info.getTerm("Goal").asInstanceOf[Struct]
    g.`match`(new Struct("text_from_file", new Struct("text"), new Var("Y"))) shouldBe true
    val argNo = info.getTerm("ArgNo").asInstanceOf[Int]
    argNo.intValue shouldBe 1
    val validType = info.getTerm("ObjectType").asInstanceOf[Struct]
    validType shouldBe new Struct("stream")
    val culprit = info.getTerm("Culprit").asInstanceOf[Struct]
    culprit shouldBe new Struct("text")
    val message = info.getTerm("Message")
    message shouldBe new Struct("File not found.")
  }
}