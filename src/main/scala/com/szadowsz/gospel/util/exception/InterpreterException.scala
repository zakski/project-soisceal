package com.szadowsz.gospel.util.exception

/**
 * class to wrap all the internal prolog exceptions that we are anticipating.
 *
 * Useful for when we are mapping the exception to another type also.
 *
 * @author Zakski : 26/05/2015.
 *
 * @param message description of the exception
 * @param cause cause of the exception
 */
class InterpreterException(message: String, cause: Throwable) extends Exception(message, cause) {


  /**
   * Constructs a new exception with a detail message and null as its cause.
   *
   * @param message description of the exception
   */
  def this(message: String) {
    this(message, null)
  }


  /**
   * Constructs a new exception with a cause and null as its detail message.
   *
   * @param cause cause of the exception
   */
  def this(cause: Throwable) {
    this(null, cause)
  }

  /**
   * Constructs a new exception with null as its detail message and cause.
   */
  def this() {
    this(null, null)
  }
}
