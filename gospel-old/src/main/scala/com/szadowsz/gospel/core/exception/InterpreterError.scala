/***/
package com.szadowsz.gospel.core.exception

import com.szadowsz.gospel.core.data.{Struct, Term, Int}
import com.szadowsz.gospel.core.engine.EngineManager

object InterpreterError {

  private def buildDescMsg(error : String, argNo : scala.Int, goal : Term) = {
    s"$error error in argument $argNo of $goal"
  }
  
  def instantiation_error(e : EngineManager, argNo : scala.Int)= {
    val errorTerm = new Struct("instantiation_error")
    val tuPrologTerm = new Struct("instantiation_error", e.getEnv.currentContext.currentGoal, Int(argNo))
 
    val descriptionError =  buildDescMsg("Instantiation",argNo,e.getEnv.currentContext.currentGoal)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def type_error(e : EngineManager, argNo : scala.Int, validType : String, culprit : Term)= {
    val errorTerm = new Struct("type_error", new Struct(validType), culprit)
    val tuPrologTerm = new Struct("type_error", e.getEnv.currentContext.currentGoal, Int(argNo), new Struct(validType), culprit)

    val descriptionError =  buildDescMsg("Type",argNo,e.getEnv.currentContext.currentGoal)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def domain_error(e : EngineManager, argNo : scala.Int, validDomain : String, culprit : Term)= {
    val errorTerm = new Struct("domain_error", new Struct(validDomain), culprit)
    val tuPrologTerm = new Struct("domain_error", e.getEnv.currentContext.currentGoal, Int(argNo), new Struct(validDomain), culprit)

    val descriptionError =  buildDescMsg("Domain",argNo,e.getEnv.currentContext.currentGoal)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def existence_error(e : EngineManager, argNo : scala.Int, objectType : String, culprit : Term, message : Term)= {
    val errorTerm = new Struct("existence_error", new Struct(objectType), culprit)
    val tuPrologTerm = new Struct("existence_error", e.getEnv.currentContext.currentGoal, Int(argNo), new Struct(objectType), culprit, message)

    val descriptionError =  buildDescMsg("Existence",argNo,e.getEnv.currentContext.currentGoal)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def permission_error(e : EngineManager, operation : String, objectType : String, culprit : Term, message : Term)= {
    val errorTerm = new Struct("permission_error", new Struct(operation), new Struct(objectType), culprit)
    val tuPrologTerm = new Struct("permission_error", e.getEnv.currentContext.currentGoal, new Struct(operation), new Struct(objectType), culprit, message)

    val descriptionError =  s"Permission error in  ${e.getEnv.currentContext.currentGoal}"
     new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
    /**/
  }

  def representation_error(e : EngineManager, argNo : scala.Int, flag : String)= {
    val errorTerm = new Struct("representation_error", new Struct(flag))
    val tuPrologTerm = new Struct("representation_error", e.getEnv.currentContext.currentGoal, Int(argNo), new Struct(flag))

    val descriptionError =  buildDescMsg("Representation",argNo,e.getEnv.currentContext.currentGoal)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def evaluation_error(e : EngineManager, argNo : scala.Int, error : String)= {
    val errorTerm = new Struct("evaluation_error", new Struct(error))
    val tuPrologTerm = new Struct("evaluation_error", e.getEnv.currentContext.currentGoal, Int(argNo), new Struct(error))

    val descriptionError =  buildDescMsg("Evaluation",argNo,e.getEnv.currentContext.currentGoal)
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def resource_error(e : EngineManager, resource : Term)= {
    val errorTerm = new Struct("resource_error", resource)
    val tuPrologTerm = new Struct("resource_error", e.getEnv.currentContext.currentGoal, resource)

    val descriptionError =  s"Resource error in ${e.getEnv.currentContext.currentGoal}"
     new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def syntax_error(e : EngineManager, ex : InvalidTermException): InterpreterError= {
     syntax_error(e,-1,ex.getLine,ex.getPos, new Struct (ex.getMessage))
  }

  def syntax_error(e : EngineManager, ex : InvalidTheoryException): InterpreterError= {
     syntax_error(e,ex.getClause,ex.getLine,ex.getPos, new Struct (ex.getMessage))
  }

  private def syntax_error(e : EngineManager, clause : scala.Int, line : scala.Int, position : scala.Int, message : Term): InterpreterError= {
    val errorTerm = new Struct("syntax_error", message)
    val tuPrologTerm = new Struct("syntax_error", e.getEnv.currentContext.currentGoal, Int(line), Int(position), message)
    /*Castagna 06/2011*/
    // new InterpreterError(new Struct("error", errorTerm, tuPrologTerm))

    val errorInformation = Array(clause, line, position)
    val nameInformation = Array("clause", "line", "position")
    var syntaxErrorDescription = message.getTerm.toString

      //Replacing any new line characters with a space
      syntaxErrorDescription = syntaxErrorDescription.replace("\n", " ")

      //Elimination apex of opening and closing string
      syntaxErrorDescription = syntaxErrorDescription.substring(1, syntaxErrorDescription.length()-1)
      val start = 	(""+syntaxErrorDescription.charAt(0)).toLowerCase()

      syntaxErrorDescription = start + syntaxErrorDescription.substring(1)


    var descriptionError = "Syntax error"

    var  firstSignificativeInformation = true
    for(i <- errorInformation.indices) {
      if(errorInformation(i) != -1) {
        if (firstSignificativeInformation) {
          descriptionError += " at " + nameInformation(i) + "#" + errorInformation(i)
          firstSignificativeInformation = false
        } else {
          descriptionError += ", " + nameInformation(i) + "#" + errorInformation(i)
        }
      }
    }
    descriptionError += ": " + syntaxErrorDescription

     new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), descriptionError)
  }

  def system_error(message : Term): InterpreterError = {
    val errorTerm = new Struct("system_error")
    val tuPrologTerm = new Struct("system_error", message)
 
    new InterpreterError(new Struct("error", errorTerm, tuPrologTerm), "System error")
  }
}

class InterpreterError(protected val error: Term, message : String) extends Exception(message) {


  def this (error: Term) {
    this(error,null)
  }

  def getError: Term = error


  override def toString: String = getMessage
}
