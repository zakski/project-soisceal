/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
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

package alice.tuprolog;

import alice.tuprolog.json.JSONSerializerManager;
import alice.util.OneWayList;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.db.ops.OperatorManager;
import com.szadowsz.gospel.core.engine.EngineManager;
import com.szadowsz.gospel.core.engine.context.ExecutionContext;

import java.io.Serializable;
import java.util.*;

//import java.util.ArrayList;

/**
 * Term class is the root abstract class for prolog data type
 *
 * @see Struct
 * @see Var
 * @see Number
 */
public abstract class Term implements Serializable {

    // true and false constants
    public static final Term TRUE = new Struct("true");
    public static final Term FALSE = new Struct("false");
    private static final long serialVersionUID = 1L;

    //boolean isCyclic = false; //Alberto -> da usare quando si supporteranno i termini ciclici

    // checking type and properties of the Term

    //Alberto
    public static Term fromJSON(String jsonString) {
        if (jsonString.contains("Var")) {
            return JSONSerializerManager.fromJSON(jsonString, Var.class);
        } else if (jsonString.contains("Struct")) {
            return JSONSerializerManager.fromJSON(jsonString, Struct.class);
        } else if (jsonString.contains("Double")) {
            return JSONSerializerManager.fromJSON(jsonString, Double.class);
        } else if (jsonString.contains("Int")) {
            return JSONSerializerManager.fromJSON(jsonString, Int.class);
        } else if (jsonString.contains("Long")) {
            return JSONSerializerManager.fromJSON(jsonString, Long.class);
        } else if (jsonString.contains("Float")) {
            return JSONSerializerManager.fromJSON(jsonString, Float.class);
        } else
            return null;
    }

    /**
     * is this term a prolog numeric term?
     *
     * @deprecated Use <tt>instanceof Number</tt> instead.
     */
    public abstract boolean isNumber();

    /**
     * is this term a struct?
     *
     * @deprecated Use <tt>instanceof Struct</tt> instead.
     */
    public abstract boolean isStruct();

    /**
     * is this term a variable?
     *
     * @deprecated Use <tt>instanceof Var</tt> instead.
     */
    public abstract boolean isVar();

    /**
     * is this term a null term?
     */
    public abstract boolean isEmptyList();

    /**
     * is this term a constant prolog term?
     */
    public abstract boolean isAtomic();

    /**
     * is this term a prolog compound term?
     */
    public abstract boolean isCompound();

    /**
     * is this term a prolog (alphanumeric) atom?
     */
    public abstract boolean isAtom();

    /**
     * is this term a prolog list?
     */
    public abstract boolean isList();

    /**
     * is this term a ground term?
     */
    public abstract boolean isGround();

    /**
     * Tests for the equality of two object terms
     * <p>
     * The comparison follows the same semantic of
     * the isEqual method.
     */
    public boolean equals(Object t) {
        return t instanceof Term && isEqual((Term) t);
    }

    /**
     * is term greater than term t?
     */
    public abstract boolean isGreater(Term t);

    /**
     * Tests if this term is (logically) equal to another
     */
    public boolean isEqual(Term t) { //Alberto
        return this.toString().equals(t.toString());
    }

    /**
     * Tests if this term (as java object) is equal to another
     */
    public boolean isEqualObject(Term t) { //Alberto
        return t instanceof Term && this == t;
    }

    /**
     * Gets the actual term referred by this Term. if the Term is a bound variable, the method gets the Term linked to the variable
     */
    public abstract Term getTerm();

    /**
     * Unlink variables inside the term
     */
    public abstract void free();

    /**
     * Resolves variables inside the term, starting from a specific time count.
     * <p>
     * If the variables has been already resolved, no renaming is done.
     *
     * @param count new starting time count for resolving process
     * @return the new time count, after resolving process
     */
    abstract long resolveTerm(long count);

    /**
     * Resolves variables inside the term
     * <p>
     * If the variables has been already resolved, no renaming is done.
     */
    public void resolveTerm() {
        resolveTerm(System.currentTimeMillis());
    }

    /**
     * gets a engine's copy of this term.
     *
     * @param idExecCtx Execution Context identified
     */
    public Term copyGoal(AbstractMap<Var, Var> vars, int idExecCtx) {
        return copy(vars, idExecCtx);
    }

    /**
     * gets a copy of this term for the output
     */
    public Term copyResult(Collection<Var> goalVars, List<Var> resultVars) {
        IdentityHashMap<Var, Var> originals = new IdentityHashMap<>();
        for (Var key : goalVars) {
            Var clone = new Var();
            if (!key.isAnonymous()) {
                clone = new Var(key.getOriginalName());
            }
            originals.put(key, clone);
            resultVars.add(clone);
        }
        return copy(originals, new IdentityHashMap<>());
    }

    /**
     * gets a copy (with renamed variables) of the term.
     * <p>
     * The list argument passed contains the list of variables to be renamed
     * (if empty list then no renaming)
     *
     * @param idExecCtx Execution Context identifier
     */
    public abstract Term copy(AbstractMap<Var, Var> vMap, int idExecCtx);

