/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2008-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package com.szadowsz.gospel.core.result

import com.szadowsz.gospel.core.engine.ExecutionResultType

import scala.language.implicitConversions
import scala.util.control.NonFatal

sealed abstract class Solution(protected val endState : ExecutionResultType.Value) extends Serializable {

  /** Returns `true` if the `Try` is a `Failure`, `false` otherwise.
   */
  final def isFailure: Boolean =  endState <= ExecutionResultType.FALSE

  /**
    * Checks if the solve request was halted
    *
    * @return true if the solve was successful
    */
  final def isSuccess: Boolean = endState > ExecutionResultType.FALSE

  /**
    * Checks if the solve request was halted
    *
    * @return true if the solve was successful
    */
  final def isHalted: Boolean = endState == ExecutionResultType.HALT

  /**
    * Checks if there are other possible solutions to this successful one
    *
    * @return true if a solution was found and there are other possible options
    */
  final def hasOpenAlternatives: Boolean = endState == ExecutionResultType.TRUE_CP

  /** Returns the value from this `Success` or the given `default` argument if this is a `Failure`.
   *
   * ''Note:'': This will throw an exception if it is not a success and default throws an exception.
   */
  def getOrElse[U >: T](default: => U): U = if (isSuccess) get else default

  /** Returns this `Try` if it's a `Success` or the given `default` argument if this is a `Failure`.
   */
  def orElse[U >: T](default: => Try[U]): Try[U] =
    try if (isSuccess) this else default
    catch {
      case NonFatal(e) => Failure(e)
    }

  /** Returns the value from this `Success` or throws the exception if this is a `Failure`.
   */
  def get: T

  /**
   * Applies the given function `f` if this is a `Success`, otherwise returns `Unit` if this is a `Failure`.
   *
   * ''Note:'' If `f` throws, then this method may throw an exception.
   */
  def foreach[U](f: T => U): Unit

  /**
   * Returns the given function applied to the value from this `Success` or returns this if this is a `Failure`.
   */
  def flatMap[U](f: T => Try[U]): Try[U]

  /**
   * Maps the given function to the value from this `Success` or returns this if this is a `Failure`.
   */
  def map[U](f: T => U): Try[U]

  /**
   * Converts this to a `Failure` if the predicate is not satisfied.
   */
  def filter(p: T => Boolean): Try[T]

  /** Creates a non-strict filter, which eventually converts this to a `Failure`
   *  if the predicate is not satisfied.
   *
   *  Note: unlike filter, withFilter does not create a new Try.
   *        Instead, it restricts the domain of subsequent
   *        `map`, `flatMap`, `foreach`, and `withFilter` operations.
   *
   * As Try is a one-element collection, this may be a bit overkill,
   * but it's consistent with withFilter on Option and the other collections.
   *
   *  @param p   the predicate used to test elements.
   *  @return    an object of class `WithFilter`, which supports
   *             `map`, `flatMap`, `foreach`, and `withFilter` operations.
   *             All these operations apply to those elements of this Try
   *             which satisfy the predicate `p`.
   */
  @inline final def withFilter(p: T => Boolean): WithFilter = new WithFilter(p)

  /** We need a whole WithFilter class to honor the "doesn't create a new
   *  collection" contract even though it seems unlikely to matter much in a
   *  collection with max size 1.
   */
  class WithFilter(p: T => Boolean) {
    def map[U](f:     T => U): Try[U]           = Try.this filter p map f
    def flatMap[U](f: T => Try[U]): Try[U]      = Try.this filter p flatMap f
    def foreach[U](f: T => U): Unit             = Try.this filter p foreach f
    def withFilter(q: T => Boolean): WithFilter = new WithFilter(x => p(x) && q(x))
  }

  /**
   * Applies the given function `f` if this is a `Failure`, otherwise returns this if this is a `Success`.
   * This is like `flatMap` for the exception.
   */
  def recoverWith[U >: T](f: PartialFunction[Throwable, Try[U]]): Try[U]

