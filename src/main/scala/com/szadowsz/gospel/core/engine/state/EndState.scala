/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.szadowsz.gospel.core.engine.state

import java.util.{ArrayList, List, StringTokenizer}

import com.szadowsz.gospel.core.Prolog
import com.szadowsz.gospel.core.data.{Struct, Term, Var}
import com.szadowsz.gospel.core.engine.{Engine, EngineRunner}

import scala.collection.JavaConverters._

/**
 * @author Alex Benini
 *
 *         End state of demostration.
 *
 * Constructor
 * @param end Terminal state of computation
 */
class EndState(runner : EngineRunner, end: scala.Int) extends State(runner,"End") {
  private var endState: scala.Int = end
  private var goal: Struct = null
  private var vars: List[Var] = null
  private var setOfCounter = 0

  def getResultDemo: scala.Int = {
    endState
  }

  def getResultGoal: Struct = {
    goal
  }

  def getResultVars: List[Var] = {
    vars
  }

  private[gospel] override def doJob(e: Engine) {
    vars = new ArrayList[Var]
    goal = e.startGoal.copyResult(e.goalVars, vars).asInstanceOf[Struct]
    if (this.endState == EngineRunner.TRUE || this.endState == EngineRunner.TRUE_CP)
      relinkVar(e)
  }

  private def solve(theA1: Term, a: Array[AnyRef], theInitGoalBag: Term): Term = {
    var initGoalBag = theInitGoalBag
    var a1 = theA1
    while (a1.isInstanceOf[Struct] && (a1.asInstanceOf[Struct]).getArity > 0) {
      var a10: Term = (a1.asInstanceOf[Struct]).getArg(0)

      if (a10.isInstanceOf[Var]) {
        initGoalBag = findVarName(a10, a, a1, 0)
      } else if (a10.isInstanceOf[Struct]) {
        var a100: Term = null
        a100 = (a10.asInstanceOf[Struct]).getArg(0)
        a10 = solve(a100, a, a10)
        (a1.asInstanceOf[Struct]).setArg(0, a10)
      }
      var a11: Term = null
      if ((a1.asInstanceOf[Struct]).getArity > 1) a11 = (a1.asInstanceOf[Struct]).getArg(1)
      a1 = a11
    }

    if (a1.isInstanceOf[Var]) {
      initGoalBag = findVarName(a1, a, initGoalBag, 0)
    }

    initGoalBag
  }

  private def findVarName(theLink: Term, a: Array[AnyRef], initGoalBag: Term, pos: scala.Int): Term = {
    var findName: Boolean = false
    var link = theLink
    while (link != null && link.isInstanceOf[Var] && !findName) {
      var y = 0
      while (!findName && y < a.length) {
        var gVar : Term = a(y).asInstanceOf[Var]

        while (!findName && gVar != null && gVar.isInstanceOf[Var]) {

          if ((gVar.asInstanceOf[Var]).getName.toString.compareTo((link.asInstanceOf[Var]).getName.toString) == 0) {
            (initGoalBag.asInstanceOf[Struct]).setArg(pos, new Var((a(y).asInstanceOf[Var]).getName.toString))
            findName = true
          }

          gVar = (gVar.asInstanceOf[Var]).getLink
        }
        y += 1
      }
      link = (link.asInstanceOf[Var]).getLink
    }
    return initGoalBag
  }

  private def initQueryForRelink(e : Engine) ={
    var query: Term = e.query
    if ((query.asInstanceOf[Struct]).getName == ";") {
      var query_temp: Struct = (query.asInstanceOf[Struct]).getArg(0).asInstanceOf[Struct]
      if (((query_temp.asInstanceOf[Struct]).getName == "setof") && setOfCounter == 0) {
        query = query_temp
        this.setOfCounter += 1
      }
      else {
        query_temp = (query.asInstanceOf[Struct]).getArg(1).asInstanceOf[Struct]
        if ((query_temp.asInstanceOf[Struct]).getName == "setof")
          query = query_temp
      }
    }
    query
  }

