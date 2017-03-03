package com.szadowsz.gospel.core.db.theory

import java.util

import alice.tuprolog.{InvalidTermException, InvalidTheoryException, Struct, Term, Var}
import alice.tuprolog.json.{AbstractEngineState, FullEngineState}
import alice.util.Tools
import com.szadowsz.gospel.core.PrologEngine
import com.szadowsz.gospel.core.Theory
import com.szadowsz.gospel.core.db.theory.clause.{ClauseDatabase, FamilyClausesList}
import com.szadowsz.gospel.core.engine.context.ExecutionContext
import com.szadowsz.gospel.core.engine.context.clause.ClauseInfo
import com.szadowsz.gospel.core.engine.context.subgoal.tree.SubGoalLeaf
import com.szadowsz.gospel.core.parser.Parser
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * This class defines the Theory Manager who manages the clauses/theory often referred to as the Prolog database.
  *
  * The theory (as a set of clauses) are stored in the ClauseDatabase which in essence is a HashMap grouped by functor/arity.
  *
  * The TheoryManager functions logically, as prescribed by ISO Standard 7.5.4 section. The effects of assertions and retractions shall not be undone if the
  * program subsequently backtracks over the assert or retract call, as prescribed by ISO Standard 7.7.9 section.
  *
  * To use the TheoryManager one should primarily use the methods assertA, assertZ, consult, retract, abolish and find.
  *
  * Created on 19/02/2017.
  *
  * @version Gospel 2.0.0
  */
