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
package com.szadowsz.gospel.core.json

import com.szadowsz.gospel.core.{BaseEngineSpec, PrologEngine}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class JsonSpec extends FunSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  describe("Json Marshalling") {
    it("should serialise the default engine with its knowledge base successfully") {
      val json = prolog.toJSON().replaceFirst("\"serializationTimestamp\":\\d+", "\"serializationTimestamp\":1")
      json shouldBe Source.fromInputStream(getClass.getResourceAsStream("/json/defaultFullEngineState.json")).mkString
    }
  }

  describe("Json Unmarshalling") {
    it("should deserialise the default engine with its knowledge base successfully") {
      val json = Source.fromInputStream(getClass.getResourceAsStream("/json/defaultFullEngineState.json")).mkString
      PrologEngine.fromJSON(json) should not be null
    }
  }
}