  private def getInitGoalBagInfo(p : Prolog, a: Array[AnyRef], tg : Term, initBag: Term): (Term,Boolean,Boolean)= {
    var tgoal = tg
    var initGoalBag: Term = null
    var find = false
    var findSamePredicateIndicator = false
    while (tgoal.isInstanceOf[Var] && (tgoal.asInstanceOf[Var]).getLink != null) {
      tgoal = (tgoal.asInstanceOf[Var]).getLink
      if (tgoal.isInstanceOf[Struct]) {
        tgoal = (tgoal.asInstanceOf[Struct]).getArg(1)
        if (p.unify(tgoal, (initBag.asInstanceOf[Var]).getLink)) {
          return (tgoal,true,true) // initGoalBag, find, findSamePredicateIndicator
        }
        else if ((initBag.asInstanceOf[Var]).getLink.isInstanceOf[Struct]) {
          val s: Struct = (initBag.asInstanceOf[Var]).getLink.asInstanceOf[Struct]
          if (tgoal.isInstanceOf[Struct] && s.getPredicateIndicator.toString.compareTo((tgoal.asInstanceOf[Struct]).getPredicateIndicator.toString) == 0) {
            findSamePredicateIndicator = true
            find = true
            initGoalBag = tgoal
          }
        }
        // get initBagGoal and replace its names with the names of variables goal
        if ((find && initGoalBag.isInstanceOf[Struct]) || (findSamePredicateIndicator && initGoalBag.isInstanceOf[Struct])) {
          val a0: Term = (initGoalBag.asInstanceOf[Struct]).getArg(0)
          var a1: Term = (initGoalBag.asInstanceOf[Struct]).getArg(1)
          if (a0.isInstanceOf[Var]) {
            val link: Term = a0
            initGoalBag = findVarName(link, a, initGoalBag, 0)
          }
          a1 = solve(a1, a, a1)
          (initGoalBag.asInstanceOf[Struct]).setArg(1, a1)
        }
      }
    }
    (initGoalBag,find,findSamePredicateIndicator)
  }

  private def handleLeft(initGoalBagList: ArrayList[Term], initGoalBagListVar: ArrayList[String], left: ArrayList[Term], left_temp: ArrayList[Term], m : scala.Int) {
    var k = 0
    var break = false
    while (k < left.size && !break) {
      if (initGoalBagList.get(m).isGreaterRelink(left.get(k), initGoalBagListVar)) {
        left_temp.add(left.get(k))
      }
      else {
        left_temp.add(initGoalBagList.get(m))
        break = true
      }
      k += 1
    }
    if (k == left.size)
      left_temp.add(initGoalBagList.get(m))
  }

  private def handleRight(left: ArrayList[Term], left_temp: ArrayList[Term], right: ArrayList[Term], right_temp: ArrayList[Term]) {
    var y = 0
    while (y < left.size) {
      var search: Boolean = false
      for (r <- 0 until left_temp.size) {
        if (left_temp.get(r).toString == left.get(y).toString)
          search = true
      }
      if (search) {
        left.remove(y)
        y -= 1
      } else {
        right_temp.add(left.get(y))
        left.remove(y)
        y -= 1
      }
      y += 1
    }
    y = 0
    while (y < right.size) {
      right_temp.add(right.get(y))
      right.remove(y)
      y -= 1
      y += 1
    }
  }

