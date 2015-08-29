package alice.tuprolog.ext.pj.model

import alice.tuprologx.pj.model.{JavaTerm}

@SuppressWarnings(Array("serial"))
private[ext] class TermifiableStruct[O](name: String, arr: Array[alice.tuprolog.core.data.Term]) extends alice.tuprolog.core.data.Struct(name, arr){
  private[ext] var _term: JavaTerm[O] = null

  private[ext] def setJavaTerm(term: JavaTerm[O]): TermifiableStruct[O] = {
    _term = term
    this
  }

  private[ext] def getJavaTerm: JavaTerm[O] = _term
}