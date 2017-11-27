package com.szadowsz.gospel.core.utils

import java.io.File
import java.net.URL

import com.szadowsz.gospel.core.utils.classloader.JavaDynamicClassLoader
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class JavaDynamicClassLoaderSpec extends FlatSpec with Matchers {


  behavior of "Java Dynamic Classloader"

  private val PATHS_NUMBER = 2
  private val paths = new Array[String](PATHS_NUMBER)

  private def setPath(valid: Boolean): Unit = {
    val file = new File(".")
    // Array paths contains a valid path
    if (valid) paths(0) = file.getCanonicalPath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "TestURLClassLoader.jar"
    paths(1) = file.getCanonicalPath
  }

  private def getURLsFromStringArray(paths: Array[String]) = {
    val urls = new Array[URL](paths.length)
    for (i <- paths.indices) {
      val directory = new File(paths(i))
      urls(i) = directory.toURI.toURL
    }
    urls
  }

  it should "be successfully instantiated" in {
    var loader = new JavaDynamicClassLoader
    loader should not be null
    setPath(true)
    val urls = getURLsFromStringArray(paths)
    loader = new JavaDynamicClassLoader(urls, this.getClass.getClassLoader)
    loader.getURLs.length shouldBe 2
  }

  it should "successfully load a class" in {
    setPath(true)
    val urls = getURLsFromStringArray(paths)
    val loader = new JavaDynamicClassLoader(urls, this.getClass.getClassLoader)
    loader.getURLs.length shouldBe 2

    val cl = loader.loadClass("Counter")
    cl should not be null

    val m = cl.getMethod("inc")
    m.setAccessible(true)
    val obj = cl.newInstance
    m.invoke(obj)
    val m1 = cl.getMethod("getValue")
    m1.setAccessible(true)
    val res_obj = m1.invoke(obj)
    val res = new Integer(res_obj.toString)
    res shouldBe 1
  }

  it should "fail to load a non-existant class" in {
    setPath(true)
    val urls = getURLsFromStringArray(paths)
    val loader = new JavaDynamicClassLoader(urls, this.getClass.getClassLoader)

    intercept[ClassNotFoundException] {
      loader.loadClass("ClassNotFound")
    }
  }

  it should "fail to load a class with an invalid path" in {
    val url = new File(".").toURI.toURL
    val loader = new JavaDynamicClassLoader(Array[URL](url), this.getClass.getClassLoader)
    intercept[ClassNotFoundException] {
      loader.loadClass("Counter")
    }
  }

  it should "handle urls successfully" in {
    val url = new File(".").toURI.toURL
    val loader = new JavaDynamicClassLoader(Array[URL](url), this.getClass.getClassLoader)
    loader.getURLs.length shouldBe 1

    loader.removeURL(url)
    loader.getURLs.length shouldBe 0

    setPath(true)
    loader.addURLs(getURLsFromStringArray(paths))
    loader.getURLs.length shouldBe 2

    loader.loadClass("Counter")
    loader.getLoadedClasses.length shouldBe 1
  }

  it should "handle nested packages successfully" in {
    val file = new File(".")
    val tempPath = file.getCanonicalPath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "TestURLClassLoaderNestedPackage.jar"
    val urls = getURLsFromStringArray(Array[String](tempPath))
    val loader = new JavaDynamicClassLoader(urls, this.getClass.getClassLoader)

    var cl = loader.loadClass("acme.corp.Counter")
    cl should not be null

    cl = loader.loadClass("java.lang.String")
    cl should not be null
  }
}
