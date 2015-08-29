package com.szadowsz.gospel.core.data.socket

import java.net.InetAddress
import java.{util => ju}

import com.szadowsz.gospel.core.data.{Term, Var}

@SerialVersionUID(1L)
abstract class AbstractSocket extends Term {
  def isClientSocket: Boolean

  def isServerSocket: Boolean

  def isDatagramSocket: Boolean

  def getSocket: AnyRef

  def getAddress: InetAddress

  override def isEmptyList: Boolean = false

  override def isAtomic: Boolean = false

  override def isCompound: Boolean = false

  override def isAtom: Boolean = false

  override def isList: Boolean = false


  override def isGround: Boolean = false

  override def isGreater(t: Term): Boolean = false


  override def isGreaterRelink(t: Term, vorder: ju.ArrayList[String]): Boolean = false

  override def isEqual(t: Term): Boolean = false

  override def getTerm: Term = this

  override def free {}

  private[gospel] override def resolveTerm(count: Long): Long = count


  /**
   * gets a copy (with renamed variables) of the term.
   * <p>
   * the list argument passed contains the list of variables to be renamed
   * (if empty list then no renaming)
   */
  private[gospel] override def copy(vMap: ju.AbstractMap[Var, Var], idExecCtx: Int): Term = this


  /**
   * gets a copy of the term.
   */
  private[gospel] override def copy(vMap: ju.AbstractMap[Var, Var], substMap: ju.AbstractMap[Term, Var]): Term = this
}