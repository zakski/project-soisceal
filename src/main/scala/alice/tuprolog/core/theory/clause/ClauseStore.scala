package alice.tuprolog.core.theory.clause

import java.{util => ju}

import scala.collection.JavaConverters._

import alice.tuprolog.core.data.{Term, Var}
import alice.util.OneWayList


object ClauseStore {
  /**
   * Upload a family of clauses
   *
   * Reviewed by Paolo Contessi:
   * OneWayList.transform(List) -> OneWayList.transform2(List)
   *
   * @param familyClauses
   */
  def build(goal: Term, vars: ju.List[Var], familyClauses: List[ClauseInfo]): ClauseStore = {
    val clauseStore: ClauseStore = new ClauseStore(goal, vars)
    clauseStore._clauses = OneWayList.transform2(familyClauses.asJava)
    if (clauseStore._clauses == null || !clauseStore.existCompatibleClause)
      null
    else
      clauseStore
  }
}

/**
 * A list of clauses belonging to the same family as a goal. A family is
 * composed by clauses with the same functor and arity.
 */
class ClauseStore(theGoal: Term, theVars: ju.List[Var]) {
  private var _clauses: OneWayList[ClauseInfo] = null
  private val _goal: Term = theGoal
  private var _vars: ju.List[Var] = theVars
  private var _haveAlternatives: Boolean = false

  /**
   * Returns the clause to load
   */
  def fetch: ClauseInfo = {
    if (_clauses == null)
      return null

    deunify(_vars)

    if (!checkCompatibility(_goal))
      return null

    val clause: ClauseInfo = _clauses.getHead
    _clauses = _clauses.getTail
    _haveAlternatives = checkCompatibility(_goal)
    clause
  }

  def haveAlternatives(): Boolean = {
    _haveAlternatives
  }

  /**
   * Verify if there is a term in compatibleGoals compatible with goal.
   * @return true if compatible or false otherwise.
   */
  protected[core] def existCompatibleClause: Boolean = {
    val saveUnifications: ju.List[Term] = deunify(_vars)
    val found: Boolean = checkCompatibility(_goal)
    reunify(_vars, saveUnifications)
    found
  }

  /**
   * Save the unifications of the variables to deunify
   * @param varsToDeunify
   * @return Unification of variables
   */
  private def deunify(varsToDeunify: ju.List[Var]): ju.List[Term] = {
    val saveUnifications: ju.List[Term] = new ju.ArrayList[Term]
    val it: ju.Iterator[Var] = varsToDeunify.iterator
    while (it.hasNext) {
      val v: Var = it.next
      saveUnifications.add(v.getLink)
      v.free
    }
    saveUnifications
  }

  /**
   * Restore previous unifications into variables.
   * @param varsToReunify
   * @param saveUnifications
   */
  private def reunify(varsToReunify: ju.List[Var], saveUnifications: ju.List[Term]) {
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

  /**
   * Verify if a clause exists that is compatible with goal.
   * As a side effect, clauses that are not compatible get
   * discarded from the currently examined family.
   * @param goal
   */
  private def checkCompatibility(goal: Term): Boolean = {
    if (_clauses == null)
      return false
    var clause: ClauseInfo = null

    do {
      clause = _clauses.getHead
      if (goal.matches(clause.getHead))
        return true

      _clauses = _clauses.getTail

    } while (_clauses != null)

    false
  }

  override def toString: String = {
    "clauses: " + _clauses + "\n" + "goal: " + _goal + "\n" + "vars: " + _vars + "\n"
  }

  def getClauses: ju.List[ClauseInfo] = {
    val l: ju.ArrayList[ClauseInfo] = new ju.ArrayList[ClauseInfo]
    var t: OneWayList[ClauseInfo] = _clauses
    while (t != null) {
      l.add(t.getHead)
      t = t.getTail
    }
    l
  }

  def getMatchGoal: Term = {
    _goal
  }

  def getVarsForMatch: ju.List[Var] = {
    _vars
  }
}