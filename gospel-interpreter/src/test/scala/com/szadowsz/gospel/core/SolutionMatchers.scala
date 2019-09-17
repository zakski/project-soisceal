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

import org.scalatest.matchers.{MatchResult, Matcher}

trait SolutionMatchers {
  
  class QueryShouldSucceedMatcher() extends Matcher[Solution] {
    
    def apply(left: Solution): MatchResult = {
      val query = left.getQuery
      MatchResult(
        left.isSuccess,
        s"$query was unsuccessful",
        s"$query was successful"
      )
    }
  }
  
  def beSuccessful(): Matcher[Solution] = new QueryShouldSucceedMatcher()
  
  class QueryShouldFailMatcher() extends Matcher[Solution] {
    
    def apply(left: Solution): MatchResult = {
      val query = left.getQuery
      MatchResult(
        !left.isSuccess,
        s"$query was successful",
        s"$query was unsuccessful"
      )
    }
  }
  
  def beUnsuccessful(): Matcher[Solution] = new QueryShouldFailMatcher()
  
  class QueryShouldHaltMatcher() extends Matcher[Solution] {
    
    def apply(left: Solution): MatchResult = {
      val query = left.getQuery
      MatchResult(
        left.isHalted,
        s"$query was halted",
        s"$query was not halted"
      )
    }
  }
  
  def beHalted(): Matcher[Solution] = new QueryShouldHaltMatcher()
}