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
package com.szadowsz.gospel.core

import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import com.szadowsz.gospel.core.data.{Number, Term}
import org.scalatest.matchers.{MatchResult, Matcher}

trait SolutionMatchers {
  
  val beSuccessful: Matcher[Solution] = Matcher { (s: Solution) =>
    MatchResult(
      s.isSuccess,
      s"${s.getQuery} was unsuccessful",
      s"${s.getQuery} was successful"
    )
  }
  
  val beUnsuccessful: Matcher[Solution] = Matcher { (s: Solution) =>
    MatchResult(
      !s.isSuccess,
      s"${s.getQuery} was successful",
      s"${s.getQuery} was unsuccessful"
    )
  }

  val beHalted: Matcher[Solution] = Matcher { (s: Solution) =>
    MatchResult(
      s.isHalted,
      s"${s.getQuery} was successful",
      s"${s.getQuery} was halted"
    )
  }
  
  def beWithinTolerance(v: Long, tol: Long): Matcher[Long] = be >= v - tol and be <= v + tol
  
  def beWithinTolerance(v: Double, tol: Double): Matcher[Double] = be >= v - tol and be <= v + tol
  
  def bePrologNumberWithinTolerance(v: Double, tol: Double): Matcher[Number] = {
    beWithinTolerance(v, tol) compose { (n: Number) => n.doubleValue }
  }
  
  def bePrologNumberWithinTolerance(v: Long, tol: Long): Matcher[Number] = {
    beWithinTolerance(v, tol) compose { (n: Number) => n.longValue }
  }
  
  def haveVarWithinTolerance(name: String, v: Long, tol: Long): Matcher[Solution] = {
    bePrologNumberWithinTolerance(v, tol) compose { (s: Solution) => s.getVar(name).asInstanceOf[Number] }
  }
  
  def haveVarWithinTolerance(name: String, v: Double, tol: Double): Matcher[Solution] = {
    bePrologNumberWithinTolerance(v, tol) compose { (s: Solution) => s.getVar(name).asInstanceOf[Number] }
  }
  
  def haveVar(name : String, value : Term): Matcher[Solution] = {
    be (value) compose { (s: Solution) => s.getVarOpt(name).value}
  }
}
