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
package com.szadowsz.gospel.core.db.primitives.slang

import alice.tuprolog.IPrimitives
import com.szadowsz.gospel.core.data.{Struct, Term}
import com.szadowsz.gospel.core.db.primitives.SPrimitive

private[primitives] final class SPrimitive3(pType : Int, pKey: String, source: IPrimitives, val func: AnyRef) extends SPrimitive(pType, pKey, source,3) {

  override def evalAsDirective(g: Struct): Unit = {
    func.asInstanceOf[Function3[Term,Term,Term,Unit]](
      g.getTerm(0),
      g.getTerm(1),
      g.getTerm(2)
    )
  }

  override def evalAsPredicate(g: Struct): Boolean = {
    func.asInstanceOf[Function3[Term,Term,Term,Boolean]](
      g.getTerm(0),
      g.getTerm(1),
      g.getTerm(2)
    )
  }

  override def evalAsFunctor(g: Struct): Term = {
    func.asInstanceOf[Function3[Term,Term,Term,Term]](
      g.getTerm(0),
      g.getTerm(1),
      g.getTerm(2)
    )
  }
}
