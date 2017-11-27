package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.db.Library
import com.szadowsz.gospel.core.db.libs.{StringLibrary, _}
import com.szadowsz.gospel.core.error.InvalidTheoryException
import com.szadowsz.gospel.core.event.interpreter.SpyEvent
import com.szadowsz.gospel.core.listener.{SpyListener, TestPrologEventListener}
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

  it should "get a library by its name" in {
    val engine = new PrologEngine(Array[String]("com.szadowsz.gospel.core.db.libs.TestLibrary"))
    engine.getLibrary("TestLibraryName") should not be null
  }

  it should "be able to unload a library after setting a theory" in {
    prolog.getLibrary("com.szadowsz.gospel.core.db.libs.IOLibrary") should not be null
    val t = new Theory("a(1).\na(2).\n")
    prolog.setTheory(t)
    prolog.unloadLibrary("com.szadowsz.gospel.core.db.libs.IOLibrary")
    prolog.getLibrary("com.szadowsz.gospel.core.db.libs.IOLibrary") shouldBe null
  }

  it should "fail to add an invalid theory" in {
    val t = new Theory("test :- notx existing(s).")
    intercept[InvalidTheoryException] {
      prolog.addTheory(t)
    }
  }

  it should "be able to attach multiple spy listeners" in {
    val listener1 = new SpyListener {
      override def onSpy(e: SpyEvent): Unit = {
      }
    }
    val listener2 = new SpyListener {
      override def onSpy(e: SpyEvent): Unit = {
      }
    }
    prolog.addSpyListener(listener1)
    prolog.addSpyListener(listener2)
    prolog.getSpyListenerList.size shouldBe 2
  }

  it should "be able to report library events" in {
    val engine = new PrologEngine(Array[String]())
    engine.loadLibrary("com.szadowsz.gospel.core.db.libs.BasicLibrary")
    engine.loadLibrary("com.szadowsz.gospel.core.db.libs.IOLibrary")
    val a = new TestPrologEventListener()
    engine.addLibraryListener(a)

    engine.loadLibrary("com.szadowsz.gospel.core.db.libs.OOLibrary")
    a.firstMessage shouldBe "com.szadowsz.gospel.core.db.libs.OOLibrary"

    engine.unloadLibrary("com.szadowsz.gospel.core.db.libs.OOLibrary")
    a.firstMessage shouldBe "com.szadowsz.gospel.core.db.libs.OOLibrary"
  }

  it should "be able to report theory events" in {
    val a = new TestPrologEventListener
    prolog.addTheoryListener(a)
    var t = new Theory("a(1).\na(2).\n")
    prolog.setTheory(t)
    a.firstMessage shouldBe ""
    a.secondMessage shouldBe "a(1).\n\na(2).\n\n"
    t = new Theory("a(3).\na(4).\n")
    prolog.addTheory(t)
    a.firstMessage shouldBe "a(1).\n\na(2).\n\n"
    a.secondMessage shouldBe "a(1).\n\na(2).\n\na(3).\n\na(4).\n\n"
  }

  it should "be able to report query events" in {
    val a = new TestPrologEventListener
    prolog.addQueryListener(a)
    prolog.setTheory(new Theory("a(1).\na(2).\n"))
    prolog.solve("a(X).")
    a.firstMessage shouldBe "a(X)"
    a.secondMessage shouldBe "yes.\nX / 1"
    prolog.solveNext()
    a.firstMessage shouldBe "a(X)"
    a.secondMessage shouldBe "yes.\nX / 2"
  }
}
