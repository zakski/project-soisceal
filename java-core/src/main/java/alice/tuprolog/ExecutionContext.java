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

package alice.tuprolog;

import java.util.*;

import alice.tuprolog.Struct;
import alice.tuprolog.SubGoalId;
import alice.tuprolog.interfaces.IEngine;
import alice.util.OneWayList;


/**
 * 
 * @author Alex Benini
 */
public class ExecutionContext {
    
    private int id;
    public int depth;
    public Struct currentGoal;
    public ExecutionContext fatherCtx;
    public SubGoalId fatherGoalId;
    public Struct clause;
    public Struct headClause;
    public SubGoalStore goalsToEval;
    public OneWayList<List<Var>> trailingVars;
    public OneWayList<List<Var>> fatherVarsList;
    public ChoicePointContext choicePointAfterCut;
    public boolean haveAlternatives;
    
    public ExecutionContext(int id) {
        this.id=id;
    }
    
    public int getId() { return id; }
    
    public String toString(){
        return "        id: "+id+"\n"+
        "      currentGoal: "+currentGoal+"\n"+
        "           clause: "+clause+"\n"+
        "     subGoalStore: "+goalsToEval+"\n"+
        "     trailingVars: "+trailingVars+"\n";
    }
    
    public int getDepth() {
        return depth;
    }
    
    public Struct getCurrentGoal() {
        return currentGoal;
    }
    
    public SubGoalId getFatherGoalId() {
        return fatherGoalId;
    }
    
    public Struct getClause() {
        return clause;
    }
    
    public Struct getHeadClause() {
        return headClause;
    }
    
    public SubGoalStore getSubGoalStore() {
        return goalsToEval;
    }
    
    public List<List<Var>> getTrailingVars() {
        ArrayList<List<Var>> l = new ArrayList<List<Var>>();
        OneWayList<List<Var>> t = trailingVars;
        while (t != null) {
            l.add(t.getHead());
            t = t.getTail();
        }
        return l;        
    }
    
    /**
     * Save the state of the parent context to later bring the ExectutionContext
     * objects tree in a consistent state after a backtracking step.
     */
    public void saveParentState() {
        if (fatherCtx != null) {
            fatherGoalId = fatherCtx.goalsToEval.getCurrentGoalId();
            fatherVarsList = fatherCtx.trailingVars;
        }
    }

    public boolean isHaveAlternatives() {
        return haveAlternatives;
    }

    /**
     * If no open alternatives, no other term to execute and
     * current context doesn't contain as current goal a catch or java_catch predicate ->
     * current context no more needed ->
     * reused to execute g subgoal =>
     * got TAIL RECURSION OPTIMIZATION!   
     */
   
    //Alberto
    public boolean tryToPerformTailRecursionOptimization(IEngine e)
    {
    	if(!haveAlternatives && e.getContext().goalsToEval.getCurSGId() == null && !e.getContext().goalsToEval.haveSubGoals() && !(e.getContext().currentGoal.getName().equalsIgnoreCase("catch") || e.getContext().currentGoal.getName().equalsIgnoreCase("java_catch")))
    	{
    		fatherCtx = e.getContext().fatherCtx;
    		depth = e.getContext().depth;
    		return true;
    	}
    	else
    		return false;
    }

    //Alberto
	public void updateContextAndDepth(IEngine e)
	{
		fatherCtx = e.getContext();
        depth = e.getContext().depth +1;
	}
}