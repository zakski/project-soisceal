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
package com.szadowsz.gospel.core.db.libraries.inbuilt

import com.szadowsz.gospel.core.data.Struct
import com.szadowsz.gospel.core.db.primitives.PrimitiveType
import com.szadowsz.gospel.core.{BaseEngineSpec, Interpreter}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuiltInSpec extends FunSpec with BaseEngineSpec {
  
  protected var builtIn: BuiltIn = _
  
  override protected def init(): Interpreter = new Interpreter()
  
  override def beforeEach() {
    super.beforeEach()
    builtIn = new BuiltIn(prolog)
  }
  
  describe("Built-In Library") {
    
    it("should be loaded successfully") {
      val prolog = new Interpreter(classOf[BuiltIn])
      prolog.getLibraryManager.getLibCount should be(1)
    }
    
    it("should return the current number of annotated methods") {
      val prims = builtIn.getPrimitives
      
      prims should have size 3
      prims.getOrElse(PrimitiveType.DIRECTIVE, Seq()) should have length 2
      prims.getOrElse(PrimitiveType.FUNCTOR, Seq()) should have length 21
      prims.getOrElse(PrimitiveType.PREDICATE, Seq()) should have length 17
    }
    
    // def at_halt TODO http://www.swi-prolog.org/pldoc/doc_for?object=at_halt/1
    it("allow for registration of a Goal to be run when the system halts")(pending)
    
    // def cancel_halt/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=cancel_halt/1
    it("allow for the cancellation of a system halt")(pending)
    
    // def cancel_halt/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=cancel_halt/1
    it("cancelling a halt should do nothing if the system is not halting")(pending)
    
    // def compiling TODO http://www.swi-prolog.org/pldoc/doc_for?object=compiling/0
    
    // def consult/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=consult/1
    it("read a file as a prolog source file")(pending)
    
    // def encoding/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=encoding/1
    
    // def ensure_loaded/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=ensure_loaded/1
    it("read a file as a prolog source file, if is not already loaded")(pending)
    
    // def expand_file_search_path/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=expand_file_search_path/2
    it("provide all possible expansions of a filename specification")(pending)
    
    // def file_search_path/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=file_search_path/2
    it("specify `path aliases'.")(pending)
    
    // def include/1 TODO  http://www.swi-prolog.org/pldoc/doc_for?object=include/1
    
    // def initialization/0 TODO http://www.swi-prolog.org/pldoc/doc_for?object=(initialization)/0
    
    // def initialization/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=(initialization)/1
    
    // def initialization/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=(initialization)/2
    
    // def load_files/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=load_files/1
    
    // def load_files/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=load_files/2
    
    // def make/0 TODO http://www.swi-prolog.org/pldoc/doc_for?object=make/0
    
    // def prolog_file_type/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=prolog_file_type/2
    
    // def prolog_load_context/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=prolog_load_context/2
    
    // def require/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=require/1
    
    // def source_file/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_file/1
    
    // def source_file/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_file/2
    
    // def source_file_property/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_file_property/2
    
    // def source_location/2 TODO http://www.swi-prolog.org/pldoc/doc_for?object=source_location/2
    
    // def unload_file/1 TODO http://www.swi-prolog.org/pldoc/doc_for?object=unload_file/1
  }
  
  // http://www.swi-prolog.org/pldoc/doc_for?object=use_module/1
  describe("Built-In use_module/1") {
    
    it("should be executed successfully") {
      val prolog = new Interpreter(classOf[BuiltIn])
      val directive = parseTerm(prolog, "use_module(library('plfile.pl'))").asInstanceOf[Struct]
      prolog.getPrimitiveManager.identifyDirective(directive) // get the primitive
      
      prolog.getLibraryManager.getLibCount should be(1)
      
      directive.getPrimitive.foreach(prim => prim.evalAsDirective(directive)) // run the primitive
      
      prolog.getLibraryManager.getLibCount should be(2)
    }
  }
  
  // http://www.swi-prolog.org/pldoc/doc_for?object=use_module/2
  describe("Built-In use_module/2") {
    
    it("should be executed successfully with an Empty List") {
      val prolog = new Interpreter(classOf[BuiltIn])
      val directive = parseTerm(prolog, "use_module(library('plfile.pl'),[])").asInstanceOf[Struct]
      prolog.getPrimitiveManager.identifyDirective(directive) // get the primitive
      
      prolog.getLibraryManager.getLibCount should be(1)
      val beforeDynamicCount = prolog.getTheoryManager.dynamicDBSize
      val beforeStaticCount = prolog.getTheoryManager.staticDBSize
      
      directive.getPrimitive.foreach(prim => prim.evalAsDirective(directive)) // run the primitive
      
      prolog.getLibraryManager.getLibCount should be(2)
      prolog.getTheoryManager.dynamicDBSize should be(beforeDynamicCount)
      prolog.getTheoryManager.staticDBSize should be(beforeStaticCount + 5)
    }
    
    it("should be executed successfully with all predicate") {
      val prolog = new Interpreter(classOf[BuiltIn])
      val directive = parseTerm(prolog, "use_module(library('plfile.pl'),'all')").asInstanceOf[Struct]
      prolog.getPrimitiveManager.identifyDirective(directive) // get the primitive
      
      prolog.getLibraryManager.getLibCount should be(1)
      val beforeDynamicCount = prolog.getTheoryManager.dynamicDBSize
      val beforeStaticCount = prolog.getTheoryManager.staticDBSize
      
      directive.getPrimitive.foreach(prim => prim.evalAsDirective(directive)) // run the primitive
      
      prolog.getLibraryManager.getLibCount should be(2)
      prolog.getTheoryManager.dynamicDBSize should be(beforeDynamicCount)
      prolog.getTheoryManager.staticDBSize should be(beforeStaticCount + 5)
    }
  
    it("should be executed successfully with Whitelist") {
      val prolog = new Interpreter(classOf[BuiltIn])
      val directive = parseTerm(prolog, "use_module(library('plfile.pl'),[woman/1])").asInstanceOf[Struct]
      prolog.getPrimitiveManager.identifyDirective(directive) // get the primitive
    
      prolog.getLibraryManager.getLibCount should be(1)
      val beforeDynamicCount = prolog.getTheoryManager.dynamicDBSize
      val beforeStaticCount = prolog.getTheoryManager.staticDBSize
    
      directive.getPrimitive.foreach(prim => prim.evalAsDirective(directive)) // run the primitive
    
      prolog.getLibraryManager.getLibCount should be(2)
      prolog.getTheoryManager.dynamicDBSize should be(beforeDynamicCount)
      prolog.getTheoryManager.staticDBSize should be(beforeStaticCount + 3)
   
      val term = parseTerm(prolog,"woman(_).")
  
      prolog.getTheoryManager.find(term) should have length 3
    }
  
    it("should be executed successfully with Whitelist with Rename") {
      val prolog = new Interpreter(classOf[BuiltIn])
      val directive = parseTerm(prolog, "use_module(library('plfile.pl'),[woman/1 as person])").asInstanceOf[Struct]
      prolog.getPrimitiveManager.identifyDirective(directive) // get the primitive
    
      prolog.getLibraryManager.getLibCount should be(1)
      val beforeDynamicCount = prolog.getTheoryManager.dynamicDBSize
      val beforeStaticCount = prolog.getTheoryManager.staticDBSize
    
      directive.getPrimitive.foreach(prim => prim.evalAsDirective(directive)) // run the primitive
    
      prolog.getLibraryManager.getLibCount should be(2)
      prolog.getTheoryManager.dynamicDBSize should be(beforeDynamicCount)
      prolog.getTheoryManager.staticDBSize should be(beforeStaticCount + 3)
  
      val term = parseTerm(prolog,"person(_).")
      
      prolog.getTheoryManager.find(term) should have length 3
    }
  
    it("should be executed successfully with Blacklist") {
      val prolog = new Interpreter(classOf[BuiltIn])
      val directive = parseTerm(prolog, "use_module(library('plfile.pl'),except([party/0]))").asInstanceOf[Struct]
      prolog.getPrimitiveManager.identifyDirective(directive) // get the primitive
    
      prolog.getLibraryManager.getLibCount should be(1)
      val beforeDynamicCount = prolog.getTheoryManager.dynamicDBSize
      val beforeStaticCount = prolog.getTheoryManager.staticDBSize
    
      directive.getPrimitive.foreach(prim => prim.evalAsDirective(directive)) // run the primitive
    
      prolog.getLibraryManager.getLibCount should be(2)
      prolog.getTheoryManager.dynamicDBSize should be(beforeDynamicCount)
      prolog.getTheoryManager.staticDBSize should be(beforeStaticCount + 4)
    
      val term = parseTerm(prolog,"party.")
    
      prolog.getTheoryManager.find(term) should have length 0
    }
  }
}
