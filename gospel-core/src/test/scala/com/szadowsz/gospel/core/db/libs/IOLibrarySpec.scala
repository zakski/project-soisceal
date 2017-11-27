package com.szadowsz.gospel.core.db.libs

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
    val engine = new PrologEngine()
    val l = new TestOutputListener()
    engine.addOutputListener(l)
    engine.solve("tab(5).")
    l.output should be ("     ")
  }
}