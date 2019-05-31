package com.szadowsz.gospel.core.engine.state
import com.szadowsz.gospel.core.engine.Executor

class ExceptionState extends State {
  
  /**
    * the name of the engine state.
    */
  override protected val stateName: String = "Exception"
  
  override def doJob(e: Executor): Unit = ???
}
