package com.szadowsz.gospel.util.exception.lib

/**
 * Created by zszadowski on 14/05/2015.
 */
class LibraryNotFoundException(libName: String) extends InvalidLibraryException(libName) {

  override def getMessage() = "Library \"" + _libraryName + "\" Not Found."
}
