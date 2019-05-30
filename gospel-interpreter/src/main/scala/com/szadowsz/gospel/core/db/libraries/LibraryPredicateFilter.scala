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

import com.szadowsz.gospel.core.data.Struct

object LibraryPredicateFilter {
  
  
  private def interpretName(struct : Struct): String = {
      struct.getPredicateIndicator match {
        case "//2" => struct.getTerm(0).toString + "/" + struct.getTerm(1).toString
        case _ => struct.getName
      }
  }
    
    
  private def interpretWhiteList(importList : Struct): Map[String,String] = {
    importList.getListIterator.map { t =>
      t.asInstanceOf[Struct] match {
        case as : Struct if as.getPredicateIndicator == "as/2" =>
          (interpretName(as.getTerm(0).asInstanceOf[Struct]),
            as.getTerm(1).asInstanceOf[Struct].getName
          )
        
        case any : Struct =>
          val name = interpretName(any)
          (name,any.getTerm(0).asInstanceOf[Struct].getName)
      }
    }.toMap
  }
  
  private def interpretBlackList(importList : Struct): Seq[String] = {
    importList.getListIterator.map { case any : Struct => interpretName(any)}.toList
  }
  
  private def interpretList(importList : Struct): (Map[String,String],Seq[String]) = {
    importList match {
      case empty if empty.isEmptyList =>
        (Map.empty,Nil)
        
      case all if all.getName == "all" =>
        (Map.empty,Nil)
        
      case whitelist if whitelist.isList =>
        (interpretWhiteList(whitelist),Nil)
        
      case blacklist if blacklist.getName == "except" =>
        (Map.empty,interpretBlackList(blacklist.getTerm(0).asInstanceOf[Struct]))
    }
  }
}

class LibraryPredicateFilter private() {
  
  var whitelist : Map[String,String] = Map.empty
  var blackList: Seq[String] = Nil
  
  def this(importList : Struct) {
    this()
    val (nWhitelist,nBlackList) = LibraryPredicateFilter.interpretList(importList)
    whitelist = nWhitelist
    blackList = nBlackList
  }
  
  def retainPredicate(predicateIndicator: String) : Boolean = {
    !blackList.contains(predicateIndicator) && (whitelist.isEmpty || whitelist.exists{ case (k,_) => k == predicateIndicator})
  }
  
  def mapKey(orgIndicator: String) : String = {
    whitelist.get(orgIndicator).map(nu =>  nu + orgIndicator.substring(orgIndicator.indexOf('/'))).getOrElse(orgIndicator)
  }
  
  def mapStruct(predicate: Struct) : Struct = {
    val nuName = whitelist.getOrElse(predicate.getPredicateIndicator,predicate.getName)
    if (nuName == predicate.getName){
      predicate
    } else {
      new Struct(nuName,predicate.getArity,predicate.getTermIterator.toList)
    }
  }
}
