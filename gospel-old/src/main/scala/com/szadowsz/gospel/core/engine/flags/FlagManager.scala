/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.szadowsz.gospel.core.engine.flags

import java.util

import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.json.{EngineState, JSONSerializerManager}
import scala.collection.JavaConverters._

/**
  * Administrator of declared flags.
  *
  * Created on 15/02/2017.
  *
  * @version Gospel 2.0.0
  */
private[core] final class FlagManager extends java.io.Serializable {

  private var flags: List[Flag] = List()
  init()

  private def init() = {
    val s = new Struct
    s.append(new Struct("on"))
    s.append(new Struct("off"))
    this.defineFlag("occursCheck", s, new Struct("on"), true, "ScalaBuiltIn")
  }


  /**
    * Defines a new flag
    */
  def defineFlag(name: String, valueList: Struct, defValue: Term, modifiable: Boolean, libName: String): Boolean = synchronized {
    if (!flags.exists(flag => flag.getName == name)) {
      flags = flags :+ new Flag(name, valueList, defValue, modifiable, libName)
      return true
    } else return false
  }

  def setFlag(name: String, value: Term): Boolean = synchronized {
    flags.find(_.getName == name).exists(_.setValue(value))
  }

  def getPrologFlagList: Struct = synchronized {
    flags.map(fl => new Struct("flag", new Struct(fl.getName), fl.getValue)).foldLeft(new Struct)((b, a) => new Struct(a, b))
  }

  def getFlag(name: String): Term = synchronized {
    flags.map(flag => flag.getName -> flag.getValue).toMap.getOrElse(name, null)
  }

  def isOccursCheckEnabled: Boolean = synchronized {
    flags.exists(f => f.getName == "occursCheck" && f.getValue.toString == "on")
  }

  /*
   * returns true if there is a flag with the given name and this flag is editable
   */
  def isModifiable(name: String): Boolean = synchronized {
    flags.exists(flag => flag.getName == name && flag.isModifiable)
  }

  /*
   * Returns true if there is a flag with the given name and the value is eligible for this flag
   */
  def isValidValue(name: String, value: Term): Boolean = synchronized {
    flags.exists(flag => flag.getName == name && flag.isValidValue(value))
  }

  def serializeFlags(brain: EngineState): Unit = {
    val a = new util.ArrayList[String]
    flags.foreach(f => a.add(JSONSerializerManager.toJSON(f)))
    brain.asInstanceOf[EngineState].setFlags(a)
  }

  def reloadFlags(brain: EngineState): Unit = {
    brain.getFlags.asScala.foreach(s => flags = flags :+ JSONSerializerManager.fromJSON(s, classOf[Flag]))
  }
}