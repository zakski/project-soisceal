package com.szadowsz.gospel.core.event.io

import java.util.EventObject

import com.szadowsz.gospel.core.lib.UserContextInputStream

class ReadEvent(source : UserContextInputStream) extends EventObject(source) {

  private val _stream = source

  def getStream() = _stream
}