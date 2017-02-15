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
package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.db.theory.Theory

import java.io.InputStream
import java.util.concurrent.Callable

import com.szadowsz.gospel.core.engine.Solution
import com.szadowsz.gospel.core.event.OutputListener
import com.szadowsz.gospel.util.LoggerCategory
import org.slf4j.LoggerFactory

object Agent {
  private var defaultIDCounter = 0

  private def generateID = {
    defaultIDCounter+=1
    (defaultIDCounter-1).toString
  }
}


/**
 * Provides a prolog virtual machine embedded in a separate thread.
 * It needs a theory and optionally a goal.
 * It parses the theory, solves the goal and stops.
 *
 * @see alice.gospel.core.Prolog
 *
 */
class Agent private (id : String, pro : Prolog, theory: String,is: InputStream, goal: String)  extends Callable[Solution] with Runnable {
  private val _logger = LoggerFactory.getLogger(LoggerCategory.AGENT)

  private val _core: Prolog  = pro
  private val _theoryText: String = theory
  private val _theoryInputStream: InputStream = is
  private val _goalText: String = goal

  /**
   * Builds a prolog agent providing it a theory and a goal
   */
  def this(id : String, theory: String, goal: String) {
    this(id,new Prolog,theory,null,goal)
  }

  /**
   * Builds a prolog agent providing it a theory and a goal
   */
  def this(theory: String, goal: String) {
    this(Agent.generateID,theory,goal)
  }

  /**
   * Builds a prolog agent providing it a theory
   */
  def this(theory: String) {
    this(Agent.generateID,theory,null)
  }

  /**
   * Constructs the Agent with a theory provided
   * by an input stream and a goal
   */
  def this(id : String, is: InputStream, goal: String) {
    this(id,new Prolog,null,is,goal)
  }

  /**
   * Builds a prolog agent providing it a theory and a goal
   */
  def this(is: InputStream, goal: String) {
    this(Agent.generateID,is,goal)
  }

  /**
   * Builds a prolog agent providing it a theory
   */
  def this(is: InputStream) {
    this(Agent.generateID,is,null)
  }

  /**
   * Adds a listener to ouput events
   *
   * @param l the listener
   */
  def addOutputListener(l: OutputListener) {
    _core.addOutputListener(l)
  }

  /**
   * Removes a listener to ouput events
   *
   * @param l the listener
   */
  def removeOutputListener(l: OutputListener) {
    _core.removeOutputListener(l)
  }

  /**
   * Removes all output event listeners
   */
  def removeAllOutputListener {
    _core.removeAllOutputListeners
  }

  private def wrappedBody : Option[Solution] ={
    var result : Option[Solution] = None
    try {
      result = body
    } catch {
      case ex: Exception => {
        _logger.error("Agent " + id + ": invalid theory or goal.",ex)
      }
    }
    result
  }

  private def body : Option[Solution] ={
    var result : Option[Solution] = None
    if (_theoryText == null) {
      _logger.info("Agent " + id + ": Setting Theory to input stream")
      _core.setTheory(new Theory(_theoryInputStream))
    } else {
      _logger.info("Agent " + id + ": Setting Theory to text")
      _logger.debug("Agent " + id + ": Theory:\n" + _theoryText)
      _core.setTheory(new Theory(_theoryText))
    }

    if (_goalText != null) {
      _logger.info("Agent " + id + ": Attempting to solve " + _goalText)
      result = Some(_core.solve(_goalText))
      _logger.info("Agent " + id + ": Solved " + _goalText)
    }
    result
  }

  override def call(): Solution = wrappedBody.getOrElse(null)

  override def run(): Unit = wrappedBody

  def spawn {new Thread(this).run()}
}