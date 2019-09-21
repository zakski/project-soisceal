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

import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter}
import com.szadowsz.gospel.core.data.{Struct, Term}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class LibraryPredicateFilterSpec extends FunSpec with BaseEngineSpec {
  
  override protected def init(): Interpreter = new Interpreter
  
  describe("LibraryPredicateFilter interpretList Function") {
    
    it("should interpret an Empty List successfully") {
      val filter = new LibraryPredicateFilter(new Struct)
      
      filter.retainPredicate("women/1") should be (true)
      filter.retainPredicate("women/2") should be (true)
    }
   
    it("should interpret a List as a Whitelist successfully") {
      val importList = parseTerm("[women/1]).").asInstanceOf[Struct]
      val filter = new LibraryPredicateFilter(importList)
    
      filter.retainPredicate("women/1") should be (true)
      filter.retainPredicate("women/2") should be (false)
    }
   
    it("should interpret a renamed predicate on the Whitelist successfully") {
      val importList = parseTerm("[women/1 as person]).").asInstanceOf[Struct]
      val filter = new LibraryPredicateFilter(importList)
    
      filter.retainPredicate("women/1") should be (true)
      filter.retainPredicate("women/2") should be (false)
      
      filter.mapKey("women/1") should be ("person/1")
    }
    
    it("should interpret a Blacklist successfully") {
      val importList = parseTerm("except([women/1])).").asInstanceOf[Struct]
      val filter = new LibraryPredicateFilter(importList)
    
      filter.retainPredicate("women/1") should be (false)
      filter.retainPredicate("women/2") should be (true)
    }
  }
}
