package com.szadowsz.gospel.core.listener

import com.szadowsz.gospel.core.event.io.ReadEvent
import java.util.EventListener

trait ReadListener extends EventListener {
  def readCalled(event: ReadEvent)
}