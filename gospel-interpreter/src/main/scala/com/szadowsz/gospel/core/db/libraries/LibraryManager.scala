/**
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
package com.szadowsz.gospel.core.db.libraries

import java.util.concurrent.ConcurrentHashMap

import com.szadowsz.gospel.core.Interpreter
import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.db.primitives.PrimitivesManager
import com.szadowsz.gospel.core.db.theory.TheoryManager
import com.szadowsz.gospel.core.exception.library.{InvalidLibraryException, LibraryInstantiationException, LibraryNotFoundException}
import com.szadowsz.gospel.core.exception.{InvalidTheoryException, PrologException}
import com.szadowsz.gospel.core.parser.PrologSrcFinder
import io.github.classgraph.ClassGraph
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.concurrent
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

private[core] class LibraryManager(private val wam: Interpreter) {
  
  private val logger: Logger = LoggerFactory.getLogger(classOf[LibraryManager])

  private lazy implicit val primManager: PrimitivesManager = wam.getPrimitiveManager

  private lazy val thManager: TheoryManager = wam.getTheoryManager

  private val libs: concurrent.Map[String, Library] = new ConcurrentHashMap[String, Library]().asScala

  @throws(classOf[LibraryInstantiationException])
  private def instantiateLibrary(libName : String, func : Interpreter => Library):Library = {
    try {
      func(wam)
    } catch {
      case ie: InstantiationException => throw new LibraryInstantiationException(libName, s"Library $libName is abstract")

      case nsme: NoSuchMethodException =>
        throw new LibraryInstantiationException(libName, s"Library $libName does not have a valid constructor",nsme)

      case iae: IllegalAccessException =>
        throw new LibraryInstantiationException(libName, s"Failed to access library $libName", iae)

      case NonFatal(ex) =>
        throw new LibraryInstantiationException(libName, ex)
    }
  }

  /**
    * Binds a library by adding its clauses to the database and carrying out tis directives.
    *
    * @param lib the library object
    * @param importList the list of predicates to be included or excluded
    * @return the reference to the Library just loaded
    * @throws InvalidLibraryException if the Library instance is incorrectly configured and cannot be bound.
    */
  @throws(classOf[InvalidLibraryException])
  private def bindLibrary(lib: Library, importList : Struct): Unit = {
    try {
      libs += lib.getName -> lib
      val filter = new LibraryPredicateFilter(importList)
      primManager.bindLibrary(lib, filter)
      lib.getTheory match {
        case Some(th) =>
          thManager.consult(th, false, filter, Some(lib.getName))
          thManager.validateStack()
        case None =>
          logger.debug("No Theory Object Detected for {} Library.", lib.getName)
      }
      // in current theory there could be predicates and functors which become built-ins after lib loading.
      thManager.rebindPrimitives()
    } catch {
      case ite: InvalidTheoryException => new InvalidLibraryException(ite, lib.getName)
      case ile: InvalidLibraryException => throw ile // do not stack InvalidLibraryExceptions for readability
      case NonFatal(ex) => throw new InvalidLibraryException(ex, lib.getName, "Failed to Bind Library")
    }
  }

  /**
    * Loads a specific instance of a Library Class.
    *
    * If a library of the same class is already present, a warning event is notified. Then, the current instance of that
    * library is discarded, and the new instance gets loaded.
    *
    * @param lib the Java object of the library to be loaded
    * @throws InvalidLibraryException if we cannot bind the library to the engine.
    * @return the library object now loaded in the interpreter
    */
  @throws(classOf[InvalidLibraryException])
  private def loadLibrary(lib: Library): Library = {
    loadLibrary(lib,new Struct)
  }
  
  /**
    * Loads a specific instance of a Library Class.
    *
    * If a library of the same class is already present, a warning event is notified. Then, the current instance of that
    * library is discarded, and the new instance gets loaded.
    *
    * Allows for a whitelist/blacklist of predicates to be included
    *
    * @param lib the Java object of the library to be loaded
    * @param importList the list of predicates to be included or excluded
    * @throws InvalidLibraryException if we cannot bind the library to the engine.
    * @return the library object now loaded in the interpreter
    */
  @throws(classOf[InvalidLibraryException])
  private def loadLibrary(lib: Library, importList : Struct): Library = {
    libs.get(lib.getName) match {
      case Some(oldLib) =>
        logger.warn("{} Library already loaded.", oldLib.getName)
        unloadLibrary(oldLib.getName)
        logger.warn("Old Instance of {} Library removed.", lib.getName)
      
      case None => // nop
    }
    bindLibrary(lib,importList)
    logger.info("Loaded Library {}", lib)
    lib
  }

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param clazz the library class.
    * @throws LibraryInstantiationException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[LibraryInstantiationException])
  private def instantiateLibraryFromClass(clazz: Class[_ <: Library]): Library = {
    instantiateLibrary(clazz.getSimpleName, w => clazz.getDeclaredConstructor(classOf[Interpreter]).newInstance(w))
  }

  private def instantiateLibraryUsingReflection(identifier: String): Library = {

    val result = new ClassGraph().enableClassInfo().scan()
    val libs = result.getSubclasses(classOf[Library].getName).asScala
    val namedLib = libs.filter(_.getSimpleName  == identifier)

    if (namedLib.length == 1){
      instantiateLibraryFromClass(namedLib.head.loadClass().asInstanceOf[Class[Library]])
    } else {
      val it = libs.filter(!_.isAbstract).iterator
      var lib : Try[Library] = Failure(new LibraryNotFoundException(identifier))
      while (!lib.toOption.exists(_.getName == identifier) && it.hasNext) {
        val next = Try(instantiateLibraryFromClass(it.next().loadClass().asInstanceOf[Class[Library]]))
        lib = if (next.toOption.exists(_.getName == identifier)) next else lib
      }
      lib.get
    }
  }


  private def instantiateLibraryFromResource(identifier: String): Library = {
    try {
      val res = PrologSrcFinder.searchForTheories(identifier).head
      new ResourceLibrary(wam, res)
    } catch {
      case lie: LibraryInstantiationException => throw lie
      case NonFatal(nf) => throw new LibraryNotFoundException(identifier,nf)
    }
  }

  /**
    * Loads a library.
    *
    * If a library with the same name is already present, a warning event is notified and the request is ignored.
    *
    * @param className the name of the Java class containing the library to be loaded.
    * @throws LibraryInstantiationException if we cannot create a valid library.
    * @return the reference to the Library just loaded.
    */
  @throws(classOf[LibraryInstantiationException])
  private def instantiateLibraryFromClassName(className: String): Library = {
    try {
      instantiateLibraryFromClass(Class.forName(className).asInstanceOf[Class[Library]])
    } catch {
      case lie : LibraryInstantiationException => throw lie
      case cnfe : ClassNotFoundException => throw new LibraryNotFoundException(className,cnfe)
    }
  }

  /**
    * Method to load a library/module into the Prolog Engine.
    *
    * This method attempts to find the library based on the identifier from several locations in the following order:
    *
    * 1. Attempts to match the identifier to a valid library class name
    * 2. Attempts to find a file/classpath resource that matches the identifier name / pattern
    * 3. Attempts to find a valid library class name based on it's simplified
    *
    * @param clazz the Java class that defines the library to be loaded.
    * @throws InvalidLibraryException if we cannot find and create a valid library.
    * @return the library object now loaded in the interpreter
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibraryFromClass(clazz: Class[_ <: Library]): Library = {
   try {
      loadLibrary(Try(instantiateLibraryFromClass(clazz)).get)
    } catch {
      case ile: InvalidLibraryException => throw ile
      case NonFatal(nf) => throw new InvalidLibraryException(nf, clazz.getSimpleName, "Failed to Load Library")
    }
  }

  /**
    * Method to load a library/module into the Prolog Engine.
    *
    * This method attempts to find and instantiate the library based on the identifier from several locations in the
    * following order:
    *
    * 1. Attempts to match the identifier to a valid library class name
    * 2. Attempts to find a file/classpath resource that matches the identifier name / pattern
    * 3. Attempts to find a valid library class name based on it's simplified
    *
    * After that it will attempt to load the library into the db.
    *
    * @param identifier the name / matching pattern of the library to be loaded.
    * @throws InvalidLibraryException if we cannot find and create a valid library.
    * @return the library object now loaded in the interpreter
    */
  @throws(classOf[InvalidLibraryException])
  def loadLibrary(identifier: String): Library = {
    def recovery( f: () => Library) : PartialFunction[Throwable, Try[Library]]  = {
      case x : LibraryNotFoundException => Try(f())
    }

    val lib = Try(instantiateLibraryFromClassName(identifier))
      .recoverWith(recovery(() => instantiateLibraryFromResource(identifier)))
      .recoverWith(recovery(() => instantiateLibraryUsingReflection(identifier)))

    try {
      loadLibrary(lib.get)
    } catch {
      case ile: InvalidLibraryException => throw ile
      case NonFatal(nf) => throw new InvalidLibraryException(nf, identifier, "Failed to Load Library")
    }
  }
  
  /**
    * Method to load a library/module into the Prolog Engine.
    *
    * This method attempts to find and instantiate the library based on the identifier from several locations in the
    * following order:
    *
    * 1. Attempts to match the identifier to a valid library class name
    * 2. Attempts to find a file/classpath resource that matches the identifier name / pattern
    * 3. Attempts to find a valid library class name based on it's simplified
    *
    * After that it will attempt to load the library into the db.
    *
    * @param identifier the name / matching pattern of the library to be loaded.
    * @throws InvalidLibraryException if we cannot find and create a valid library.
    * @return the library object now loaded in the interpreter
    */
  def loadLibrary(identifier: String, importList: Struct): Unit = {
    def recovery( f: () => Library) : PartialFunction[Throwable, Try[Library]]  = {
      case x : LibraryNotFoundException => Try(f())
    }
  
    val lib = Try(instantiateLibraryFromClassName(identifier))
      .recoverWith(recovery(() => instantiateLibraryFromResource(identifier)))
      .recoverWith(recovery(() => instantiateLibraryUsingReflection(identifier)))
  
    try {
      loadLibrary(lib.get, importList)
    } catch {
      case ile: InvalidLibraryException => throw ile
      case NonFatal(nf) => throw new InvalidLibraryException(nf, identifier, "Failed to Load Library")
    }
  }

  /**
    * Unloads a previously loaded library.
    *
    * @param name the name of the library to be unloaded.
    */
  def unloadLibrary(name: String): Unit = {
    libs.get(name) match {
      case Some(lib) =>
        lib.dismiss()
        primManager.unbindLibrary(lib)
        libs.remove(name)
        thManager.unloadLibrary(name)
        thManager.rebindPrimitives()
      case None =>
        logger.warn("Library {} not loaded.", name)
    }
  }

  private[libraries] def getLibCount: Int ={
    libs.size
  }
}
