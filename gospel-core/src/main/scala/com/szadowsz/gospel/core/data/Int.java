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
package com.szadowsz.gospel.core.data;

import com.szadowsz.gospel.core.data.Number;
import com.szadowsz.gospel.core.data.Struct;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.data.Var;

import java.util.List;

/**
 * Int class represents the integer prolog data type
 */
public class Int extends Number {
    private static final long serialVersionUID = 1L;
    private final int value;

    @SuppressWarnings("unused")
    private String type = "Int";

    public Int(int v) {
        value = v;
    }

    /**
     * Returns the value of the Integer as int
     */
    final public int intValue() {
        return value;
    }

    /**
     * Returns the value of the Integer as float
     */
    final public float floatValue() {
        return (float) value;
    }

    /**
     * Returns the value of the Integer as double
     */
    final public double doubleValue() {
        return (double) value;
    }

    /**
     * Returns the value of the Integer as long
     */
    final public long longValue() {
        return value;
    }

    /**
     * is this term a prolog integer term?
     */
    final public boolean isInteger() {
        return true;
    }

    /**
     * is this term a prolog real term?
     */
    final public boolean isReal() {
        return false;
    }

    /**
     * Returns true if this integer term is grater that the term provided.
     * For number term argument, the int value is considered.
     */
    public boolean isGreater(Term t) {
        t = t.getTerm();
        if (t instanceof Number) {
            return value > ((Number) t).intValue();
        } else
            return !(t instanceof Struct) && t instanceof Var;
    }

    /**
     * Tries to unify a term with the provided term argument.
     * This service is to be used in demonstration context.
     */
    public boolean unify(List<Var> vl1, List<Var> vl2, Term t, boolean isOccursCheckEnabled) {
        t = t.getTerm();
        if (t instanceof Var) {
            return t.unify(vl2, vl1, this, isOccursCheckEnabled);
        } else {
            return t instanceof Number && ((Number) t).isInteger() && value == ((Number) t).intValue();
        }
    }

    public String toString() {
        return Integer.toString(value);
    }

    /**
     * @author Paolo Contessi
     */
    public int compareTo(Number o) {
        return (new java.lang.Integer(value)).compareTo(o.intValue());
    }

    @Override
    boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t) {
        return unify(varsUnifiedArg1, varsUnifiedArg2, t, true);
    }

}