package alice.tuprolog.interfaces;

import alice.tuprolog.Struct;
import alice.tuprolog.Term;

/**
 * Created on 15/02/2017.
 */
public interface IFlagManager {

    public boolean isOccursCheckEnabled();

    public boolean isModifiable(String name);

    public boolean isValidValue(String name, Term value);

    public Struct getPrologFlagList();

    public Term getFlag(String name);

    public boolean defineFlag(String name, Struct valueList, Term defValue, boolean modifiable, String libName);

    public boolean setFlag(String name, Term value);
}
