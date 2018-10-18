package com.szadowsz.gospel.core.engine.context.clause

import java.util

import alice.util.OneWayList
import com.szadowsz.gospel.core.data.{Term, Var}


object ClauseStore {

  /**
    * Upload a family of clauses
    *
    * Reviewed by Paolo Contessi:
    * OneWayList.transform(List) -> OneWayList.transform2(List)
    *
    * @param familyClauses
    */
  def build(goal: Term, vars: util.List[Var], familyClauses: util.List[ClauseInfo]): ClauseStore = {
    val clauseStore: ClauseStore = new ClauseStore(goal, vars)
    clauseStore.clauses = OneWayList.transform2(familyClauses)
    if (clauseStore.clauses == null || !clauseStore.existCompatibleClause)
      null
    else
      clauseStore
  }
}

/**
  * A list of clauses belonging to the same family as a goal. A family is composed by clauses with the same functor and arity.
  */
class ClauseStore(goalTerm: Term, varList: util.List[Var]) {
  private val goal: Term = goalTerm
  private val vars: util.List[Var] = varList
  private var clauses: OneWayList[ClauseInfo] = _
  private var haveAlternatives: Boolean = false

  /**
    * Returns the clause to load
    */
  def fetch: ClauseInfo = {
    if (clauses == null)
      return null

    deunify(vars)

    if (!checkCompatibility(goal))
      return null

    val clause: ClauseInfo = clauses.getHead
    clauses = clauses.getTail
    haveAlternatives = checkCompatibility(goal)
    clause
  }

  /**
    * Save the unifications of the variables to deunify
    *
    * @param varsToDeunify
    * @return Unification of variables
    */
  private def deunify(varsToDeunify: util.List[Var]): util.List[Term] = {
    val saveUnifications: util.List[Term] = new util.ArrayList[Term]
    val it: util.Iterator[Var] = varsToDeunify.iterator
    while (it.hasNext) {
      val v: Var = it.next
      saveUnifications.add(v.getLink)
      v.free()
    }
    saveUnifications
  }

  /**
    * Verify if a clause exists that is compatible with goal.
    * As a side effect, clauses that are not compatible get
    * discarded from the currently examined family.
    *
    * @param goal
    */
  private def checkCompatibility(goal: Term): Boolean = {
    if (clauses == null)
      return false
    var clause: ClauseInfo = null

    do {
      clause = clauses.getHead
      if (goal.`match`(clause.getHead))
        return true

      clauses = clauses.getTail

    } while (clauses != null)

    false
  }

  def hasAlternatives(): Boolean = haveAlternatives

  override def toString: String = {
    "clauses: " + clauses + "\n" + "goal: " + goal + "\n" + "vars: " + vars + "\n"
  }

  def getClauses: util.List[ClauseInfo] = {
    val l: util.ArrayList[ClauseInfo] = new util.ArrayList[ClauseInfo]
    var t: OneWayList[ClauseInfo] = clauses
    while (t != null) {
      l.add(t.getHead)
      t = t.getTail
    }
    l
  }

  def getMatchGoal: Term = goal

  def getVarsForMatch: util.List[Var] = vars

  /**
    * Verify if there is a term in compatibleGoals compatible with goal.
    *
    * @return true if compatible or false otherwise.
    */
  protected[core] def existCompatibleClause: Boolean = {
    val saveUnifications: util.List[Term] = deunify(vars)
    val found: Boolean = checkCompatibility(goal)
    reunify(vars, saveUnifications)
    found
  }

  /**
    * Restore previous unifications into variables.
    *
    * @param varsToReunify
    * @param saveUnifications
    */
  private def reunify(varsToReunify: util.List[Var], saveUnifications: util.List[Term]) {
    val size: Int = varsToReunify.size
    val it1 = varsToReunify.listIterator(size)
    val it2 = saveUnifications.listIterator(size)
    // Only the first occurrence of a variable gets its binding saved;
    // following occurrences get a null instead. So, to avoid clashes
    // between those values, and avoid random variable deunification,
    // the reunification is made starting from the end of the list.
    while (it1.hasPrevious) {
      it1.previous.setLink(it2.previous)
    }
  }
}