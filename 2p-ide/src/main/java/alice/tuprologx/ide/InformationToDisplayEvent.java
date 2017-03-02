package alice.tuprologx.ide;

import alice.tuprolog.Prolog;
import com.szadowsz.gospel.core.PrologEngine;
import com.szadowsz.gospel.core.Solution;
import com.szadowsz.gospel.core.event.interpreter.PrologEvent;
import com.szadowsz.gospel.core.event.interpreter.QueryEvent;

import java.util.ArrayList;

/**
 * This class represents events concerning information to display in the console.
 * 
 * 
 *
 */
@SuppressWarnings("serial")
public class InformationToDisplayEvent extends PrologEvent {

    private ArrayList<QueryEvent> queryEventList;
    private ArrayList<String> queryEventListString;
    private int solveType;

    public InformationToDisplayEvent(PrologEngine source, ArrayList<QueryEvent> queryEventList, ArrayList<String> queryEventListString, int solveType){
        super(source);
        this.queryEventList=queryEventList;
        this.queryEventListString=queryEventListString;
        this.solveType=solveType;
    }
    
    public int getSolveType()
    {
        return solveType;
    }

    public QueryEvent[] getQueryResults()
    {
        return (QueryEvent[]) queryEventList.toArray(new QueryEvent[queryEventList.size()]);
    }
    
    public ArrayList<String> getQueryResultsString()
    {
        return queryEventListString;
    }

    public Solution getQueryResult()
    {
        return ( (QueryEvent) queryEventList.get(0)).getSolution();
    }

    public int getListSize()
    {
        return queryEventList.size();
    }
}
