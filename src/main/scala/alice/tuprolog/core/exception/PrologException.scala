package alice.tuprolog.core.exception

import alice.tuprolog.core.data.numeric.Int
import alice.tuprolog.core.data.{Struct, Term}
import alice.tuprolog.core.engine.EngineManager

/* Castagna 06/2011*
 *  
 * syntax to predict :
 * TYPE  in  argument  ARGUMENT  of      GOAL        (instantiation, type, domain, existence, representation, evaluation)
 * TYPE  in        GOAL                    (permission, resource)
 * TYPE  at clause#CLAUSE, line#LINE, position#POS: DESCRIPTION    (syntax)
 * TYPE                                (system)
 *
 */
object PrologException {

  def instantiation_error(eng: EngineManager, argNo: scala.Int) = {
    val errorTerm = new Struct("instantiation_error")
    val tuPrologTerm = new Struct("instantiation_error", eng.getEnv.context.currentGoal, new Int(argNo))
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm))   
    val desc = "Instantiation error in argument " + argNo + " of " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }

  def type_error(eng: EngineManager, argNo: scala.Int, validType: String, culprit: Term) = {
    val errorTerm = new Struct("type_error", new Struct(validType), culprit)
    val tuPrologTerm = new Struct("type_error", eng.getEnv.context.currentGoal, new Int(argNo), new Struct(validType), culprit)

    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm)) 
    val desc = "Type error in argument " + argNo + " of " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }

  def domain_error(eng: EngineManager, argNo: scala.Int, validDomain: String, culprit: Term) = {
    val errorTerm = new Struct("domain_error", new Struct(validDomain), culprit)
    val tuPrologTerm = new Struct("domain_error", eng.getEnv.context.currentGoal, new Int(argNo), new Struct(validDomain), culprit)
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm)) 
    val desc = "Domain error in argument " + argNo + " of " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }

  def existence_error(eng: EngineManager, argNo: scala.Int, objectType: String, culprit: Term, message: Term) = {
    val errorTerm = new Struct("existence_error", new Struct(objectType), culprit)
    val tuPrologTerm = new Struct("existence_error", eng.getEnv.context.currentGoal, new Int(argNo), new Struct(objectType), culprit, message)
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm)) 
    val desc = "Existence error  in argument " + argNo + " of " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }

  def permission_error(eng: EngineManager, operation: String, objectType: String, culprit: Term, message: Term) = {
    val errorTerm = new Struct("permission_error", new Struct(operation), new Struct(objectType), culprit)
    val tuPrologTerm = new Struct("permission_error", eng.getEnv.context.currentGoal, new Struct(operation), new Struct(objectType), culprit, message)
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm)) 
    val desc = "Permission error in  " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }

  def representation_error(eng: EngineManager, argNo: scala.Int, flag: String) = {
    val errorTerm = new Struct("representation_error", new Struct(flag))
    val tuPrologTerm = new Struct("representation_error", eng.getEnv.context.currentGoal, new Int(argNo), new Struct(flag))
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm))
    val desc = "Representation error in argument " + argNo + " of " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }

  def evaluation_error(eng: EngineManager, argNo: scala.Int, error: String) = {
    val errorTerm = new Struct("evaluation_error", new Struct(error))
    val tuPrologTerm = new Struct("evaluation_error", eng.getEnv.context.currentGoal, new Int(argNo), new Struct(error))
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm)) 
    val descriptionError = "Evaluation error in argument " + argNo + " of " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def resource_error(eng: EngineManager, resource: Term) = {
    val errorTerm = new Struct("resource_error", resource)
    val tuPrologTerm = new Struct("resource_error", eng.getEnv.context.currentGoal, resource)
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm))   
    val desc = "Resource error in " + eng.getEnv.context.currentGoal.toString()
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }

  private def syntaxErrorDesc(desc: String) = {
    //Replacement of any newline characters with a space
    val desc1 = desc.replace("\n", " ")
    //Elimination of opening and closing chars
    val desc2 = desc1.substring(1, desc1.length() - 1)
    val start = ("" + desc2.charAt(0)).toLowerCase()
    //enforce desciption lowercase start
    start + desc2.substring(1)
  }

  def syntax_error(eng: EngineManager, clause: scala.Int, line: scala.Int, position: scala.Int, message: Term) = {
    val errorTerm = new Struct("syntax_error", message)
    val tuPrologTerm = new Struct("syntax_error", eng.getEnv.context.currentGoal, new Int(line), new Int(position), message)
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm))

    val errorInformation = Array(clause, line, position)
    val nameInformation = Array("clause", "line", "position")
    val syntaxErrorDescription = syntaxErrorDesc(message.getTerm.toString())

    var descriptionError = "Syntax error"
    var firstSignificativeInformation = true

    for (i <- 0 until errorInformation.length) {
      if (errorInformation(i) != -1) {
        if (firstSignificativeInformation) {
          descriptionError += " at " + nameInformation(i) + "#" + errorInformation(i)
          firstSignificativeInformation = false
        } else
          descriptionError += ", " + nameInformation(i) + "#" + errorInformation(i)
      }
    }
    descriptionError += ": " + syntaxErrorDescription

    new PrologException(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def system_error(message: Term) = {
    val errorTerm = new Struct("system_error")
    val tuPrologTerm = new Struct("system_error", message)
    /*Castagna 06/2011*/
    //return new PrologError(new Struct("error", errorTerm, tuPrologTerm))
    val desc = "System error"
    new PrologException(new Struct("error", errorTerm, tuPrologTerm), desc)
  }
}

/**
 * @author Matteo Iuliani
 */
@SerialVersionUID(1L)
class PrologException private (error: Term, descriptionError: String) extends Throwable {
  // Prolog term that is the subject of throw/1
  private val _error = error
  /*Castagna 06/2011*/
  private val _descriptionError = descriptionError
  /**/

  def this (t : Term){this(t,"")}
  
  def getError() = _error

  /*Castagna 06/2011*/
  override def toString() = _descriptionError
}