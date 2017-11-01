package alice.tuprologx.pj.model;

import com.szadowsz.gospel.core.data.Term;

public class Nil extends Compound<Nil> {
    public int arity() {
        return 0;
    }


    public <Z> Z/*Object*/ toJava() {
        throw new UnsupportedOperationException();
    }

    public Term marshal() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return null;
    }
}