  private def handleSetOf(initGoalBagList: ArrayList[Term]) ={
    val initGoalBagListVar: ArrayList[String] = new ArrayList[String]
    val initGoalBagListOrdered: ArrayList[Term] = new ArrayList[Term]

    for (m <- 0 until initGoalBagList.size) {
      if (initGoalBagList.get(m).isInstanceOf[Var])
        initGoalBagListVar.add((initGoalBagList.get(m).asInstanceOf[Var]).getName)
    }

    val left: ArrayList[Term] = new ArrayList[Term]
    val left_temp: ArrayList[Term] = new ArrayList[Term]
    left.add(initGoalBagList.get(0))
    val right: ArrayList[Term] = new ArrayList[Term]
    val right_temp: ArrayList[Term] = new ArrayList[Term]
    for (m <- 1 until initGoalBagList.size) {
      handleLeft(initGoalBagList, initGoalBagListVar, left, left_temp, m )

      handleRight(left, left_temp, right, right_temp)

      right.addAll(right_temp)
      right_temp.clear
      left.addAll(left_temp)
      left_temp.clear
    }
    // recreate the structure of the initial goal from the ordered list
    initGoalBagListOrdered.addAll(left)
    initGoalBagListOrdered.addAll(right)
    initGoalBagListOrdered
  }

  private def rename(bagVarName: String, goalSolution: Var) {
    for (j <- 0 until vars.size) {
      val vv: Var = vars.get(j)
      if (vv.getOriginalName == bagVarName) {
        val solVar: Var = varValue2(goalSolution)
        solVar.setName(vv.getOriginalName)
        solVar.rename(0, 0)
        vars.set(j, solVar)
        return //todo: break is not supported
      }
    }
  }

    private def updateInitBags(initBag: Term, initGoalBag: Term, initGoalBagListOrdered: ArrayList[Term]) ={
      var initGoalBagTemp = initGoalBag.asInstanceOf[Struct]
      val t1: Array[Term] = initGoalBagListOrdered.asScala.toArray

      val s: Struct = new Struct(initGoalBagTemp.getName, t1)
      val initBagList: ArrayList[Term] = new ArrayList[Term]
      var initBagTemp: Struct = (initBag.asInstanceOf[Var]).getLink.asInstanceOf[Struct]
      while (initBagTemp.getArity > 0) {
        val t0: Term = initBagTemp.getArg(0)
        initBagList.add(t0)
        val t2: Term = initBagTemp.getArg(1)
        if (t2.isInstanceOf[Struct]) {
          initBagTemp = t2.asInstanceOf[Struct]
        }
      }
      val termNoOrd: Array[Term] = initBagList.asScala.toArray
      (s, new Struct(initGoalBagTemp.getName, termNoOrd))
    }

