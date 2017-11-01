package alice.tuprologx.pj.model;

import alice.tuprologx.pj.annotations.Termifiable;
import com.szadowsz.gospel.core.data.Struct;

/**
 * @author maurizio
 */
public abstract class Term<X extends Term<?>> {

    // Added by ED 2013-05-21 following MC suggestion
    @SuppressWarnings("unchecked")
    static <S, T> T uncheckedCast(S s) {
        return (T) s;
    }
    // END ADDITION

    static <Z extends Term<?>> Z fromJava(Object o) {
        if (o instanceof Integer) {
            //return (Z)new Int((Integer)o);
            return uncheckedCast(new Int((Integer) o));
        } else if (o instanceof java.lang.Double) {
            //return (Z)new Double((java.lang.Double)o);
            return uncheckedCast(new Double((java.lang.Double) o));
        } else if (o instanceof String) {
            //return (Z)new Atom((String)o);
            return uncheckedCast(new Atom((String) o));
        } else if (o instanceof Boolean) {
            //return (Z)new Bool((Boolean)o);
            return uncheckedCast(new Bool((Boolean) o));
        } else if (o instanceof java.util.Collection<?>) {
            // return (Z)new List<Term<?>>((java.util.Collection<?>)o);
            return uncheckedCast(new List<>((java.util.Collection<?>) o));
        } else if (o instanceof Term<?>[]) {
            // return (Z)new Cons<Term<?>, Compound<?>>("_",(Term<?>[])o);
            return uncheckedCast(new Cons<>("_", (Term<?>[]) o));
        } else if (o instanceof Term<?>) {
            //return (Z)o;
            return uncheckedCast(o);
        } else if (o.getClass().isAnnotationPresent(Termifiable.class)) {
            // return (Z)new JavaTerm<Object>(o);
            return uncheckedCast(new JavaTerm<>(o));
        }
        /*else {
			throw new UnsupportedOperationException();
		}*/
        else {
            // return (Z)new JavaObject<Object>(o);
            return uncheckedCast(new JavaObject<>(o));
        }
    }

    public static <Z extends Term<?>> Z unmarshal(com.szadowsz.gospel.core.data.Term t) {
        if (Int.matches(t)) {
            // return (Z)Int.unmarshal((com.szadowsz.gospel.core.data.Int)t);
            return uncheckedCast(Int.unmarshal((com.szadowsz.gospel.core.data.Int) t));
        } else if (Double.matches(t)) {
            //return (Z)Double.unmarshal((com.szadowsz.gospel.core.data.Double)t);
            return uncheckedCast(Double.unmarshal((com.szadowsz.gospel.core.data.Double) t));
        } else if (JavaObject.matches(t)) {
            //return (Z)JavaObject.unmarshalObject((com.szadowsz.gospel.core.data.Struct)t);
            return uncheckedCast(JavaObject.unmarshalObject((Struct) t));
        } else if (Atom.matches(t)) {
            //return (Z)Atom.unmarshal((com.szadowsz.gospel.core.data.Struct)t);
            return uncheckedCast(Atom.unmarshal((Struct) t));
        } else if (Bool.matches(t)) {
            //return (Z)Bool.unmarshal((com.szadowsz.gospel.core.data.Struct)t);
            return uncheckedCast(Bool.unmarshal((Struct) t));
        } else if (List.matches(t)) {
            //return (Z)List.unmarshal((com.szadowsz.gospel.core.data.Struct)t);
            return uncheckedCast(List.unmarshal((Struct) t));
        } else if (JavaTerm.matches(t)) {
            //return (Z)JavaTerm.unmarshalObject((com.szadowsz.gospel.core.data.Struct)t.getTerm());
            return uncheckedCast(JavaTerm.unmarshalObject((Struct) t.getTerm()));
        } else if (Cons.matches(t)) {
            //return (Z)Cons.unmarshal((com.szadowsz.gospel.core.data.Struct)t);
            return uncheckedCast(Cons.unmarshal((Struct) t));
        } else if (Var.matches(t)) {
            //return (Z)Var.unmarshal((com.szadowsz.gospel.core.data.Var)t);
            return uncheckedCast(Var.unmarshal((com.szadowsz.gospel.core.data.Var) t));
        } else {
            System.out.println(t);
            throw new UnsupportedOperationException();
        }
    }

    protected abstract <Z> Z toJava(); // {return null;}

    public abstract com.szadowsz.gospel.core.data.Term marshal() /*{
            throw new UnsupportedOperationException();
        }*/;
}













