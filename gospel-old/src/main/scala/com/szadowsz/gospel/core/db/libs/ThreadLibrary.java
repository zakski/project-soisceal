/**
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.szadowsz.gospel.core.db.libs;

import com.szadowsz.gospel.core.data.Int;
import com.szadowsz.gospel.core.db.JLibrary;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.engine.EngineManager;
import com.szadowsz.gospel.core.exception.InterpreterError;
import com.szadowsz.gospel.core.exception.InvalidTermException;
import scala.Option;


public class ThreadLibrary extends JLibrary {
    private static final long serialVersionUID = 1L;
    protected EngineManager engineManager;

    @Override
    public void setEngine(PrologEngine en) {
        super.setEngine(en);
        engineManager = en.getEngineManager();
    }

    //Tenta di unificare a t l'identificativo del thread corrente
    public boolean thread_id_1(Term t) throws InterpreterError {
        int id = engineManager.runnerId();
        unify(t, new Int(id));
        return true;
    }

    //Crea un nuovo thread di identificatore id che comincia ad eseguire il goal dato
    public boolean thread_create_2(Term id, Term goal) {
        return engineManager.threadCreate(id, goal);
    }

    /*Aspetta la terminazione del thread di identificatore id e ne raccoglie il risultato,
    unificando il goal risolto a result. Il thread viene eliminato dal sistema*/
    public boolean thread_join_2(Term id, Term result) throws InterpreterError {
        id = id.getTerm();
        if (!(id instanceof Int))
            throw InterpreterError.type_error(engineManager, 1, "integer", id);
        Solution res = engineManager.join(((Int) id).intValue());
        if (res == null) return false;
        Option<Term> status = res.getSolution();

        if (status.isEmpty()){
            return false;
        }
  //      try {
            unify(result, status.get());
//        } catch (InvalidTermException e) {
//            throw InterpreterError.syntax_error(engineManager, e);
//        }
        return true;
    }

    public boolean thread_read_2(Term id, Term result) throws InterpreterError {
        id = id.getTerm();
        if (!(id instanceof Int))
            throw InterpreterError.type_error(engineManager, 1, "integer", id);
        Solution res = engineManager.read(((Int) id).intValue());
        if (res == null) return false;
        Option<Term> status = res.getSolution();

        if (status.isEmpty()){
            return false;
        }
      //  try {
            unify(result, status.get());
//        } catch (InvalidTermException e) {
//            throw InterpreterError.syntax_error(engineManager, e);
//        }
        return true;
    }

    public boolean thread_has_next_1(Term id) throws InterpreterError {
        id = id.getTerm();
        if (!(id instanceof Int)) {
            throw InterpreterError.type_error(engineManager, 1, "integer", id);
        }
        return engineManager.hasNext(((Int) id).intValue());
    }


    public boolean thread_next_sol_1(Term id) throws InterpreterError {
        id = id.getTerm();
        if (!(id instanceof Int)) {
            throw InterpreterError.type_error(engineManager, 1, "integer", id);
        }
        return engineManager.nextSolution(((Int) id).intValue());
    }

    public boolean thread_detach_1(Term id) throws InterpreterError {
        id = id.getTerm();
        if (!(id instanceof Int)) {
            throw InterpreterError.type_error(engineManager, 1, "integer", id);
        }
        engineManager.detach(((Int) id).intValue());
        return true;
    }

    public boolean thread_sleep_1(Term millisecs) throws InterpreterError {
        millisecs = millisecs.getTerm();
        if (!(millisecs instanceof Int))
            throw InterpreterError.type_error(engineManager, 1,
                    "integer", millisecs);
        long time = ((Int) millisecs).intValue();
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println("ERRORE SLEEP");
            return false;
        }
        return true;
    }

    public boolean thread_send_msg_2(Term id, Term msg) throws InterpreterError {
        id = id.getTerm();
        if (id instanceof Int) {
            return engineManager.sendMsg(((Int) id).intValue(), msg);
        } else if (!id.isAtomic() || !id.isAtom()) {
            throw InterpreterError.type_error(engineManager, 1, "atom, atomic or integer", id);
        }
        return engineManager.sendMsg(id.toString(), msg);
    }

    public boolean thread_get_msg_2(Term id, Term msg) throws InterpreterError {
        id = id.getTerm();
        if (id instanceof Int) {
            return engineManager.getMsg(((Int) id).intValue(), msg);
        } else if (!id.isAtom() || !id.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom, atomic or integer", id);
        }
        return engineManager.getMsg(id.toString(), msg);
    }

    public boolean thread_peek_msg_2(Term id, Term msg) throws InterpreterError {
        id = id.getTerm();
        if (id instanceof Int) {
            return engineManager.peekMsg(((Int) id).intValue(), msg);
        } else if (!id.isAtom() || !id.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom, atomic or integer", id);
        }
        return engineManager.peekMsg(id.toString(), msg);
    }

    public boolean thread_wait_msg_2(Term id, Term msg) throws InterpreterError {
        id = id.getTerm();
        if (id instanceof Int) {
            return engineManager.waitMsg(((Int) id).intValue(), msg);
        } else if (!id.isAtom() || !id.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom, atomic or integer", id);
        }
        return engineManager.waitMsg(id.toString(), msg);
    }

    public boolean thread_remove_msg_2(Term id, Term msg) throws InterpreterError {
        id = id.getTerm();
        if (id instanceof Int) {
            return engineManager.removeMsg(((Int) id).intValue(), msg);
        } else if (!id.isAtom() || !id.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom, atomic or integer", id);
        }
        return engineManager.removeMsg(id.toString(), msg);
    }

    public boolean msg_queue_create_1(Term q) throws InterpreterError {
        q = q.getTerm();
        if (!q.isAtomic() || !q.isAtom()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", q);
        }
        return engineManager.createQueue(q.toString());
    }

    public boolean msg_queue_destroy_1(Term q) throws InterpreterError {
        q = q.getTerm();
        if (!q.isAtomic() || !q.isAtom()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", q);
        }
        engineManager.destroyQueue(q.toString());
        return true;
    }

    public boolean msg_queue_size_2(Term id, Term n) throws InterpreterError {
        id = id.getTerm();
        int size;
        if (id instanceof Int) {
            size = engineManager.queueSize(((Int) id).intValue());
        } else {
            if (!id.isAtom() || !id.isAtomic()) {
                throw InterpreterError.type_error(engineManager, 1, "atom, atomic or integer", id);
            }
            size = engineManager.queueSize(id.toString());
        }
        if (size < 0) {
            return false;
        } else {
            return unify(n, new Int(size));
        }
    }

    public boolean mutex_create_1(Term mutex) throws InterpreterError {
        mutex = mutex.getTerm();
        if (!mutex.isAtom() || !mutex.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", mutex);
        }
        return engineManager.createLock(mutex.toString());
    }

    public boolean mutex_destroy_1(Term mutex) throws InterpreterError {
        mutex = mutex.getTerm();
        if (!mutex.isAtom() || !mutex.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", mutex);
        }
        engineManager.destroyLock(mutex.toString());
        return true;
    }

    public boolean mutex_lock_1(Term mutex) throws InterpreterError {
        mutex = mutex.getTerm();
        if (!mutex.isAtom() || !mutex.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", mutex);
        }
        return engineManager.mutexLock(mutex.toString());
    }

    public boolean mutex_trylock_1(Term mutex) throws InterpreterError {
        mutex = mutex.getTerm();
        if (!mutex.isAtom() || !mutex.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", mutex);
        }
        return engineManager.mutexTryLock(mutex.toString());
    }

    public boolean mutex_unlock_1(Term mutex) throws InterpreterError {
        mutex = mutex.getTerm();
        if (!mutex.isAtom() || !mutex.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", mutex);
        }
        return engineManager.mutexUnlock(mutex.toString());
    }

    public boolean mutex_isLocked_1(Term mutex) throws InterpreterError {
        mutex = mutex.getTerm();
        if (!mutex.isAtom() || !mutex.isAtomic()) {
            throw InterpreterError.type_error(engineManager, 1, "atom or atomic", mutex);
        }
        return engineManager.isLocked(mutex.toString());
    }

    public boolean mutex_unlock_all_0() {
        engineManager.unlockAll();
        return true;
    }

    public String getTheory() {
        return
                "thread_execute(ID, GOAL):- thread_create(ID, GOAL), '$next'(ID). \n" +
                        "'$next'(ID). \n" +
                        "'$next'(ID) :- '$thread_execute2'(ID). \n" +
                        "'$thread_execute2'(ID) :- not thread_has_next(ID),!,false. \n" +
                        "'$thread_execute2'(ID) :- thread_next_sol(ID). \n" +
                        "'$thread_execute2'(ID) :- '$thread_execute2'(ID). \n" +

                        "with_mutex(MUTEX,GOAL):-mutex_lock(MUTEX), call(GOAL), !, mutex_unlock(MUTEX).\n" +
                        "with_mutex(MUTEX,GOAL):-mutex_unlock(MUTEX), fail."
                ;

    }
}