    private def relinkVar(e: Engine) { // TODO Further explore refactoring
      if (runner.getEngineMan.getRelinkVar) {
        val bag: ArrayList[Term] = runner.getEngineMan.getBagOFres
        var initBag: Term = runner.getEngineMan.getBagOFbag
        val BOgoal: Term = runner.getEngineMan.getBagOFgoal
        var tgoal: Term = BOgoal
        var initGoalBag: Term = null
        var find: Boolean = false // find a struct in the goals bag that unifies with the initial bag
        var findSamePredicateIndicator: Boolean = false
        val a: Array[AnyRef] = e.goalVars.toArray
        val p: Prolog = new Prolog
        var query: Term = initQueryForRelink(e)

        if ((query.asInstanceOf[Struct]).getArity > 2 && (query.asInstanceOf[Struct]).getArg(2).isInstanceOf[Struct]) { // check possible solutions
          {
            val tup =  getInitGoalBagInfo(p,a,tgoal,initBag)
            initGoalBag = tup._1
            find = tup._2
            findSamePredicateIndicator = tup._3
          }

          if (initGoalBag != null) {
            val initGoalBagList: ArrayList[Term] = setInitGoalBagList(initGoalBag)

            var initGoalBagListOrdered: ArrayList[Term] = new ArrayList[Term]
            if ((query.asInstanceOf[Struct]).getName == "setof") { // TODO tidy up string
              initGoalBagListOrdered = handleSetOf(initGoalBagList)
            } else
              initGoalBagListOrdered = initGoalBagList

            {
              val tup = updateInitBags(initBag, initGoalBag, initGoalBagListOrdered)
              initGoalBag = tup._1
              initBag = tup._2
            }
          }
          if (findSamePredicateIndicator) {
            if (!(find && p.unify(initGoalBag, initBag))) {
              val s: String = runner.getEngineMan.getSetOfSolution + "\n\nfalse."
              runner.getEngineMan.setSetOfSolution(s)
              e.nextState = runner.END_FALSE
              runner.getEngineMan.setRelinkVar(false)
              runner.getEngineMan.setBagOFres(null)
              runner.getEngineMan.setBagOFgoal(null)
              runner.getEngineMan.setBagOFvarSet(null)
              runner.getEngineMan.setBagOFbag(null)
              return
            }
          }
        }
        val lSolVar: ArrayList[String] = new ArrayList[String]
        for (i <- 0 until bag.size){
          var resVar: Var = bag.get(i).asInstanceOf[Var]
          var t: Term = resVar.getLink
          if (t != null) {
            if (t.isInstanceOf[Struct]) {
              val t1: Struct = (t.asInstanceOf[Struct])
              var l_temp: ArrayList[String] = new ArrayList[String]
              l_temp = findVar(t1, l_temp)

              for (w <- l_temp.size - 1 to 0 by -1){
                lSolVar.add(l_temp.get(w))
              }
            } else if (t.isInstanceOf[Var]) {
              while (t != null && t.isInstanceOf[Var]) {
                resVar = t.asInstanceOf[Var]
                t = resVar.getLink
              }
              lSolVar.add((resVar.asInstanceOf[Var]).getName)
              bag.set(i, resVar)
            }
          }
          else lSolVar.add(resVar.getName)

        }
        val goalBO: Var = (runner.getEngineMan).getBagOFgoal.asInstanceOf[Var]
        val lgoalBOVar: ArrayList[String] = new ArrayList[String]
        val goalBOvalue: Term = goalBO.getLink
        if (goalBOvalue.isInstanceOf[Struct]) {
          val t1: Struct = (goalBOvalue.asInstanceOf[Struct])
          var l_temp: ArrayList[String] = new ArrayList[String]
          l_temp = findVar(t1, l_temp)
          for (w <- l_temp.size - 1 to 0 by -1){
            lgoalBOVar.add(l_temp.get(w))
          }
        }
        val v: Var = runner.getEngineMan.getBagOFvarSet.asInstanceOf[Var]
        val varList: Struct = v.getLink.asInstanceOf[Struct]
        val lGoalVar: ArrayList[String] = new ArrayList[String]
        if (varList != null) {
          val it = varList.iterator
          for (curr <- it) {
            for (y <- 0 until a.length) {
              val vv: Var = a(y).asInstanceOf[Var]
              if (vv.getLink != null && vv.getLink.isEqual(curr)) {
                lGoalVar.add(vv.getName)
              }
            }
          }
        }
        lgoalBOVar.retainAll(lGoalVar)
        if (lGoalVar.size > lgoalBOVar.size) {
          for (h <- 0 until lGoalVar.size) {
            if (h >= lgoalBOVar.size) {
              lgoalBOVar.add(lGoalVar.get(h))
            }
          }
        }
        var bagVarName: String = null
        var goalSolution: Var = new Var
        if (lSolVar.size > 0 && lgoalBOVar.size > 0 && !varList.isGround && !goalBO.isGround) {
          for (i <- 0 until  bag.size) {
            val resVar: Var = bag.get(i).asInstanceOf[Var]
            var t: Term = resVar.getLink
            if (t == null) {
              t = resVar
            }
            bagVarName = null
            for(y <- 0 until a.length) {
              val vv: Var = a(y).asInstanceOf[Var]
              val vv_link: Var = structValue(vv, i)
              if (vv_link.isEqual(t)) {
                if (bagVarName == null) {
                  bagVarName = vv.getOriginalName
                  goalSolution = vv
                }
                if (vv_link.getLink != null && vv_link.getLink.isInstanceOf[Struct]) {
                  val s: Struct = substituteVar(vv_link.getLink.asInstanceOf[Struct], lSolVar, lgoalBOVar)
                }
                else {
                  val index  = lSolVar.indexOf(resVar.getName)
                  setStructValue(vv, i, new Var(lgoalBOVar.get(index)))
                }
              }
            }
          }
          rename(bagVarName, goalSolution)
        }
        val bagString: ArrayList[String] = (runner.getEngineMan).getBagOFresString
        var i = 0
        var s: String = ""
        for (m <- 0 until bagString.size) {
          val bagResString: String = bag.get(m).toString
          var `var`: Boolean = false

          if (bag.get(m).isInstanceOf[Var] && (bag.get(m).asInstanceOf[Var]).getLink != null
            && ((bag.get(m).asInstanceOf[Var]).getLink.isInstanceOf[Struct])
            && !((bag.get(m).asInstanceOf[Var]).getLink.asInstanceOf[Struct]).isAtomic)
            `var` = true

          if (`var` && bagResString.length != bagString.get(m).length) {
            val st: StringTokenizer = new StringTokenizer(bagString.get(m))
            val st1: StringTokenizer = new StringTokenizer(bagResString)
            while (st.hasMoreTokens) {
              val t1: String = st.nextToken(" / ( ) , ;")
              val t2: String = st1.nextToken(" / ( ) , ;")
              if (t1.compareTo(t2) != 0 && !t2.contains("_")) {
                s = s + lGoalVar.get(i).toString + "=" + t2 + " "
                runner.getEngineMan.setSetOfSolution(s)
                i += 1
              }
            }
          }
        }
      }
      runner.getEngineMan.setRelinkVar(false)
      runner.getEngineMan.setBagOFres(null)
      runner.getEngineMan.setBagOFgoal(null)
      runner.getEngineMan.setBagOFvarSet(null)
      runner.getEngineMan.setBagOFbag(null)
    }

