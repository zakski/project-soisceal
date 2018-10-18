package com.szadowsz.gospel.core.test.swi

import com.szadowsz.gospel.core.PrologEngine
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SwiTester extends FunSpec {

  val testFiles = PrologModuleGetter.getPrologFiles("prolog").filter(f => f.getName.startsWith("test_"))

  testFiles.foreach { f =>
    describe(f.getName.dropRight(3)){

      it ("should load swi-prolog test suite successfully"){
        val engine = new PrologEngine()
        engine.loadLibrary(f)
      }

    }
  }
}
