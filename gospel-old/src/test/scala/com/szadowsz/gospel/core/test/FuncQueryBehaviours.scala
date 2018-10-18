///**
//  *
//  */
//package com.szadowsz.gospel.core.test
//
//import com.szadowsz.gospel.core.{NoSolution, Theory}
//
//trait FuncQueryBehaviours {
//  this: TermsSpec =>
//
//  def noSolution(predicateName: String, args: Seq[String], theory : Option[String] = None) {
//
//    it(s"should not be solvable for args (${args.mkString(",")})") {
//      theory.foreach(t => prolog.setTheory(new Theory(t)))
//      val solution = prolog.solve(s"$predicateName(${args.mkString(",")}).")
//      solution.isSuccess shouldBe false
//      solution.isHalted shouldBe false
//      solution.isFailure shouldBe true
//    }
//  }
//
//  def noSolutionWithException(predicateName: String, args: Seq[String], exceptionMsg : String, theory : Option[String] = None) {
//
//    it(s"should halt with an exception for args (${args.mkString(",")})") {
//      val ex = getExceptionListener
//      prolog.addExceptionListener(ex)
//
//      theory.foreach(t => prolog.setTheory(new Theory(t)))
//      prolog.solve(s"$predicateName(${args.mkString(",")}).") match {
//        case no: NoSolution =>
//          no.isHalted shouldBe true
//          no.isFailure shouldBe true
//
//          no.getHaltingThrowable match {
//            case Some(e) => e.getMessage shouldBe exceptionMsg
//            case None => fail("Should have Exception")
//          }
//        case _ => fail("Should not be Solved")
//      }
//    }
//  }
//
//  def solution(predicateName: String, args: Seq[String], theory : Option[String] = None) {
//
//    it(s"should be solvable for args (${args.mkString(",")})") {
//      theory.foreach(t => prolog.setTheory(new Theory(t)))
//      val solution = prolog.solve(s"$predicateName(${args.mkString(",")}).")
//      solution.isSuccess shouldBe true
//    }
//  }
//
//  def solutionWithVars(predicateName: String, args: Seq[String], varName: String, varValue: String) {
//
//    it(s"should be solvable with correct variables for args (${args.mkString(",")})") {
//      val solution = prolog.solve(s"$predicateName(${args.mkString(",")}).")
//      solution.isSuccess shouldBe true
//
//      val result = solution.getVar(varName)
//      replaceUnderscore(result.toString) shouldBe varValue
//    }
//  }
//}
