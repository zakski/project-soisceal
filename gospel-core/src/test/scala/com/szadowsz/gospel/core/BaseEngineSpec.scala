package com.szadowsz.gospel.core

import org.scalatest.{BeforeAndAfter, Matchers, Suite}

/**
  * Created on 16/02/2017.
  */
trait BaseEngineSpec extends Matchers with BeforeAndAfter {
  this: Suite =>

  protected var prolog : PrologEngine = _

  protected def init(): PrologEngine

  before {
    prolog = init()
  }
}