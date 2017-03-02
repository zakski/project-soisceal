package com.szadowsz.gospel.core.event.interpreter

import com.szadowsz.gospel.core.PrologEngine

@SerialVersionUID(1L)
final class ExceptionEvent(source: PrologEngine, message: String) extends PrologEvent(source) {
  private val msg : String = message

  def getMsg: String = msg
}