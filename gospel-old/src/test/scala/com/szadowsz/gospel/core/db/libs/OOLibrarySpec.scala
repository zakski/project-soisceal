package com.szadowsz.gospel.core.db.libs

import java.io.File
import java.util
import java.util.{List, Map}

import com.szadowsz.gospel.core.data.{Float, Int, Number, Struct, Term, Var}
import com.szadowsz.gospel.core.db.{JLibrary, Library}
import com.szadowsz.gospel.core._
import com.szadowsz.gospel.core.db.primitives.{JPrimitive, PrimitiveInfo}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class OOLibrarySpec extends FunSpec with BaseEngineSpec {
  private var paths: String = _

  override protected def init(): PrologEngine = new PrologEngine()

  /**
    * @param valid : used to change a valid/invalid array of paths
    */
  private def setPath(valid: Boolean): Unit = {
    val file: File = new File(".")
    // Array paths contains a valid path
    if (valid) {
      paths = "'" + file.getCanonicalPath + "'," + "'" + file.getCanonicalPath + File.separator + "test" + File.separator + "unit" + File.separator + "TestURLClassLoader.jar'"
      paths += "," + "'" + file.getCanonicalPath + File.separator + "test" + File.separator + "unit" + File.separator + "TestInterfaces.jar'"
    }
    else { // Array paths does not contain a valid path
      paths = "'" + file.getCanonicalPath + "'"
    }
  }

  describe("Java library basics") {

    it("should only have predicate primitives") {
      val library: Library = new OOLibrary
      val primitives: util.Map[Integer, util.List[PrimitiveInfo]] = library.getPrimitives

      primitives.size shouldBe 3
      primitives.get(PrimitiveInfo.DIRECTIVE).size shouldBe 0
      primitives.get(PrimitiveInfo.PREDICATE).size should be > 0
      primitives.get(PrimitiveInfo.FUNCTOR).size shouldBe 0
    }

    it("should register and unregister Anonymous Objects Successfully") {
      val lib = prolog.getLibrary("com.szadowsz.gospel.core.db.libs.OOLibrary").asInstanceOf[OOLibrary]

      val theory: String = "demo(X) :- X <- update. \n"
      prolog.setTheory(new Theory(theory))

      // check registering behaviour
      val counter: TestCounter = new TestCounter
      val t: Struct = lib.register(counter)
      prolog.solve(new Struct("demo", t))
      counter.getValue shouldBe 1

      // check unregistering behaviour
      lib.unregister(t)
      val goal: Solution = prolog.solve(new Struct("demo", t))
      goal.isSuccess shouldBe false
    }

    it("should retrieve Dynamic Objects Successfully") {
      val lib = prolog.getLibrary("com.szadowsz.gospel.core.db.libs.OOLibrary").asInstanceOf[OOLibrary]

      val theory = "demo(C) :- \n" + "java_object('com.szadowsz.gospel.core.TestCounter', [], C), \n" + "C <- update, \n" + "C <- update. \n"
      prolog.setTheory(new Theory(theory))

      val info = prolog.solve("demo(Obj).")
      val id = info.getVar("Obj").asInstanceOf[Struct]
      val counter = lib.getRegisteredDynamicObject(id).asInstanceOf[TestCounter]
      counter.getValue shouldBe 2
    }

    it("should pass Java Object test #1 Successfully") {
      setPath(true)
      var theory = "demo(C) :- \n" + "set_classpath([" + paths + "]), \n" + "java_object('Counter', [], Obj), \n" + "Obj <- inc, \n" + "Obj <- inc, \n" + "Obj <- getValue returns C."
      prolog.setTheory(new Theory(theory))
      var info = prolog.solve("demo(Value).")
      info.isSuccess shouldBe true
      val result = info.getVar("Value").asInstanceOf[Number]
      result.intValue shouldBe 2

      // Testing URLClassLoader with java.lang.String class
      theory = "demo_string(S) :- \n" + "java_object('java.lang.String', ['MyString'], Obj_str), \n" + "Obj_str <- toString returns S."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("demo_string(StringValue).")
      info.isSuccess shouldBe true
      val result2 = info.getVar("StringValue").toString.replace("'", "")
      result2 shouldBe "MyString"
    }

    it("should pass Java Object test #2 Successfully") {
      setPath(true)
      val theory = "demo_hierarchy(Gear) :- \n" + "set_classpath([" + paths + "]), \n" + "java_object('Bicycle', [3, 4, 5], MyBicycle), \n" + "java_object('MountainBike', [5, 6, 7, 8], MyMountainBike), \n" + "MyMountainBike <- getGear returns Gear."
      prolog.setTheory(new Theory(theory))
      val info = prolog.solve("demo_hierarchy(Res).")
      info.isHalted shouldBe false
      val result = info.getVar("Res").asInstanceOf[Number]
      result.intValue shouldBe 8
    }

    it("should fail to load a Java Object with an invalid path") {
      setPath(false)
      val theory = "demo(Res) :- \n" + "set_classpath([" + paths + "]), \n" + "java_object('Counter', [], Obj_inc), \n" + "Obj_inc <- inc, \n" + "Obj_inc <- inc, \n" + "Obj_inc <- getValue returns Res."
      prolog.setTheory(new Theory(theory))
      val info = prolog.solve("demo(Value).")
      info.isHalted shouldBe true
    }

    it("should pass java_call/3 test Successfully") {
      setPath(true)
      var theory = "demo(Value) :- set_classpath([" + paths + "]), class('TestStaticClass') <- echo('Message') returns Value."
      prolog.setTheory(new Theory(theory))
      var info = prolog.solve("demo(StringValue).")
      info.isSuccess shouldBe true
      val result = info.getVar("StringValue").toString.replace("'", "")
      result shouldBe "Message"
      //Testing get/set static Field
      setPath(true)
      theory = "demo_2(Value) :- set_classpath([" + paths + "]), class('TestStaticClass').'id' <- get(Value)."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("demo_2(Res).")
      info.isSuccess shouldBe true
      info.getVar("Res").toString.toInt shouldBe 0
      theory = "demo_2(Value, NewValue) :- set_classpath([" + paths + "]), class('TestStaticClass').'id' <- set(Value), \n" + "class('TestStaticClass').'id' <- get(NewValue)."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("demo_2(5, Val).")
      info.isSuccess shouldBe true
      info.getVar("Val").toString.toInt shouldBe 5
    }

    it("should pass java_call/4 test Successfully") {
      setPath(false)
      val theory = "demo(Value) :- set_classpath([" + paths + "]), class('TestStaticClass') <- echo('Message') returns Value."
      prolog.setTheory(new Theory(theory))
      val info = prolog.solve("demo(StringValue).")
      info.isHalted shouldBe true
    }

    it("should pass java_array test Successfully") {
      setPath(true)
      var theory = "demo(Size) :- set_classpath([" + paths + "]), java_object('Counter', [], MyCounter), \n" + "java_object('Counter[]', [10], ArrayCounters), \n" + "java_array_length(ArrayCounters, Size)."
      prolog.setTheory(new Theory(theory))
      var info = prolog.solve("demo(Value).")
      info.isSuccess shouldBe true
      val resultInt: Number = info.getVar("Value").asInstanceOf[Number]
      resultInt.intValue shouldBe 10
      //Testing java_array_set and java_array_get
      setPath(true)
      theory = "demo(Res) :- set_classpath([" + paths + "]), java_object('Counter', [], MyCounter), \n" + "java_object('Counter[]', [10], ArrayCounters), \n" + "MyCounter <- inc, \n" + "java_array_set(ArrayCounters, 0, MyCounter), \n" + "java_array_get(ArrayCounters, 0, C), \n" + "C <- getValue returns Res."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("demo(Value).")
      info.isSuccess shouldBe true
      val resultInt2: Number = info.getVar("Value").asInstanceOf[Number]
      resultInt2.intValue shouldBe 1
    }

    it("should pass set_classpath test Successfully") {
      setPath(true)
      val theory = "demo(Size) :- set_classpath([" + paths + "]), \n " + "java_object('Counter', [], MyCounter), \n" + "java_object('Counter[]', [10], ArrayCounters), \n" + "java_array_length(ArrayCounters, Size)."
      prolog.setTheory(new Theory(theory))
      val info = prolog.solve("demo(Value).")
      info.isSuccess shouldBe true
      val resultInt: Number = info.getVar("Value").asInstanceOf[Number]
      resultInt.intValue shouldBe 10
    }


    it("should pass get_classpath test Successfully") {
      var theory = "demo(P) :- get_classpath(P)."
      prolog.setTheory(new Theory(theory))
      var info = prolog.solve("demo(Value).")
      info.isSuccess shouldBe true
      info.getVar("Value").isList shouldBe true
      info.getVar("Value").toString shouldBe "[]"
      setPath(true)
      theory = "demo(P) :- set_classpath([" + paths + "]), get_classpath(P)."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("demo(Value).")
      info.isSuccess shouldBe true
      info.getVar("Value").isList shouldBe true
      info.getVar("Value").toString shouldBe "[" + paths + "]"
    }

    it("should pass register/1 test Successfully") {
      setPath(true)
      var theory = "demo(Obj) :- \n" + "set_classpath([" + paths + "]), \n" + "java_object('Counter', [], Obj), \n" + "Obj <- inc, \n" + "Obj <- inc, \n" + "register(Obj)."
      prolog.setTheory(new Theory(theory))
      var info = prolog.solve("demo(R).")
      info.isSuccess shouldBe true

      theory = "demo2(Obj, Val) :- \n" + "Obj <- inc, \n" + "Obj <- getValue returns Val."
      prolog.addTheory(new Theory(theory))
      val obj: String = info.getVar("R").toString
      val info2: Solution = prolog.solve("demo2(" + obj + ", V).")
      info2.isSuccess shouldBe true
      info2.getVar("V").toString.toInt shouldBe 3

      // Test invalid object_id registration
      theory = "demo(Obj1) :- register(Obj1)."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("demo(Res).")
      info.isHalted shouldBe true
    }


    it("should pass unregister/1 test Successfully") {
      var theory = "demo(Obj1) :- unregister(Obj1)."
      prolog.setTheory(new Theory(theory))
      var info = prolog.solve("demo(Res).")
      info.isHalted shouldBe true
      setPath(true)
      theory = "demo(Obj) :- \n" + "set_classpath([" + paths + "]), \n" + "java_object('Counter', [], Obj), \n" + "Obj <- inc, \n" + "Obj <- inc, \n" + "register(Obj), unregister(Obj)."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("demo(Res).")
      info.isSuccess shouldBe true
      val lib: OOLibrary = prolog.getLibrary("com.szadowsz.gospel.core.db.libs.OOLibrary").asInstanceOf[OOLibrary]
      val id: Struct = info.getVar("Res").asInstanceOf[Struct]
      val obj = lib.getRegisteredObject(id)
      obj shouldBe null
    }

    it("should pass java_catch test Successfully") {
      setPath(true)
      val theory = "goal :- set_classpath([" + paths + "]), java_object('TestStaticClass', [], Obj), Obj <- testMyException. \n" + "demo(StackTrace) :- java_catch(goal, [('java.lang.IllegalArgumentException'( \n" + "Cause, Msg, StackTrace),write(Msg))], \n" + "true)."
      prolog.setTheory(new Theory(theory))
      val info = prolog.solve("demo(S).")
      info.isSuccess shouldBe true
    }

    it("should pass java interface test Successfully") {
      setPath(true)
      var theory = "goal1 :- set_classpath([" + paths + "])," + "java_object('Pippo', [], Obj), class('Pluto') <- method(Obj)."
      prolog.setTheory(new Theory(theory))
      var info = prolog.solve("goal1.")
      info.isSuccess shouldBe true
      theory = "goal2 :- set_classpath([" + paths + "])," + "java_object('Pippo', [], Obj), class('Pluto') <- method2(Obj)."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("goal2.")
      info.isSuccess shouldBe true
      theory = "goal3 :- java_object('Pippo', [], Obj), set_classpath([" + paths + "]), class('Pluto') <- method(Obj)."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("goal3.")
      info.isSuccess shouldBe true
      theory = "goal4 :- set_classpath([" + paths + "]), " + "java_object('IPippo[]', [5], Array), " + "java_object('Pippo', [], Obj), " + "java_array_set(Array, 0, Obj)," + "java_array_get(Array, 0, Obj2)," + "Obj2 <- met."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("goal4.")
      info.isSuccess shouldBe true
      theory = "goal5 :- set_classpath([" + paths + "])," + "java_object('Pippo', [], Obj)," + "class('Pluto') <- method(Obj as 'IPippo')."
      prolog.setTheory(new Theory(theory))
      info = prolog.solve("goal5.")
      info.isSuccess shouldBe true
    }
  }

  describe("java_object/3") {
    it("should pass simple test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], zero), java_object('java.lang.Integer', [0], expected), zero <- equals(expected) " +
        "returns true.")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("java_object('non.existant.Class', [], _).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("java_object('java.lang.Integer', [], _).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], Z), java_object('java.lang.Integer', [1], Z).")
      solution.isSuccess shouldBe false
    }
  }

  describe("<-/2 and returns/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- clear returns X. \t")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #2") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- size.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #3") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- isEmpty returns true.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #4") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S), S <- toUpperCase returns 'HELLO'.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #5") {
      val solution = prolog.solve("class('java.lang.System') <- gc returns X.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #6") {
      val solution = prolog.solve("class('java.lang.Integer') <- parseInt('15') returns 15.")
      solution.isSuccess shouldBe true
    }

    it("should pass simple test #7") {
      val solution = prolog.solve("class('java.lang.System') <- currentTimeMillis.")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), L <- clear(10).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("java_object('java.lang.Object', [], Obj), Obj <- nonExistantMethod.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], Z), Z <- compareTo(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], Z), Z <- compareTo('ciao').")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("class('java.lang.Integer') <- parseInt(10) returns N.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #6") {
      val solution = prolog.solve("class('java.lang.Integer') <- parseInt(X) returns N.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #7") {
      val solution = prolog.solve("class('java.lang.System') <- currentTimeMillis(10).")
      solution.isSuccess shouldBe false
    }
    it("should pass negative test #8") {
      val solution = prolog.solve("class('non.existant.Class') <- nonExistantMethod.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #9") {
      val solution = prolog.solve("class('java.lang.Integer') <- nonExistantMethod.")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer', [5], N), N <- intValue returns V.")
      solution.isSuccess shouldBe true

      val result = solution.getVar("V")
      replaceUnderscore(result.toString) shouldBe "5"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], X), X <- toArray returns A, atom_chars(A, L).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("L").asInstanceOf[Struct]
      replaceUnderscore(result.toString) shouldBe "['$', 'o', 'b', 'j', '_', '2']"
    }
  }

  describe("as and '.'") {
    it("should pass simple test #1") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S1), java_object('java.lang.String', ['world'], S2), S2 <- compareTo(S1 as 'java.lang.Object') returns X, X > 0.")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #2") {
      val solution = prolog.solve("class('java.lang.Integer').'MAX_VALUE' <- get(V), V > 0.")
      solution.isSuccess shouldBe true
    }
    it("should pass simple test #3") {
      val solution = prolog.solve("java_object('java.awt.GridBagConstraints', [], C), java_object('java.awt.Insets', [1,1,1,1], I1), C.insets <- set(I1), C.insets <- get(I2), I1 == I2.")
      solution.isSuccess shouldBe true
    }
    it("should pass negative test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer', [5], I), class('java.lang.Integer') <- toString(I as int) returns '5'.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S1), java_object('java.lang.String', ['world'], S2), S2 <- compareTo(S1 as 'non.existant.Class') returns X.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("java_object('java.util.ArrayList', [], L), java_object('java.lang.String', ['hello'], S), S <- compareToIgnoreCase(L as 'java.util.List') returns X.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("java_object('java.lang.String', ['hello'], S), java_object('java.lang.Integer', [2], I), S <- indexOf(I as 'java.util.List') returns N.")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("class('java.lang.Integer').MAX_VALUE <- get(V).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #6") {
      val solution = prolog.solve("class('java.lang.Integer').'NON_EXISTANT_FIELD' <- get(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #7") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.nonExistantField <- get(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #8") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.nonExistantField <- set(0).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #9") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.y <- set(X).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.x <- get(X).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Int]
      result.toString shouldBe "0"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("java_object('java.awt.Point', [], P), P.y <- set(5), P.y <- get(Y).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("Y").asInstanceOf[Int]
      result.toString shouldBe "5"
    }
  }

  describe("java_array_set/3, java_array_get/3 and java_array_length/2") {
    it("should pass simple test #1") {
      val solution = prolog.solve("java_object('java.lang.Object[]', [3], A), java_object('java.lang.Object', [], Obj), java_array_set(A, 2, Obj), java_array_get(A, 2, X), X == Obj.")
      solution.isSuccess shouldBe true
    }

    it("should pass negative test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_array_get(A, 4, Obj).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #2") {
      val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_get_boolean(XP, 2, V).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #3") {
      val solution = prolog.solve("java_object('java.lang.String[]', [5], A), java_array_set(A, 2, X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #4") {
      val solution = prolog.solve("java_object('java.lang.Integer[]', [5], A), java_array_set(A, 2, zero).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #5") {
      val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_set_boolean(XP, 3, 2).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #6") {
      val solution = prolog.solve("java_object('java.lang.Object', [], Obj), java_array_length(Obj, Size).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #7") {
      val solution = prolog.solve("java_object('java.lang.Object', [], Obj), java_array_get(Obj, 0, X).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #8") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], I), java_array_set(I, 0, 5).")
      solution.isSuccess shouldBe false
    }

    it("should pass negative test #9") {
      val solution = prolog.solve("java_object('java.lang.Integer', [0], I), java_array_set_int(I, 0, 5).")
      solution.isSuccess shouldBe false
    }

    it("should pass variable test #1") {
      val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_array_length(A, Size).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("Size").asInstanceOf[Int]
      result.toString shouldBe "3"
    }

    it("should pass variable test #2") {
      val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_array_get(A, 0, I), I <- intValue returns V.")
      solution.isSuccess shouldBe true

      val result = solution.getVar("V").asInstanceOf[Int]
      result.toString shouldBe "_"
    }


    it("should pass variable test #3") {
      val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_get_int(XP, 3, V).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("V").asInstanceOf[Int]
      result.toString shouldBe "0"
    }

    it("should pass variable test #4") {
      val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_get_float(XP, 3, V).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("V").asInstanceOf[Float]
      result.toString shouldBe "0.0"
    }

    it("should pass variable test #5") {
      val solution = prolog.solve("java_object('java.lang.Integer[]', [3], A), java_object('java.lang.Integer', [2], Two), java_array_set(A, 2, Two), java_array_get(A, 2, X).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("X").asInstanceOf[Int]
      result.toString shouldBe "2"
    }

    it("should pass variable test #6") {
      val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_set_int(XP, 3, 2), java_array_get_int(XP, 3, V).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("V").asInstanceOf[Int]
      result.toString shouldBe "2"
    }

    it("should pass variable test #7") {
      val solution = prolog.solve("java_object('java.awt.Polygon', [], P), P.xpoints <- get(XP), java_array_set_float(XP, 3, 2), java_array_get_int(XP, 3, V).")
      solution.isSuccess shouldBe true

      val result = solution.getVar("V").asInstanceOf[Int]
      result.toString shouldBe "2.0"
    }
  }

  describe("java_throw/1 and java_catch/3") {

    it("the manager should be executed with the substitutions made between the exception and the catcher, and then the finally should be executed") {
      val goal = "atom_length(err, 3), java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), ((X is Cause+2, 5 is X+3)))], Y is 2+3), Z is X+5."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true

      val cause = info.getVar("Cause").asInstanceOf[Int]
      cause.intValue shouldBe 0

      val message = info.getVar("Message").asInstanceOf[Struct]
      message shouldBe new Struct("Counter")

      val stackTrace = info.getVar("StackTrace").asInstanceOf[Struct]
      stackTrace.isList shouldBe true

      val x = info.getVar("X").asInstanceOf[Int]
      x.intValue shouldBe 2

      val y = info.getVar("Y").asInstanceOf[Int]
      y.intValue shouldBe 5

      val z = info.getVar("Z").asInstanceOf[Int]
      z.intValue shouldBe 7
    }

    it("the nearest java_catch/3 ancestor in the resolution tree that has a unifiable catcher should be executed") {
      val goal = "java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), true)], true), java_catch(java_object('Counter', ['MyCounter2'], c2), [('java.lang.ClassNotFoundException'(C, M, ST), X is C+2)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true

      val cause = info.getVar("Cause").asInstanceOf[Int]
      cause.intValue shouldBe 0

      val message = info.getVar("Message").asInstanceOf[Struct]
      message shouldBe new Struct("Counter")

      val stackTrace = info.getVar("StackTrace").asInstanceOf[Struct]
      stackTrace.isList shouldBe true

      val x = info.getVar("X").asInstanceOf[Int]
      x.intValue shouldBe 2
    }

    it("the execution should fail if an error occurs and no unifiable catcher with the argument of the thrown exception is found ") {
      val goal = "java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.Exception'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe false
      info.isHalted shouldBe true
    }

    it("java_catch/3 should fail if the manager is false") {
      val goal = "java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), false)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe false
    }

    it("finally should be executed in case of success") {
      val goal = "java_catch(java_object('java.util.ArrayList', [], l), [(E, true)], (X is 2+3, Y is 3+5))."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true

      val e = info.getVar("E")
      e.isInstanceOf[Var] shouldBe true

      val x = info.getVar("X").asInstanceOf[Int]
      x.intValue shouldBe 5

      val y = info.getVar("Y").asInstanceOf[Int]
      y.intValue shouldBe 8
    }

    it("java_catch/3 should fail if an exeception occurs in the manager") {
      val goal = "java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), java_object('Counter', ['MyCounter2'], c2))], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe false
      info.isHalted shouldBe true
    }

    it("should use the correct catcher in the list") {
      val goal = "java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.Exception'(Cause, Message, StackTrace), X is 2+3), ('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), Y is 3+5)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true

      val x = info.getVar("X")
      x.isInstanceOf[Var] shouldBe true

      val y = info.getVar("Y")
      y.isInstanceOf[Int] shouldBe true

      y.asInstanceOf[Int].intValue shouldBe 8
    }
  }

  describe("java library exceptions") {
    
     it("should throw a ClassNotFoundException if ClassName does not identify a valid Java class") {
      val goal = "java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw a NoSuchMethodException if the constructor does not exist") {
      val goal = "java_catch(java_object('java.util.ArrayList', [a], c), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw an InvocationTargetException if ArgList's arguments are not grounded") {
      val goal = "java_catch(java_object('java.util.ArrayList', [X], c), [('java.lang.reflect.InvocationTargetException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw an Exception if ObjId already refers to another object in the system") {
      val goal = "java_object('java.util.ArrayList', [], c), java_catch(java_object('java.util.ArrayList', [], c), [('java.lang.Exception'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw a ClassNotFoundException if java_object_bt (ClassName, ArgList, ObjId) is called") {
      val goal = "java_catch(java_object_bt('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw a NoSuchMethodException if java_object_bt('java.util.ArrayList', [a], c) is called") {
      val goal = "java_catch(java_object_bt('java.util.ArrayList', [a], c), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw an InvocationTargetException if java_object_bt('java.util.ArrayList', [X], c) is called") {
      val goal = "java_catch(java_object_bt('java.util.ArrayList', [X], c), [('java.lang.reflect.InvocationTargetException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw an Exception if java_object_bt('java.util.ArrayList', [], c) is called") {
      val goal = "java_object_bt('java.util.ArrayList', [], c), java_catch(java_object('java.util.ArrayList', [], c), [('java.lang.Exception'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw a ClassNotFoundException if ClassSourceText contains mistakes") {
      val goal = "Source = 'public class Counter { , }', java_catch(java_class(Source, 'Counter', [], c), [('java.io.IOException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
      new File("Counter.java").delete
    }

    it("should throw a ClassNotFoundException  if the class can not be located in the specified package hierarchy") {
      val goal = "Source = 'public class Counter {  }', java_catch(java_class(Source, 'Counter', [], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
      new File("Counter.java").delete
      new File("Counter.class").delete
    }

     it("should throw a NoSuchMethodException if you invoke an invalid method") {
      val goal = "java_object('java.util.ArrayList', [], l), java_catch(java_call(l, sizes, res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw a NoSuchMethodException if method arguments are not valid") {
      val goal = "java_object('java.lang.String', ['call'], s), java_catch(java_call(s, charAt(a), res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw a NoSuchMethodException if method arguments are not grounded") {
      val goal = "java_object('java.lang.String', ['call'], s), java_catch(java_call(s, charAt(X), res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("'<-' (ObjId, MethodInfo), ObjIdResult) should throw a NoSuchMethodException if method arguments are not valid #1") {
      val goal = "java_object('java.util.ArrayList', [], l), java_catch((l <- sizes returns res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("'<-' (ObjId, MethodInfo), ObjIdResult) should throw a NoSuchMethodException if method arguments are not valid #2") {
      val goal = "java_object('java.lang.String', ['call'], s), java_catch((s <- charAt(a) returns res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("'<-' (ObjId, MethodInfo), ObjIdResult) should throw a NoSuchMethodException if method arguments are not valid #3") {
      val goal = "java_object('java.lang.String', ['call'], s), java_catch((s <- charAt(X) returns res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

     it("should throw an IllegalArgumentException if Index does not represent a correct value") {
      val goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(s, -1, a), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw an IllegalArgumentException if ObjId is not a correct value") {
      val goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(s, 0, 1), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw an IllegalArgumentException if ObjArrayId does not report any object") {
      val goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(x, 0, a), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

     it("should throw an IllegalArgumentException if java_array_get (ObjArrayId, Index, ObjIdResult) is called") {
      val goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_get(s, -1, ObjIdResult), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }

    it("should throw an IllegalArgumentException if java_array_set(x, 0, ObjIdResult) is called") {
      val goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(x, 0, ObjIdResult), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true)."
      val info = prolog.solve(goal)
      info.isSuccess shouldBe true
      val cause = info.getVar("Cause")
      cause.isInstanceOf[Var] shouldBe false
      val message = info.getVar("Message")
      message.isInstanceOf[Var] shouldBe false
      val stackTrace = info.getVar("StackTrace")
      stackTrace.isList shouldBe true
    }
  }
}
