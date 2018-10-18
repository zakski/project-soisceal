package com.szadowsz.gospel.core.event.io

import java.util.EventObject

import alice.tuprolog.lib.UserContextInputStream

class ReadEvent(source : UserContextInputStream) extends EventObject(source) {

  private val stream = source

  def getStream : UserContextInputStream = stream
}