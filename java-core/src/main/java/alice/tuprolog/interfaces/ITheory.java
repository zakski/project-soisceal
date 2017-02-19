package alice.tuprolog.interfaces;

import alice.tuprolog.*;
import alice.tuprolog.json.JSONSerializerManager;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created on 19/02/2017.
 */
public interface ITheory extends Serializable {
    //Alberto
    static Theory fromJSON(String jsonString){
        return JSONSerializerManager.fromJSON(jsonString, Theory.class);
    }

    Iterator<? extends Term> iterator(Prolog engine);

    /**
     * Adds (appends) a theory to this.
     *
     * @param th is the theory to be appended
     * @throws s InvalidTheoryException if the theory object are not compatibles (they are
     *  compatibles when both have been built from texts or both from clause lists)
     */
    void append(ITheory th) throws InvalidTheoryException;

    public boolean isTextual();

    public Struct getClauseListRepresentation();

    String toString();

    //Alberto
    String toJSON();
}
