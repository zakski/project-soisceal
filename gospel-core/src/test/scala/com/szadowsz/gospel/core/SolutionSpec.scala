package com.szadowsz.gospel.core

import com.szadowsz.gospel.core.data.{Int, Struct, Term, Var}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolutionSpec extends FlatSpec with BaseEngineSpec {
  override protected def init(): PrologEngine = new PrologEngine()

  behavior of "Solution Object"

  it should "be able to get the query made from the solution" in {
    var query = new Struct("is", new Var("X"), new Struct("+", new Int(1), new Int(2)))
    var result = prolog.solve(query)
    result.isSuccess shouldBe true
    result.getQuery shouldBe query

    query = new Struct("functor", new Struct("p"), new Var("Name"), new Var("Arity"))
    result = prolog.solve(query)
    result.isSuccess shouldBe true
    result.getQuery shouldBe query
  }
}
