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
package com.szadowsz.gospel.core.exception.library

class LibraryInstantiationException(errorLib: String, message: String, cause: Throwable)
  extends LibraryException(errorLib, message, cause){

  def this(errorLib: String, cause: Throwable){
    this(errorLib,s"Failed to Instantiate Library $errorLib",cause)
  }

  def this(errorLib: String, msg : String){
    this(errorLib,msg,null)
  }
}