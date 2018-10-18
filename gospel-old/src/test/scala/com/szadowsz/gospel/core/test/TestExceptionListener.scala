package com.szadowsz.gospel.core.test

import com.szadowsz.gospel.core.event.interpreter.ExceptionEvent
import com.szadowsz.gospel.core.listener.ExceptionListener

class TestExceptionListener extends ExceptionListener {
  var exFound = false
  var exMsg = ""

  def onException(e: ExceptionEvent) {
    exFound = true
    exMsg = e.getMsg
  }
}
