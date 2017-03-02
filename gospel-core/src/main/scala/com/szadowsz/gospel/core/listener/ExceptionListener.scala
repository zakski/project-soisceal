package com.szadowsz.gospel.core.listener

import com.szadowsz.gospel.core.event.interpreter.ExceptionEvent
import java.util.EventListener

trait ExceptionListener extends EventListener {
  def onException(e: ExceptionEvent)
}