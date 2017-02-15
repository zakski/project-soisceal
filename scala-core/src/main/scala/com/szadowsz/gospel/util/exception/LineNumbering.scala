package com.szadowsz.gospel.util.exception

/**
 * Trait for the exception that have dealings with the parser. Records line and column offsets for where the error
 * occurred.
 *
 * @author Zakski : 31/08/2015.
 */
trait LineNumbering {

  protected val _line: Int

  protected val _column: Int

  /**
   * The line at which the error occurred
   *
   * @return line number
   */
  def getLine: Int = _line

  /**
   * The character at which the error occurred
   *
   * @return column number
   */
  def getColumn: Int = _column
}
