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
package com.szadowsz.gospel.core.db

import com.szadowsz.gospel.core.{Interpreter, InterpreterBuilder}
import com.szadowsz.gospel.core.data.{Int, Struct}
import com.szadowsz.gospel.core.db.theory.{Theory, TheoryManager}
import com.szadowsz.gospel.core.test.TestLogRecorder
import org.apache.log4j.{Level, Logger}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class TheoryManagerSpec extends FlatSpec with Matchers with BeforeAndAfter {

  protected val logger : Logger = Logger.getLogger(classOf[TheoryManager])
  protected val recorder : TestLogRecorder = new TestLogRecorder

  protected var wam : Interpreter = _
  protected var theoryManager : TheoryManager = _


  behavior of "Theory Manager"

  before {
    logger.addAppender(recorder)
    wam = InterpreterBuilder.getDefaultEngine
    theoryManager = wam.getTheoryManager
  }

  after {
    recorder.clear()
    wam = null
    theoryManager = null
  }

  it should "warn about unknown directives" in {
    val theory = ":- unidentified_directive(unknown_argument)."

    theoryManager.consult(new Theory(theory))

    val result  = recorder.getLogs.filter(e => e.getLevel == Level.WARN)
    result should have length 1
    result.head.getMessage shouldBe "The directive unidentified_directive/1 is unknown."
  }

  it should "warn about failed directives" in {
    val theory = ":- use_module(library('UnknownLibrary'))."

    theoryManager.consult(new Theory(theory))
  
    val result  = recorder.getLogs.filter(e => e.getLevel == Level.WARN)
    result should have length 1
    result.head.getMessage shouldBe "Exception thrown during execution of use_module/1 directive."
   }

    it should "abolish predicates correctly via method call" in {
      val theory = "test(A, B) :- A is 1+2, B is 2+3."
      theoryManager.consult(new Theory(theory))

      val testTerm = new Struct("test", new Struct("a"), new Struct("b"))
      var testClauses  = theoryManager.find(testTerm)
      testClauses should have size 1

      theoryManager.abolish(new Struct("/", new Struct("test"), Int(2)))
      testClauses = theoryManager.find(testTerm)

      testClauses should have size 0
    }

//  it should "not allow asserts to be backtracked" in {
//    val solution = prolog.solve("assertz(a(z)).")
//    solution.isSuccess shouldBe true
//    solution.hasOpenAlternatives shouldBe false
//  }
//
//
//  it should "abolish predicates correctly via abolish predicate" in {
//    prolog.setTheory(new Theory("fact(new).\nfact(other).\n"))
//
//    var solution = prolog.solve("abolish(fact/1).")
//    solution.isSuccess shouldBe true
//
//    solution = prolog.solve("fact(V).")
//    solution.isSuccess shouldBe false
//  }
//
//  it should "retract all predicates correctly" in {
//    var solution = prolog.solve("assert(takes(s1,c2)), assert(takes(s1,c3)).")
//    solution.isSuccess shouldBe true
//    solution = prolog.solve("takes(s1, N).")
//    solution.isSuccess shouldBe true
//    solution.hasOpenAlternatives shouldBe true
//    solution.getVar("N").toString should be("c2")
//    solution = prolog.solveNext()
//    solution.isSuccess shouldBe true
//    solution.getVar("N").toString should be("c3")
//
//    solution = prolog.solve("retractall(takes(s1,c2)).")
//    solution.isSuccess shouldBe true
//    solution = prolog.solve("takes(s1, N).")
//    solution.isSuccess shouldBe true
//    solution.hasOpenAlternatives shouldBe false
//    solution.getVar("N").toString should be("c3")
//  }
//
//  // TODO test retractall: ClauseDatabase#get(f/a) should return an empty list
//
//  it should "retract a predicate successfully" in {
//    val listener = new TestOutputListener()
//    prolog.addOutputListener(listener)
//    prolog.setTheory(new Theory("insect(ant). insect(bee)."))
//    val solution = prolog.solve("retract(insect(I)), write(I), retract(insect(bee)), fail.")
//    solution.isSuccess shouldBe false
//    listener.output should be ("antbee")
//  }
}
