package com.szadowsz.gospel.util.exception.data

import com.szadowsz.gospel.util.exception.LineNumbering

/**
 *  This exception means that an attempt was made to create an invalid Prolog term and failed at a specific point in
 *  the file it was being read from.
 *
 * @author Zakski : 31/08/2015.
 *
 * @param message the reason for the term being rejected
 * @param line line number
 * @param offset column number
 */
class TermParsingException(message: String, line: Int, offset: Int)
  extends InvalidTermException(message: String) with LineNumbering{

  override protected val _line: Int = line

  override protected val _column: Int = offset

  override def getMessage:String = message
}
