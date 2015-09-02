package com.szadowsz.gospel.util.exception.lib

/**
 * Created by zszadowski on 15/05/2015.
 */
class LibraryLoadException(libName : String, cause : Throwable) extends InvalidLibraryException(libName,cause) {


  override def getMessage() = "Library \"" + _libraryName + "\" Failed to Load."

}
