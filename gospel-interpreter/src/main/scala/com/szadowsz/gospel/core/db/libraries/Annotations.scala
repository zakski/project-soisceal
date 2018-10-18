package com.szadowsz.gospel.core.db.libraries

import scala.annotation.StaticAnnotation

case class clause(args: Int, aliases : String*) extends StaticAnnotation

class directive(args: Int, aliases : String*) extends clause(args,aliases:_*)

class functor(args: Int, aliases : String*) extends clause(args,aliases:_*)

class predicate(args: Int, aliases : String*) extends clause(args,aliases:_*)
