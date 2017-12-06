package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.event.interpreter.ExceptionEvent
import com.szadowsz.gospel.core.listener.ExceptionListener
import org.scalatest.{BeforeAndAfter, Matchers, Suite}

/**
  * Created on 16/02/2017.
  */
trait BaseEngineSpec extends Matchers with BeforeAndAfter {
  this: Suite =>

  protected var prolog : PrologEngine = _

  protected def init(): PrologEngine

  protected def getExceptionListener = new ExceptionListener() {

    var exFound = false
    var exMsg = ""

    def onException(e: ExceptionEvent) {
      exFound = true
      exMsg = e.getMsg
    }
  }

  protected def replaceUnderscore(query: String): String = {
    var result: String = ""
    var trovato: Boolean = false
    for (i <- 0 until query.length) {
        if (query.charAt(i) == ',' || query.charAt(i) == ')' || query.charAt(i) == ']') trovato = false
        if (!trovato) result += (query.charAt(i) + "")
        if (query.charAt(i) == '_') trovato = true
    }
    result
  }

  before {
    prolog = init()
  }
}