@SerialVersionUID(1L)
final case class TheoryManager(private val wam: PrologEngine) {
  private lazy val logger = LoggerFactory.getLogger(getClass)

  private lazy val _primitiveManager = wam.getPrimitiveManager

  private val staticDB = new ClauseDatabase()
  private var dynamicDB = new ClauseDatabase()
  private var retractDB = new ClauseDatabase()


  private var startGoalStack: util.Stack[Term] = _
  private[theory] var lastConsultedTheory: Theory = new Theory()


  /**
    * Gets a clause from a generic Term
    *
    * @param clauseContainer the term the clause is contained in.
    * @return the identified clause.
    */
  private def toClause(clauseContainer: Struct): Struct = {
    var term = wam.createTerm(clauseContainer.toString).asInstanceOf[Struct]
    if (!term.isClause) {
      term = new Struct(":-", term, new Struct("true"))
    }
    _primitiveManager.identifyPredicate(term)
    term
  }

  /**
    * Method to check if a Strutc is a directive and if so, run it.
    *
    * @param directive Struct that contains the directive to be run
    * @return true if it is a directive, false otherwise
    */
  private def runDirective(directive: Struct): Boolean = {
    val hasDirName = "':-'" == directive.getName || ":-" == directive.getName
    if (hasDirName && directive.getArity == 1 && directive.getTerm(0).isInstanceOf[Struct]) {
      val dir: Struct = directive.getTerm(0).asInstanceOf[Struct]
      try {
        if (!_primitiveManager.evalAsDirective(dir)) {
          logger.warn("The directive {} is unknown.", dir.getPredicateIndicator)
          wam.warn("The directive " + dir.getPredicateIndicator + " is unknown.")
        }
      } catch {
        case thrown: Throwable =>
          logger.error("Exception thrown during execution of " + dir.getPredicateIndicator + " directive.", thrown)
          wam.warn("An exception has been thrown during the execution of the " + dir.getPredicateIndicator + " directive.\n" + thrown.getMessage)
      }
      true
    } else {
      false
    }
  }

  /**
    * Method inserts of a clause at the head of the theory.
    *
    * @param clause    the Struct to insert.
    * @param isDynamic True if being done on the fly, false if it comes from a library.
    * @param libName   The name of the library the clause comes from, if any.
    */
  def assertA(clause: Struct, isDynamic: Boolean, libName: String, backtrackable: Boolean): Unit = {
    synchronized {
      val d: ClauseInfo = new ClauseInfo(toClause(clause), libName)

      val key: String = d.getHead.getPredicateIndicator
      if (isDynamic) {
        dynamicDB.addFirst(key, d)
        if (staticDB.containsKey(key)) {
          logger.warn("A static predicate with signature {} has been overridden.", key)
        }
      } else staticDB.addFirst(key, d)
      logger.info("INSERTA: {}", d.getClause)
    }
  }

  /**
    * Method inserts of a clause at the end of the DB.
    *
    * @param clause    the Struct to insert.
    * @param isDynamic True if being done on the fly, false if it comes from a library.
    * @param libName   The name of the library the clause comes from, if any.
    */
  def assertZ(clause: Struct, isDynamic: Boolean, libName: String, backtrackable: Boolean): Unit = {
    synchronized {
      val d: ClauseInfo = new ClauseInfo(toClause(clause), libName)

      val key: String = d.getHead.getPredicateIndicator
      if (isDynamic) {
        dynamicDB.addLast(key, d)
        if (staticDB.containsKey(key)) {
          logger.warn("A static predicate with signature {} has been overridden.", key)
        }
      }
      else staticDB.addLast(key, d)
      logger.info("INSERTZ: {}", d.getClause)
    }
  }

  /**
    * Method to remove the first clause that unifies with the provided clause from the DB.
    *
    * @param oldClause the clause struct to remove.
    * @return the removed Clause
    */
  def retract(oldClause: Struct): ClauseInfo = {
    synchronized {
      val clause: Struct = toClause(oldClause)
      val struct: Struct = clause.getArg(0).asInstanceOf[Struct]
      val family: FamilyClausesList = dynamicDB.get(struct.getPredicateIndicator)
      val ctx: ExecutionContext = wam.getEngineManager.getCurrentContext

      /* create a new clause list to store the theory upon retract
     * This is done only on the first loop of the same retract
     * (Which we recognise by the id of the context)
     * Will be 'the retract from this db to return the result
     */
      var familyQuery: FamilyClausesList = null
      if (!retractDB.containsKey("ctxId " + ctx.getId)) {
        familyQuery = new FamilyClausesList
        for (i <- 0 until family.size) {
          familyQuery.add(family.get(i))
        }
        retractDB.put("ctxId " + ctx.getId, familyQuery)
      }
      else {
        familyQuery = retractDB.get("ctxId " + ctx.getId)
      }
      if (familyQuery == null)
        return null

      if (family != null) {
        // does the retract from the basic theory
        val it = family.iterator
        while (it.hasNext) {
          val d: ClauseInfo = it.next
          if (clause.`match`(d.getClause)) {
            it.remove()
          }
        }
      }
      val i = familyQuery.iterator // does the retract from the db
      while (i.hasNext) {
        val d: ClauseInfo = i.next
        if (clause.`match`(d.getClause)) {
          i.remove()
          logger.info("DELETED: {}", d.getClause)
          return new ClauseInfo(d.getClause, null)
        }
      }
      return null
    }
  }

  /**
    * Method to remove all the clauses corresponding to the predicate indicator passed as a parameter from the DB.
    *
    * @param indicator struct with "/" as name, arg0 "name" and arg1 "arity".
    * @throws java.lang.IllegalArgumentException thrown if predicate indicator is invalid.
    * @return true if it was successful
    */
  @throws(classOf[IllegalArgumentException])
  def abolish(indicator: Struct): Boolean = {
    synchronized {
      if (!indicator.isInstanceOf[Struct] || !indicator.isGround || !(indicator.getArity == 2)) {
        throw new IllegalArgumentException(indicator + " is not a valid Struct")

      } else if (!(indicator.getName == "/")) {
        throw new IllegalArgumentException(indicator + " has not the valid predicate name. Expected '/' but was " + indicator.getName)
      }

      val arg0: String = Tools.removeApices(indicator.getArg(0).toString)
      val arg1: String = Tools.removeApices(indicator.getArg(1).toString)
      val key: String = arg0 + "/" + arg1

      val abolished = dynamicDB.abolish(key)
      if (abolished != null) {
        logger.info("ABOLISHED: {} number of clauses={}", key, abolished.size)
      }
      true
    }
  }

  /**
    * Returns a family of clauses with functor and arity equals
    * to the functor and arity of the term passed as a parameter
    *
    * Reviewed by Paolo Contessi: modified according to new ClauseDatabase
    * implementation
    */
  def find(headt: Term): util.List[ClauseInfo] = {
    synchronized {
      if (headt.isInstanceOf[Struct]) {
        var list: util.List[ClauseInfo] = dynamicDB.getPredicates(headt)
        if (list.isEmpty) list = staticDB.getPredicates(headt)
        return list
      }
      if (headt.isInstanceOf[Var]) {
        throw new RuntimeException
      }
      new util.ArrayList[ClauseInfo]()
    }
  }

  /**
    * Consults a theory, adding it to the base of known terms.
    *
    * @param theory    the theory to add
    * @param isDynamic if it is true, then the clauses are marked as dynamic
    * @param libName   if it not null, then the clauses are marked to belong to the specified library
    */
  def consult(theory: Theory, isDynamic: Boolean, libName: String): Unit = {
    synchronized {
      startGoalStack = new util.Stack[Term]
      var clause: scala.Int = 1

      // iterate all clauses in theory and assert them or run them in the case of directives.
      try {
        val it: util.Iterator[_ <: Term] = theory.iterator(wam)
        while (it.hasNext) {
          clause += 1
          val d: Struct = it.next().asInstanceOf[Struct]

          if (!runDirective(d)) {
            assertZ(d, isDynamic, libName, true)
          }
        }
      } catch {
        case e: InvalidTermException => throw new InvalidTheoryException(e.getMessage, clause, e.line, e.pos)
      }
      if (libName == null)
        lastConsultedTheory = theory
    }
  }

  /**
    * Binds clauses in the database with the corresponding primitive predicate, if any.
    */
  def rebindPrimitives(): Unit = {
    for (d <- dynamicDB.iterator.asScala) {
      for (sge <- d.getBody.asScala) {
        val t: Term = sge.asInstanceOf[SubGoalLeaf].getValue
        _primitiveManager.identifyPredicate(t)
      }
    }
  }

  /**
    * Clears the dynamic clause database.
    */
  def clear(): Unit = {
    synchronized {
      dynamicDB = new ClauseDatabase
    }
  }

  /**
    * Method to remove all the clauses of library theory.
    *
    * @param libName the library to remove.
    */
  def removeLibraryTheory(libName: String): Unit = {
    synchronized {
      val allClauses: util.Iterator[ClauseInfo] = staticDB.iterator
      while (allClauses.hasNext) {
        val clause = allClauses.next
        if (clause.getLib != null && (libName == clause.getLib)) {
          try {
            allClauses.remove()
          }
          catch {
            case e: Exception => logger.error("Error during library theory removal", e)
          }
        }
      }
    }
  }

  /**
    * Method solves and goals that have been added during the theory initialisation process
    */
  def solveTheoryGoal(): Unit = {
    synchronized {
      var s: Struct = null
      while (!startGoalStack.empty) {
        s = if (s == null) startGoalStack.pop.asInstanceOf[Struct] else new Struct(",", startGoalStack.pop.asInstanceOf[Struct], s)
      }
      if (s != null) {
        try {
          wam.solve(s)
        }
        catch {
          case ex: Exception =>
            logger.error("Exception thrown when solving " + s.toString() + " during Theory initialisation", ex)
        }
      }
    }
  }

  /**
    * add a goal eventually defined by last parsed theory.
    */
  def addStartGoal(g: Struct): Unit = {
    synchronized {
      startGoalStack.push(g)
    }
  }

  /**
    * Gets current theory
    *
    * @param onlyDynamic if true, fetches only dynamic clauses
    */
  def getTheory(onlyDynamic: Boolean): String = {
    synchronized {
      val buffer = new StringBuffer
      val dynamicClauses = dynamicDB.iterator

      while (dynamicClauses.hasNext) {
        val d = dynamicClauses.next
        buffer.append(d.toString(wam.getOperatorManager)).append("\n")
      }

      if (!onlyDynamic) {
        val staticClauses: util.Iterator[ClauseInfo] = staticDB.iterator
        while (staticClauses.hasNext) {
          val d: ClauseInfo = staticClauses.next
          buffer.append(d.toString(wam.getOperatorManager)).append("\n")
        }
      }
      return buffer.toString
    }
  }

  def getLastConsultedTheory: Theory = {
    synchronized {
      lastConsultedTheory
    }
  }

  def clearRetractDB(): Unit = retractDB = new ClauseDatabase()

  def checkExistence(predicateIndicator: String): Boolean = dynamicDB.containsKey(predicateIndicator) || staticDB.containsKey(predicateIndicator)

  def serializeLibraries(brain: FullEngineState): Unit = brain.setLibraries(wam.getCurrentLibraries)

  def serializeTimestamp(brain: AbstractEngineState): Unit = brain.setSerializationTimestamp(System.currentTimeMillis)

  def serializeDynDataBase(brain: FullEngineState): Unit = brain.setDynTheory(getTheory(true))
}