    def setInitGoalBagList(initGoalBag: Term): ArrayList[Term] = {
      val initGoalBagList: ArrayList[Term] = new ArrayList[Term]
      var initGoalBagTemp: Struct = initGoalBag.asInstanceOf[Struct]

      while (initGoalBagTemp.getArity > 0) {
        val t1: Term = initGoalBagTemp.getArg(0)
        initGoalBagList.add(t1)
        val t2: Term = initGoalBagTemp.getArg(1)
        if (t2.isInstanceOf[Struct]) {
          initGoalBagTemp = t2.asInstanceOf[Struct]
        }
      }
      initGoalBagList
    }

    def varValue(theVar : Var): Var = {
      var v = theVar
      var break = false
      while (!break && v.getLink != null) {
        if (v.getLink.isInstanceOf[Var])
          v = v.getLink.asInstanceOf[Var]

        else if (v.getLink.isInstanceOf[Struct])
          v = (v.getLink.asInstanceOf[Struct]).getArg(0).asInstanceOf[Var]
        else
          break = true
      }
      return v
    }

    def varValue2(theVar: Var): Var = {
      var v = theVar
      while (v.getLink != null && v.getLink.isInstanceOf[Var]) {
        v = v.getLink.asInstanceOf[Var]
      }
      return v
    }

    def structValue(theVar: Var, theIndex: scala.Int): Var = {
      var v = theVar
      var i = theIndex
      var s: Struct = new Struct
      var vStruct: Var = new Var
      var break = false
      while (!break && v.getLink != null) {
        if (v.getLink.isInstanceOf[Var]) {
          v = v.getLink.asInstanceOf[Var]
        }
        else if (v.getLink.isInstanceOf[Struct]) {
          s = (v.getLink.asInstanceOf[Struct])
          while (i > 0) {
            if (s.getArg(1).isInstanceOf[Struct]) {
              s = s.getArg(1).asInstanceOf[Struct]
            }
            else if (s.getArg(1).isInstanceOf[Var]) {
              vStruct = (s.getArg(1).asInstanceOf[Var])
              if (vStruct.getLink != null) {
                i -= 1
                return structValue(vStruct, i)
              }
              return vStruct
            }
            i -= 1
          }
          vStruct = (s.getArg(0).asInstanceOf[Var])
          break = true //todo: break is not supported
        }
        else
          break= true //todo: break is not supported
      }
      return vStruct
    }

