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
package alice.tuprolog.core.flag

import java.util.HashMap

import alice.tuprolog.core.data.{Var, Term, Struct}

/**
 * This class represents a prolog Flag
 *
 */
@SerialVersionUID(1L)
private[flag] class Flag  (theName: String, theValSet: Struct, theDefValue: Term, theCurValue: Term, canModify: Boolean, theLibrary: String) extends Serializable {
  private val _name: String = theName

  private val _valueList: Struct = theValSet
  private val _defaultValue: Term = theDefValue
  private val _value: Term = theCurValue

  private val _modifiable: Boolean = canModify
  private val _libraryName: String = theLibrary


  /**
   * Builds a Prolog flag
   *
   * @param theName is the name of the flag
   * @param theValSet is the Prolog list of the possible values
   * @param theDefValue is the default value
   * @param isModifiable states if the flag is modifiable
   * @param theLibrary is the library defining the flag
   */
  def this (theName: String, theValSet: Struct, theDefValue: Term, isModifiable: Boolean, theLibrary: String){
    this(theName,theValSet,theDefValue,theDefValue,isModifiable,theLibrary)
  }


  /**
   * Gets a deep copy of the flag
   *
   * @return a copy of the flag
   */
  override def clone: AnyRef = {
    val name = _name
    val valueList = _valueList.copy(new HashMap[Var, Var], Var.ORIGINAL).asInstanceOf[Struct]
    val value = _value.copy(new HashMap[Var, Var], Var.ORIGINAL)
    val defaultValue = _defaultValue.copy(new HashMap[Var, Var], Var.ORIGINAL)
    val modifiable = _modifiable
    val libraryName = _libraryName
    val f = new Flag(name, valueList, defaultValue, value, modifiable, libraryName)
    return f
  }

  /**
   * Checks if a value is valid according to flag description
   *
   * @param value the possible value of the flag
   * @return flag validity
   */
  def isValidValue(value: Term): Boolean = {
    _valueList.iterator.exists(value.matches(_))
  }

  /**
   * Gets the name of the flag
   * @return  the name
   */
  def getName: String = {
    return _name
  }

  /**
   * Gets the list of flag possible values
   * @return  a Prolog list
   */
  def getValueList: Struct = {
    return _valueList
  }

  /**
   * Sets the value of a flag
   *
   * @param value new value of the flag
   * @return true if the value is valid
   */
  def setValue(value: Term): Flag = {
    if (isValidValue(value) && _modifiable) {
      return new Flag(_name, _valueList, _defaultValue, value, _modifiable, _libraryName)
    }
    else {
      return this
    }
  }

  /**
   * Gets the current value of the flag
   * @return  flag current value
   */
  def getValue: Term = {
    return _value
  }

  /**
   * Checks if the value is modifiable
   * @return
   */
  def isModifiable: Boolean = {
    return _modifiable
  }

  /**
   * Gets the name of the library where the flag has been defined
   * @return  the library name
   */
  def getLibraryName: String = {
    return _libraryName
  }
}