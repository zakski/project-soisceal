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
package alice.tuprolog.core.engine.state

import alice.tuprolog.core.engine.{Engine, EngineRunner}
import alice.tuprolog.util.LoggerCategory
import org.slf4j.LoggerFactory

/**
 * @author Alex Benini
 *
 *         Template for states of Core Engine
 */
abstract class State private[tuprolog] (protected val  runner : EngineRunner, protected val stateName: String) {

  protected lazy val _logger = LoggerFactory.getLogger(LoggerCategory.ENGINE_STATE)

  private[engine] def doJob(e: Engine)

  override def toString: String = {
    return stateName
  }
}