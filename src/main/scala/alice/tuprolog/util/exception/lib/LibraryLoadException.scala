package alice.tuprolog.util.exception.lib

/**
 * Created by zszadowski on 15/05/2015.
 */
class LibraryLoadException(className : String, ex : Throwable) extends InvalidLibraryException(className,ex) {


  override def getMessage() = "Library \"" + _libraryName + "\" Failed to Load."

}
