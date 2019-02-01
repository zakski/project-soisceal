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
package com.szadowsz.gospel.core.db.libraries

import com.szadowsz.gospel.core.exception.library.{InvalidLibraryException, LibraryInstantiationException}
import com.szadowsz.gospel.core.test.{TestLibrary, TestNoArgConstructorLibrary, TestLogRecorder}
import com.szadowsz.gospel.core.{Interpreter, InterpreterBuilder}
import org.apache.log4j.{Level, Logger}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, FunSpec, Matchers}

/**
  * Created on 18/02/2017.
  */
@RunWith(classOf[JUnitRunner])
class LibraryManagerSpec extends FunSpec with Matchers with BeforeAndAfter {

  protected var wam: Interpreter = _
  protected var libManager: LibraryManager = _
  protected val logger: Logger = Logger.getLogger(classOf[LibraryManager])
  protected val recorder: TestLogRecorder = new TestLogRecorder

  before {
    logger.addAppender(recorder)
    wam = InterpreterBuilder.getDefaultEngine
    libManager = wam.getLibraryManager
  }

  after {
    recorder.clear()
    wam = null
    libManager = null
  }

  describe("Library Manager unloadLibrary Function") {

    it("should unload a library successfully") {
      val originalLibCount = libManager.getLibCount
      val result = libManager.loadLibrary("com.szadowsz.gospel.core.test.TestLibrary")

      result shouldBe a[TestLibrary]
      result.getName shouldBe "test"

      libManager.getLibCount shouldBe originalLibCount+1

      libManager.unloadLibrary("test")

      libManager.getLibCount shouldBe originalLibCount
    }

    it("should warn if it is called on a library that is not loaded") {
      val originalLibCount = libManager.getLibCount
      libManager.unloadLibrary("test")

      libManager.getLibCount shouldBe originalLibCount

      val logs = recorder.getLogs
      logs.head.getLevel shouldBe Level.WARN
      logs.head.getMessage shouldBe "Library test not loaded."
    }
  }

  describe("Library Manager loadLibrary Functionality for Library Classes") {

    it("should find a named library class on the classpath using its full name") {
      val result = libManager.loadLibrary("com.szadowsz.gospel.core.test.TestLibrary")

      result shouldBe a[TestLibrary]
      result.getName shouldBe "test"
    }

    it("should find a named library class on the classpath using its short name") {
      val result = libManager.loadLibrary("TestLibrary")

      result shouldBe a[TestLibrary]
      result.getName shouldBe "test"
    }

    it("should find a named library class on the classpath using the Library's getName method") {
      val result = libManager.loadLibrary("test")

      result shouldBe a[TestLibrary]
      result.getName shouldBe "test"
    }

    it("should throw an LibraryInstantiationException if the Class can not be instantiated") {
      val exception = intercept[InvalidLibraryException] {
        libManager.loadLibrary("TestAbstractLibrary")
      }

      val cause = exception.getCause
      cause shouldBe a[LibraryInstantiationException]
      cause.getMessage shouldBe "Library TestAbstractLibrary is abstract"
    }

    it("should throw an LibraryInstantiationException if the Class does not have a valid constructor") {
      val exception = intercept[InvalidLibraryException] {
        libManager.loadLibrary(classOf[TestNoArgConstructorLibrary].getName)
      }

      val cause = exception.getCause
      cause shouldBe a[LibraryInstantiationException]
      cause.getMessage shouldBe "Library TestNoArgConstructorLibrary does not have a valid constructor"
    }

    it("should throw an LibraryInstantiationException if the Class is not accessible") {
      pendingUntilFixed {
        val exception = intercept[InvalidLibraryException] {
          libManager.loadLibrary("TestPrivateLibrary")
        }

        val cause = exception.getCause
        cause shouldBe a[LibraryInstantiationException]
        cause.getMessage shouldBe "Failed to access library TestPrivateLibrary"
      }
    }
  }
  
  describe("Library Manager loadLibrary Functionality for Resource Files") {
    it("should find a named .pl file in the resources folder") {
      val result = libManager.loadLibrary("plfile.pl")
  
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "plfile"
    }
  

      it ("should find a named .pro file in the resources folder") {
        val result = libManager.loadLibrary("profile.pro")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "profile"
      }

      it ("should find a named .prolog file in the resources folder") {
        val result = libManager.loadLibrary("prologfile.prolog")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "prologfile"
      }

      it ("should assume a .pl extension to find a named file in the resources folder") {
        val result = libManager.loadLibrary("plfile")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "plfile"
      }

      it ("should assume a .pro extension to find a named file in the resources folder") {
        val result = libManager.loadLibrary("profile")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "profile"
      }

      it ("should assume a .prolog extension to find a named file in the resources folder") {
        val result = libManager.loadLibrary("prologfile")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "prologfile"
      }

      it ("should find a nested named .pl file in the resources folder") {
        val result = libManager.loadLibrary("*/nestedplfile.pl")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "nestedplfile"
      }

      it ("should find a nested .pro file in the resources folder") {
        val result = libManager.loadLibrary("*/nestedprofile.pro")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "nestedprofile"
      }

      it ("should find a nested .prolog file in the resources folder") {
        val result = libManager.loadLibrary("*/nestedprologfile.prolog")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "nestedprologfile"
      }

      it ("should assume a .prolog extension to find a nested named file in the resources folder") {
        val result = libManager.loadLibrary("*/nestedprologfile")

        result shouldBe a [ResourceLibrary]
        result.getName shouldBe "nestedprologfile"
      }

      it ("should not find a non-existent file in the resources folder") {
        intercept[InvalidLibraryException] {
          val result = libManager.loadLibrary("nonexistent.pl")
        }
      }
  }
 
  describe("Library Manager loadLibrary Functionality for Files") {
  
  
    it("should find a relative pathed .pl file in the file system") {
      val result = libManager.loadLibrary("./test-samples/plfile.pl")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "plfile"
    }
  
    it("should find a named .pro file in the file system") {
      val result = libManager.loadLibrary("./test-samples/profile.pro")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "profile"
    }
  
    it("should find a named .prolog file in the file system") {
      val result = libManager.loadLibrary("./test-samples/prologfile.prolog")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "prologfile"
    }
  
    it("should assume a .pl extension to find a named file in the file system") {
      val result = libManager.loadLibrary("./test-samples/plfile")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "plfile"
    }
  
    it("should assume a .pro extension to find a named file in the file system") {
      val result = libManager.loadLibrary("./test-samples/profile")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "profile"
    }
  
    it("should assume a .prolog extension to find a named file in the file system") {
      val result = libManager.loadLibrary("./test-samples/prologfile")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "prologfile"
    }
  
    it("should find a nested named .pl file in the file system") {
      val result = libManager.loadLibrary("./test-samples/*/nestedplfile.pl")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "nestedplfile"
    }
  
    it("should find a nested .pro file in the file system") {
      val result = libManager.loadLibrary("./test-samples/*/nestedprofile.pro")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "nestedprofile"
    }
  
    it("should find a nested .prolog file in the file system") {
      val result = libManager.loadLibrary("./test-samples/*/nestedprologfile.prolog")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "nestedprologfile"
    }
  
    it("should assume a .prolog extension to find a nested named file in the file system") {
      val result = libManager.loadLibrary("./test-samples/*/nestedprologfile")
    
      result shouldBe a[ResourceLibrary]
      result.getName shouldBe "nestedprologfile"
    }
  
    it("should not find a non-existent file in the file system") {
      intercept[InvalidLibraryException] {
        libManager.loadLibrary("./test-samples/nonexistent.pl")
      }
    }
  }
}