    def setStructValue(theVar: Var, theIndex: scala.Int, v1: Var) {
      var v = theVar
      var i = theIndex
      var s: Struct = new Struct
      var break = false
      while (!break && v.getLink != null) {
        if (v.getLink.isInstanceOf[Var]) {
          v = v.getLink.asInstanceOf[Var]
        }
        else if (v.getLink.isInstanceOf[Struct]) {
          s = (v.getLink.asInstanceOf[Struct])
          while (i > 0) {
            if (s.getArg(1).isInstanceOf[Struct]) s = s.getArg(1).asInstanceOf[Struct]
            else if (s.getArg(1).isInstanceOf[Var]) {
              v = s.getArg(1).asInstanceOf[Var]
              s = (v.getLink.asInstanceOf[Struct])
            }
            i -= 1
          }
          s.setArg(0, v1)
          break = true
        }
        else
          break = true
      }
    }

    def findVar(s: Struct, l: ArrayList[String]): ArrayList[String] = {
      val allVar: ArrayList[String] = new ArrayList[String]
      if (s.getArity > 0) {
        val t: Term = s.getArg(0)
        var tt: Term = null
        if (s.getArity > 1) {
          tt = s.getArg(1)
          if (tt.isInstanceOf[Var]) {
            allVar.add((tt.asInstanceOf[Var]).getName)
          }
          else if (tt.isInstanceOf[Struct]) {
            val l1: ArrayList[String] = findVar(tt.asInstanceOf[Struct], l)
            allVar.addAll(l1)
          }
        }
        if (t.isInstanceOf[Var]) {
          allVar.add((t.asInstanceOf[Var]).getName)
        }
        else if (t.isInstanceOf[Struct]) {
          val l1: ArrayList[String] = findVar(t.asInstanceOf[Struct], l)
          allVar.addAll(l1)
        }
      }
      return allVar
    }

    def substituteVar(s: Struct, lSol: ArrayList[String], lgoal: ArrayList[String]): Struct = {
      val t: Term = s.getArg(0)
      var tt: Term = null
      if (s.getArity > 1) tt = s.getArg(1)
      if (tt != null && tt.isInstanceOf[Var]) {
        val index = lSol.indexOf((tt.asInstanceOf[Var]).getName)
        s.setArg(1, new Var(lgoal.get(index)))
        if (t.isInstanceOf[Var]) {
          val index1 = lSol.indexOf((t.asInstanceOf[Var]).getName)
          s.setArg(0, new Var(lgoal.get(index1)))
        }
        if (t.isInstanceOf[Struct] && (t.asInstanceOf[Struct]).getArity > 0) {
          val s1: Struct = substituteVar(t.asInstanceOf[Struct], lSol, lgoal)
          s.setArg(0, s1)
        }
      }
      else {
        if (t.isInstanceOf[Var]) {
          val index1  = lSol.indexOf((t.asInstanceOf[Var]).getName)
          s.setArg(0, new Var(lgoal.get(index1)))
        }
        if (t.isInstanceOf[Struct]) {
          val s1: Struct = substituteVar(t.asInstanceOf[Struct], lSol, lgoal)
          s.setArg(0, s1)
        }
      }
      return s
    }

    override def toString: String = {
      endState match {
        case EngineRunner.FALSE =>
          return "FALSE"
        case EngineRunner.TRUE =>
          return "TRUE"
        case EngineRunner.TRUE_CP =>
          return "TRUE_CP"
        case _ =>
          return "HALT"
      }
    }
  }