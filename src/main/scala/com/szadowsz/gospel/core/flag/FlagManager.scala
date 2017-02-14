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
package com.szadowsz.gospel.core.flag

import com.szadowsz.gospel.core.data.{Struct, Term}

/**
 * Administrator of flags declared
 *
 * @author Alex Benini
 */
final class FlagManager {

  private var flags : List[Flag] = List()

  /**
   * Defines a new flag
   */
  def defineFlag(name: String, valueList: Struct, defValue: Term, modifiable: Boolean, libName: String): Boolean = {
    synchronized{
      if (!flags.exists(flag => flag.getName == name)){
        flags = flags :+ new Flag(name, valueList, defValue, modifiable, libName)
        return true
      } else {
        return false
      }
    }
  }

  def setFlag(name: String, value: Term): Boolean = synchronized{flags.find(_.getName == name).exists(_.setValue(value))}

  def getPrologFlagList: Struct = {
    synchronized{flags.map(fl => new Struct("flag", new Struct(fl.getName), fl.getValue)).foldLeft(new Struct)((b,a) => new Struct(a,b))}
  }

  def getFlag(name: String): Term = {
    synchronized{flags.map(flag => flag.getName -> flag.getValue).toMap.getOrElse(name,null)}
  }

  /*
   * returns true if there is a flag with the given name and this flag is editable
   */
  def isModifiable(name: String): Boolean = synchronized{flags.exists(flag => flag.getName == name && flag.isModifiable)}

  /*
   * Returns true if there is a flag with the given name and the value is eligible for this flag
   */
  def isValidValue(name: String, value: Term): Boolean = synchronized{flags.exists(flag => flag.getName == name && flag.isValidValue(value))}
}