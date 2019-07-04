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

import com.szadowsz.gospel.core.data.{Struct, Term}

/**
  * This class represents a tuProlog Engine Flag.
  *
  * Created on 15/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
private[flags] class Flag(flagName: String, valueList: Struct, defaultValue: Term, v: Term, canModify: Boolean, library: String) extends Serializable {
  private val name: String = flagName
  private val lib: String = library

  private val valList: Struct = valueList
  private val defaultVal: Term = defaultValue
  private val modifiable: Boolean = canModify

  private var value: Term = v

  /**
    * Builds a Prolog flag
    *
    * @param theName      is the name of the flag
    * @param theValSet    is the Prolog list of the possible values
    * @param theDefValue  is the current & default value
    * @param isModifiable states if the flag is modifiable
    * @param theLibrary   is the library defining the flag
    */
  def this(theName: String, theValSet: Struct, theDefValue: Term, isModifiable: Boolean, theLibrary: String) {
    this(theName, theValSet, theDefValue, theDefValue, isModifiable, theLibrary)
  }

  /**
    * Checks if a value is valid according to flag description
    *
    * @param value the possible value of the flag
    * @return flag validity
    */
  def isValidValue(value: Term): Boolean = valList.getListIterator.exists(t => value.isUnifiable(t))

  /**
    * Checks if the value is modifiable
    *
    * @return
    */
  def isModifiable: Boolean = modifiable

  /**
    * Gets the name of the flag
    *
    * @return the name
    */
  def getName: String = name

  /**
    * Gets the name of the library where the flag has been defined
    *
    * @return the library name
    */
  def getLibraryName: String = lib

  /**
    * Gets the current value of the flag
    *
    * @return flag current value
    */
  def getValue: Term = value

  /**
    * Gets the list of flag possible values
    *
    * @return a Prolog list
    */
  def getValueList: Struct = valList

  /**
    * Sets the value of a flag
    *
    * @param value new value of the flag
    * @return true if the value is valid
    */
  def setValue(value: Term): Boolean = {
    if (isValidValue(value) && modifiable) {
      this.value = value
      true
    } else {
      false
    }
  }
}