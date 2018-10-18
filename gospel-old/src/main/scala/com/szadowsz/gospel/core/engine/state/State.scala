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
package com.szadowsz.gospel.core.engine.state

import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Template for states of Core Engine. Not for consumption outside of the core engine package.
  *
  */
private[engine] trait State {

  /**
    * Use one logger category for all states.
    */
  protected lazy val logger : Logger = LoggerFactory.getLogger(classOf[State])

  /**
    * the runner the state occurred in.
    */
  protected val runner : EngineRunner

  /**
    * the name of the engine state.
    */
  protected val stateName: String


  def doJob(e: Engine) : Unit

  override def toString: String = stateName
}