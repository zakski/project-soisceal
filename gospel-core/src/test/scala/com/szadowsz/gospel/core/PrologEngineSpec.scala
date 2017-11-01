package com.szadowsz.gospel.core

import alice.tuprolog.StringLibrary
import com.szadowsz.gospel.core.db.Library
import com.szadowsz.gospel.core.db.libs._
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class PrologEngineSpec extends FlatSpec with BaseEngineSpec {

  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Basic Engine Functionality"

  it should "initialise with the default libraries successfully" in {
    prolog.getCurrentLibraries should have length 4
    prolog.getLibrary(classOf[MyBasicLibrary].getName) should not be null
    prolog.getLibrary(classOf[ISOLibrary].getName) should not be null
    prolog.getLibrary(classOf[IOLibrary].getName) should not be null
    prolog.getLibrary(classOf[OOLibrary].getName) should not be null
  }

  it should "load a library from a string successfully" in {
    prolog.loadLibrary(classOf[StringLibrary].getName)
    prolog.getLibrary(classOf[StringLibrary].getName) should not be null
  }

  it should "load a library from an object successfully" in {
    val stringLibrary: Library = new StringLibrary
    prolog.loadLibrary(stringLibrary)
    prolog.getLibrary(classOf[StringLibrary].getName) should not be null

    val javaLibrary: Library = new OOLibrary
    prolog.loadLibrary(javaLibrary)
    javaLibrary eq prolog.getLibrary("com.szadowsz.gospel.core.db.libs.OOLibrary") shouldBe true
  }
}
