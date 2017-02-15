package com.szadowsz.gospel.util.exception.lib

import com.szadowsz.gospel.util.exception.LineNumbering

/**
 * @author Zakski : 31/08/2015.
 */
class LibraryBindException(libName : String, line: Int, offset: Int, cause : Throwable)
  extends InvalidLibraryException(libName,cause) with LineNumbering{

  override protected val _line: Int = line

  override protected val _column: Int = offset
}
