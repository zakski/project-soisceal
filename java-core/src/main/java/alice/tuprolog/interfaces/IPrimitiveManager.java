package alice.tuprolog.interfaces;

import alice.tuprolog.IPrimitives;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;

public interface IPrimitiveManager {

    public void createPrimitiveInfo(IPrimitives src);

    public void deletePrimitiveInfo(IPrimitives src);

    public void identifyPredicate(Term term);

    public boolean evalAsDirective(Struct d) throws Throwable;

    public boolean containsTerm(String name, int nArgs);

}
