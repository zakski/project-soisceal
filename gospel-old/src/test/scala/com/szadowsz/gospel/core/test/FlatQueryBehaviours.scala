///**
//  *
//  */
//package com.szadowsz.gospel.core.test
//
//import com.szadowsz.gospel.core.{NoSolution, Theory}
//
//trait FlatQueryBehaviours {
//  this: TermSpec =>
//
//  def noSolution(predicateName: String, args: Seq[String], theory: Option[String] = None) {
//
//    it should s"not be solvable for args (${args.mkString(",")})" in {
//      theory.foreach(t => prolog.setTheory(new Theory(t)))
//      val solution = prolog.solve(s"$predicateName(${args.mkString(",")}).")
//      solution.isSuccess shouldBe false
//      solution.isHalted shouldBe false
//      solution.isFailure shouldBe true
//    }
//  }
//
//  def noSolutionWithException(predicateName: String, args: Seq[String], exceptionMsg: String, theory: Option[String] = None) {
//
//    it should s"halt with an exception for args (${args.mkString(",")})" in {
//      val ex = getExceptionListener
//      prolog.addExceptionListener(ex)
//
//      theory.foreach(t => prolog.setTheory(new Theory(t)))
//      prolog.solve(s"$predicateName(${args.mkString(",")}).") match {
//        case no: NoSolution =>
//           no.isHalted shouldBe true
//           no.isFailure shouldBe true
//
//           no.getHaltingThrowable match {
//             case Some(e) => e.getMessage shouldBe exceptionMsg
//             case None => fail("Should have Exception")
//           }
//        case _ => fail("Should not be Solved")
//      }
//    }
//  }
//
//  def solution(predicateName: String, args: Seq[String], theory: Option[String] = None) {
//
//    it should s" be solvable for args (${args.mkString(",")})" in {
//      theory.foreach(t => prolog.setTheory(new Theory(t)))
//      val solution = prolog.solve(s"$predicateName(${args.mkString(",")}).")
//      solution.isSuccess shouldBe true
//    }
//  }
//
//  def solutionWithVars(predicateName: String, args: Seq[String], varName: String, varValue: String) {
//
//    it should s"be solvable with correct variables for args (${args.mkString(",")})" in {
//      val solution = prolog.solve(s"$predicateName(${args.mkString(",")}).")
//      solution.isSuccess shouldBe true
//
//      val result = solution.getVar(varName)
//      replaceUnderscore(result.toString) shouldBe varValue
//    }
//  }
//}
