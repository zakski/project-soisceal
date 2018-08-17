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
package com.szadowsz.gospel.core.parser

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class PrologSrcFinderSpec extends FlatSpec with Matchers with BeforeAndAfter {

  behavior of "PrologSrcFinder"

  it should "find a named .pl file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("plfile.pl")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a named .pro file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("profile.pro")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a named .prolog file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("prologfile.prolog")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .pl extension to find a named file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("plfile")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .pro extension to find a named file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("profile")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .prolog extension to find a named file in the resources folder" in {
   val result = PrologSrcFinder.searchForTheories("prologfile")

    result should have length 1

    val res = result.head


    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a nested named .pl file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("*/nestedplfile.pl")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a nested .pro file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("*/nestedprofile.pro")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a nested .prolog file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("*/nestedprologfile.prolog")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .prolog extension to find a nested named file in the resources folder" in {
    val result = PrologSrcFinder.searchForTheories("*/nestedprologfile")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "not find a non-existent file in the resources folder" in {
    val res = PrologSrcFinder.searchForTheories("nonexistent.pl")
    res should have length 0
  }

  it should "find a relative pathed .pl file in the file system" in {
    val result = PrologSrcFinder.searchForTheories("./test-samples/plfile.pl")

    result should have length 1

    val res = result.head

    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a named .pro file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/profile.pro").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a named .prolog file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/prologfile.prolog").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .pl extension to find a named file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/plfile").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .pro extension to find a named file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/profile").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .prolog extension to find a named file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/prologfile").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a nested named .pl file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/*/nestedplfile.pl").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a nested .pro file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/*/nestedprofile.pro").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "find a nested .prolog file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/*/nestedprologfile.prolog").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "assume a .prolog extension to find a nested named file in the file system" in {
    val res = PrologSrcFinder.searchForTheories("./test-samples/*/nestedprologfile").head
    res.exists() shouldBe true
    res.isFile shouldBe true
    res.isReadable shouldBe true
  }

  it should "not find a non-existent file in the file system" in {
    val result = PrologSrcFinder.searchForTheories("./test-samples/nonexistent.pl")
    result should have length 0
  }
}
