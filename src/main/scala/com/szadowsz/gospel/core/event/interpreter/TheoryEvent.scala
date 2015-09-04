/* tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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
package com.szadowsz.gospel.core.event.interpreter

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.db.theory.Theory

/**
 * This class represents events occurring in theory management.
 *
 * @since 1.3
 *
 */
@SerialVersionUID(1L)
class TheoryEvent(source: Prolog, oldth: Theory, newth: Theory) extends PrologEvent(source) {

  private val _old = oldth
  private val _new = newth

  /**
   * Gets the old theory
   *
   * @return the old theory
   */
  def getOldTheory() = _old

  /**
   * Gets the new theory
   *
   * @return the new theory
   */
  def getNewTheory() = _new

}
