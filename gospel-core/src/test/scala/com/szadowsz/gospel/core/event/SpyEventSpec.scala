package com.szadowsz.gospel.core.event

import com.szadowsz.gospel.core.event.interpreter.SpyEvent
import com.szadowsz.gospel.core.PrologEngine
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SpyEventSpec extends FlatSpec with Matchers {

  behavior of "Spy Event"

  it should "have its toString method map to its message" in {
    val msg = "testConstruction"
    val e = new SpyEvent(new PrologEngine, msg)
    e.toString shouldBe msg
  }
}