  /**
   * Applies the given function `f` if this is a `Failure`, otherwise returns this if this is a `Success`.
   * This is like map for the exception.
   */
  def recover[U >: T](f: PartialFunction[Throwable, U]): Try[U]

  /**
   * Returns `None` if this is a `Failure` or a `Some` containing the value if this is a `Success`.
   */
  def toOption: Option[T] = if (isSuccess) Some(get) else None

  /**
   * Transforms a nested `Try`, ie, a `Try` of type `Try[Try[T]]`,
   * into an un-nested `Try`, ie, a `Try` of type `Try[T]`.
   */
  def flatten[U](implicit ev: T <:< Try[U]): Try[U]

  /**
   * Inverts this `Try`. If this is a `Failure`, returns its exception wrapped in a `Success`.
   * If this is a `Success`, returns a `Failure` containing an `UnsupportedOperationException`.
   */
  def failed: Try[Throwable]

  /** Completes this `Try` by applying the function `f` to this if this is of type `Failure`, or conversely, by applying
   *  `s` if this is a `Success`.
   */
  def transform[U](s: T => Try[U], f: Throwable => Try[U]): Try[U] =
    try this match {
      case Success(v) => s(v)
      case Failure(e) => f(e)
    } catch {
      case NonFatal(e) => Failure(e)
    }

}

object Try {
  /** Constructs a `Try` using the by-name parameter.  This
   * method will ensure any non-fatal exception is caught and a
   * `Failure` object is returned.
   */
  def apply[T](r: => T): Try[T] =
    try Success(r) catch {
      case NonFatal(e) => Failure(e)
    }

}

final case class Failure[+T](exception: Throwable) extends Try[T] {
  def isFailure: Boolean = true
  def isSuccess: Boolean = false
  def recoverWith[U >: T](f: PartialFunction[Throwable, Try[U]]): Try[U] =
    try {
      if (f isDefinedAt exception) f(exception) else this
    } catch {
      case NonFatal(e) => Failure(e)
    }
  def get: T = throw exception
  def flatMap[U](f: T => Try[U]): Try[U] = this.asInstanceOf[Try[U]]
  def flatten[U](implicit ev: T <:< Try[U]): Try[U] = this.asInstanceOf[Try[U]]
  def foreach[U](f: T => U): Unit = ()
  def map[U](f: T => U): Try[U] = this.asInstanceOf[Try[U]]
  def filter(p: T => Boolean): Try[T] = this
  def recover[U >: T](rescueException: PartialFunction[Throwable, U]): Try[U] =
    try {
      if (rescueException isDefinedAt exception) {
        Try(rescueException(exception))
      } else this
    } catch {
      case NonFatal(e) => Failure(e)
    }
  def failed: Try[Throwable] = Success(exception)
}


final case class Success[+T](value: T) extends Solution {
  def isFailure: Boolean = false
  def isSuccess: Boolean = true
  def recoverWith[U >: T](f: PartialFunction[Throwable, Try[U]]): Try[U] = this
  def get = value
  def flatMap[U](f: T => Try[U]): Try[U] =
    try f(value)
    catch {
      case NonFatal(e) => Failure(e)
    }
  def flatten[U](implicit ev: T <:< Try[U]): Try[U] = value
  def foreach[U](f: T => U): Unit = f(value)
  def map[U](f: T => U): Try[U] = Try[U](f(value))
  def filter(p: T => Boolean): Try[T] = {
    try {
      if (p(value)) this
      else Failure(new NoSuchElementException("Predicate does not hold for " + value))
    } catch {
      case NonFatal(e) => Failure(e)
    }
  }
  def recover[U >: T](rescueException: PartialFunction[Throwable, U]): Try[U] = this
  def failed: Try[Throwable] = Failure(new UnsupportedOperationException("Success.failed"))
}
