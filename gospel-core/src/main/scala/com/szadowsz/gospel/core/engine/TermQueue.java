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
package com.szadowsz.gospel.core.engine;

import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.engine.EngineRunner;

import java.util.LinkedList;
import java.util.ListIterator;

public class TermQueue {

    private final LinkedList<Term> queue;

    public TermQueue() {
        queue = new LinkedList<>();
    }

    public synchronized boolean get(Term t, PrologEngine engine, EngineRunner er) {
        return searchLoop(t, engine, true, true, er);
    }

    private synchronized boolean searchLoop(Term t, PrologEngine engine, boolean block, boolean remove, EngineRunner er) {
        do {
            boolean found = search(t, engine, remove);
            if (found) return true;
            er.setSolving(false);
            try {
                wait();
            } catch (InterruptedException e) {
                break;
            }
        } while (block);
        return false;
    }


    private synchronized boolean search(Term t, PrologEngine engine, boolean remove) {
        boolean found = false;
        Term msg = null;
        ListIterator<Term> it = queue.listIterator();
        while (!found) {
            if (it.hasNext()) {
                msg = it.next();
            } else {
                return false;
            }
            found = engine.unify(t, msg);
        }
        if (remove) {
            queue.remove(msg);
        }
        return true;
    }


    public synchronized boolean peek(Term t, PrologEngine engine) {
        return search(t, engine, false);
    }

    public synchronized boolean remove(Term t, PrologEngine engine) {
        return search(t, engine, true);
    }

    public synchronized boolean wait(Term t, PrologEngine engine, EngineRunner er) {
        return searchLoop(t, engine, true, false, er);
    }

    public synchronized void store (Term t){
        queue.addLast(t);
        notifyAll();
    }

    public synchronized int size(){
        return queue.size();
    }

    public synchronized void clear(){
        queue.clear();
    }
}
