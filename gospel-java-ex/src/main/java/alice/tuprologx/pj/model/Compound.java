package alice.tuprologx.pj.model;

/**
 * @author Maurizio
 */
abstract class Compound<X extends Compound<?>> extends Term<X> {
    //public abstract class Compound<X extends Term<?>> extends Term<Compound<X>> {
    protected abstract int arity();

    protected abstract String getName();
}
