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

import java.nio.file.Paths

import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import scala.util.Try

/**
  * Resource Retrieval Class For Theories and Libraries.
  */
object PrologSrcFinder {

  val CLASSPATH_ALL_URL_PREFIX = "classpath*:"

  private val resolver = new PathMatchingResourcePatternResolver

  private val defaultExtensions = List(".pl",".pro",".prolog")


  private def workingDir = Paths.get("").toAbsolutePath.toString

  private def validateExtension(filePath: String) : Unit = {
    assert(defaultExtensions.exists(filePath.endsWith) || filePath.lastIndexOf('.') <= 0)
  }

  private def validateResources(resources: Try[Array[Resource]]) : Try[Array[Resource]] = {
    resources.map { rs =>
      assert(rs.nonEmpty && rs.forall(r => r.exists() && r.isReadable))
      rs
    }
  }

  private def sanitizePath(filePath: String): String = {
    val classifierRemoved = if (filePath.indexOf(':') < 0) filePath.substring(filePath.indexOf(':') + 1) else filePath
    val extensionAdded = if (classifierRemoved.lastIndexOf('.') <= 0) classifierRemoved + ".*" else classifierRemoved
    if (extensionAdded.indexOf('.') == 0) s"$workingDir/${extensionAdded.substring(1)}" else extensionAdded
  }

  def searchForTheories(filePath : String) : Array[Resource] = {
    validateExtension(filePath)
    val wrappedPath = sanitizePath(filePath)
    val searchAttempt = Try(resolver.getResources(CLASSPATH_ALL_URL_PREFIX + wrappedPath))

    validateResources(validateResources(searchAttempt).orElse(Try(resolver.getResources("file:" +wrappedPath)))).getOrElse(Array())
  }
}
