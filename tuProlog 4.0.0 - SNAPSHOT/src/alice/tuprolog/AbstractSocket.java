package alice.tuprolog;
import java.net.InetAddress;
import java.util.AbstractMap;
//import java.util.ArrayList;

import alice.tuprolog.interfaces.TermVisitor;

public abstract class AbstractSocket extends Term{
	
	private static final long serialVersionUID = 1L;
	
	public abstract boolean isClientSocket();
	
	public abstract boolean isServerSocket();
	
	public abstract boolean isDatagramSocket();
	
	public abstract Object getSocket();
	
	public abstract InetAddress getAddress();
	
	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public boolean isStruct() {
		return false;
	}

	@Override
	public boolean isVar() {
		return false;
	}

	@Override
	public boolean isEmptyList() {
		return false;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public boolean isAtom() {
		return false;
	}

	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public boolean isGreater(Term t) {
		return false;
	}

	@Override
	public boolean isEqual(Term t) {
		return false;
	}

	@Override
	public Term getTerm() {
		return this;
	}

	@Override
	public void free() {
	}

	@Override
	long resolveTerm(long count) {
		return count;
	}

	@Override
	Term copy(AbstractMap<Var, Var> vMap, int idExecCtx) {
		return this;
	}

	@Override
	Term copy(AbstractMap<Var, Var> vMap, AbstractMap<Term, Var> substMap) {
		return this;
	}
	
	@Override //Alberto
	public Term copyAndRetainFreeVar(AbstractMap<Var,Var> vMap, int idExecCtx) {
		return this;
	}

	@Override
	public void accept(TermVisitor tv) {
	}
}