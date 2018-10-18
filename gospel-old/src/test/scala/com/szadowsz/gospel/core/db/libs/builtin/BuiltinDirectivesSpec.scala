/**
  * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
  *
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
package com.szadowsz.gospel.core.db.libs.builtin

import java.io.File

import com.szadowsz.gospel.core.data.Number
import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine, Theory}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuiltinDirectivesSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine(Array[String]()) // include built-in predicates only

  describe("initialization/1") {

    it("should successfully execute a goal") {
      val theory =
        """
          |:- initialization(asserta(test(2))).
          |
          |test(1).
        """.stripMargin
      prolog.setTheory(new Theory(theory))
      val solution = prolog.solve("test(X).")
      solution.isSuccess shouldBe true
      val x = solution.getVar("X").asInstanceOf[Number]
      x.intValue shouldBe 2

    }

    it("should fail silently if the goal does not succeed") {
      val theory =
        """
          |:- initialization(fail).
          |
          |test(1).
        """.stripMargin
      prolog.setTheory(new Theory(theory))
      val solution = prolog.solve("test(X).")
      solution.isSuccess shouldBe true
      val x = solution.getVar("X").asInstanceOf[Number]
      x.intValue shouldBe 1
    }

    it("should fail silently if the goal is not a struct") {
      val theory =
        """
          |:- initialization(Z).
          |
          |test(1).
        """.stripMargin
      prolog.setTheory(new Theory(theory))
      val solution = prolog.solve("test(X).")
      solution.isSuccess shouldBe true
      val x = solution.getVar("X").asInstanceOf[Number]
      x.intValue shouldBe 1
    }
  }

  describe("include/1") {

    it("should successfully load a prolog file from the file system using an absolute path") {
      val path = new File("./src/test/resources/prolog/include.pl").getAbsolutePath
      val theory = s":- include('$path')."
      prolog.setTheory(new Theory(theory))
      val solution = prolog.solve("helloworld(X).")
      solution.isSuccess shouldBe true
    }

    it("should successfully load a prolog file from the file system using a relative path") {
      val path = "./src/test/resources/prolog/include.pl"
      val theory = s":- include('$path')."
      prolog.setTheory(new Theory(theory))
      val solution = prolog.solve("helloworld(X).")
      solution.isSuccess shouldBe true
    }

    it("should fail silently if the file does not exist") {
      val path = "./src/test/resources/prolog/nonexistent.pl"
      val theory = s"""
          |:- include('$path').
          |
          |test(1).
        """.stripMargin
      prolog.setTheory(new Theory(theory))
      val solution = prolog.solve("test(X).")
      solution.isSuccess shouldBe true
      val x = solution.getVar("X").asInstanceOf[Number]
      x.intValue shouldBe 1
    }
  }
}
