package alice.tuprolog.core.event.io

import java.util.EventObject
import alice.tuprolog.lib.UserContextInputStream

class ReadEvent(source : UserContextInputStream) extends EventObject(source) {

  private val _stream = source

  def getStream() = _stream
}