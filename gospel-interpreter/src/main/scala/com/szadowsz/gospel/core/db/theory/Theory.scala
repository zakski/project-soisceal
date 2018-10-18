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
package com.szadowsz.gospel.core.db.theory

import java.io.{BufferedReader, File, InputStreamReader}
import java.net.{URI, URL}

import com.szadowsz.gospel.core.data.Term
import com.szadowsz.gospel.core.db.operators.OperatorManager
import com.szadowsz.gospel.core.parser.Parser
import org.springframework.core.io.{FileSystemResource, Resource, StringResource, UrlResource}

import scala.io.Source

class Theory(private val resource : Resource) {

  def this(s : String){
    this(new StringResource(s))
  }

  def this(f : File){
    this(new FileSystemResource(f))
  }

  def this(u : URI){
    this(new UrlResource(u.toURL))
  }

  def this(u : URL){
    this(new UrlResource(u))
  }

  private[db] def getResourceName : Option[String] = Option(resource.getFilename).map(fn => fn.substring(0,fn.lastIndexOf('.')))

  def iterator()(implicit opManager: OperatorManager): Iterator[Term] = {
    if (resource != null && resource.isFile){
      println(Source.fromFile(resource.getFile).getLines().mkString("/n"))
    }
    new Parser(new BufferedReader(new InputStreamReader(resource.getInputStream))).iterator
  }
}