    //Alberto
    public abstract Term copyAndRetainFreeVar(AbstractMap<Var, Var> vMap, int idExecCtx);

    /**
     * gets a copy for result.
     */
    abstract Term copy(AbstractMap<Var, Var> vMap, AbstractMap<Term, Var> substMap);

    //Alberto

    /**
     * Try to unify two terms
     *
     * @param mediator have the reference of EngineManager
     * @param t1       the term to unify
     * @return true if the term is unifiable with this one
     */
    public boolean unify(PrologEngine mediator, Term t1) {
        EngineManager engine = mediator.getEngineManager();
        resolveTerm();
        t1.resolveTerm();
        List<Var> v1 = new LinkedList<>(); /* Reviewed by: Paolo Contessi (was: ArrayList()) */
        List<Var> v2 = new LinkedList<>(); /* Reviewed by: Paolo Contessi (was: ArrayList()) */
        boolean ok = unify(v1, v2, t1, mediator.getFlagManager().isOccursCheckEnabled());
        if (ok) {
            ExecutionContext ec = engine.getCurrentContext();
            if (ec != null) {
                int id = (engine.getEnv() == null) ? Var.PROGRESSIVE : engine.getEnv().getNDemoSteps();
                // Update trailingVars
                ec.trailingVars_$eq(new OneWayList<>(v1, ec.trailingVars()));
                // Renaming after unify because its utility regards not the engine but the user
                int count = 0;
                for (Var v : v1) {
                    v.rename(id, count);
                    if (id >= 0) {
                        id++;
                    } else {
                        count++;
                    }
                }
                for (Var v : v2) {
                    v.rename(id, count);
                    if (id >= 0) {
                        id++;
                    } else {
                        count++;
                    }
                }
            }
            return true;
        }
        Var.free(v1);
        Var.free(v2);
        return false;
    }

    /**
     * Tests if this term is unifiable with an other term.
     * No unification is done.
     * <p>
     * The test is done outside any demonstration context
     *
     * @param t                    the term to checked
     * @param isOccursCheckEnabled
     * @return true if the term is unifiable with this one
     */
    public boolean match(boolean isOccursCheckEnabled, Term t) {
        resolveTerm();
        t.resolveTerm();
        List<Var> v1 = new LinkedList<>();
        List<Var> v2 = new LinkedList<>();
        boolean ok = unify(v1, v2, t, isOccursCheckEnabled);
        Var.free(v1);
        Var.free(v2);
        return ok;
    }

    //Alberto

    /**
     * Tests if this term is unifiable with an other term.
     * No unification is done.
     * <p>
     * The test is done outside any demonstration context
     *
     * @param t the term to checked
     * @return true if the term is unifiable with this one
     */
    public boolean match(Term t) {
        return match(true, t); //Alberto
    }

    /**
     * Tries to unify two terms, given a demonstration context
     * identified by the mark integer.
     * <p>
     * Try the unification among the term and the term specified
     *
     * @param varsUnifiedArg1      Vars unified in myself
     * @param varsUnifiedArg2      Vars unified in term t
     * @param isOccursCheckEnabled
     */
    public abstract boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t, boolean isOccursCheckEnabled);

    // term representation

    /**
     * Tries to unify two terms, given a demonstration context
     * identified by the mark integer.
     * <p>
     * Try the unification among the term and the term specified
     *
     * @param varsUnifiedArg1 Vars unified in myself
     * @param varsUnifiedArg2 Vars unified in term t
     */
    abstract boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t);

    /**
     * Gets the string representation of this term
     * as an X argument of an operator, considering the associative property.
     */
    public String toStringAsArgX(OperatorManager op, int prio) {
        return toStringAsArg(op, prio, true);
    }

    /**
     * Gets the string representation of this term
     * as an Y argument of an operator, considering the associative property.
     */
    public String toStringAsArgY(OperatorManager op, int prio) {
        return toStringAsArg(op, prio, false);
    }

    /**
     * Gets the string representation of this term
     * as an argument of an operator, considering the associative property.
     * <p>
     * If the boolean argument is true, then the term must be considered
     * as X arg, otherwise as Y arg (referring to prolog associative rules)
     */
    String toStringAsArg(OperatorManager op, int prio, boolean x) {
        return toString();
    }
    
    /*Castagna 06/2011*/

    /**
     * The iterated-goal term G of a term T is a term defined
     * recursively as follows:
     * <ul>
     * <li>if T unifies with ^(_, Goal) then G is the iterated-goal
     * term of Goal</li>
     * <li>else G is T</li>
     * </ul>
     */
    public Term iteratedGoalTerm() {
        return this;
    }

    /**
     * Visitor pattern
     *
     * @param tv - Visitor
     */
    public abstract void accept(TermVisitor tv);

    //Alberto
    public String toJSON() {
        return JSONSerializerManager.toJSON(this);
    }
}