package alice.tuprolog;

import java.util.AbstractMap;
import java.util.List;

public class Label extends Term{
	
	private static final long serialVersionUID = 1L;
	private Term labelTerm;
	private Term labelEvaluation;

	public Label(Term t){
		labelTerm=t;
	}
	
	public Term getLabelTerm() {
		return labelTerm;
	}

	public void setLabelTerm(Term labelTerm) {
		this.labelTerm = labelTerm;
	}
	
	
	@Override
	public boolean isNumber() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStruct() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVar() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmptyList() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAtomic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCompound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAtom() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isList() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGround() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGreater(Term t) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isEqual(Term t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Term getTerm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub
		
	}

	@Override
	long resolveTerm(long count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	Term copy(AbstractMap<Var, Var> vMap, int idExecCtx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Term copy(AbstractMap<Var, Var> vMap, AbstractMap<Term, Var> substMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void accept(TermVisitor tv) {
		// TODO Auto-generated method stub
		
	}
	
	public Label unifyLabel(Label labelToUnify){
		LabelUnifier lu = LabelUnifierManager.getLabelUnifier();
//		System.out.println("LABEL unif LABEL uso "+lu);
//		System.out.println("LABEL unif LABEL label "+this);
//		System.out.println("LABEL unif LABEL labelToUnify "+labelToUnify);
		return lu.unifyLabel(this, labelToUnify);
	};
	
	public boolean isCompatible(Term termToLink){
		LabelUnifier lu = LabelUnifierManager.getLabelUnifier();
		//System.out.println("LABEL isCompatible "+lu.toString());
		return lu.isCompatible(this, termToLink);
	};
	
	public boolean isEvaluable(){
		LabelUnifier lu = LabelUnifierManager.getLabelUnifier();
		//System.out.println("LABEL isEvaluable "+lu.toString());
		return lu.isEvaluable(this);
	};
	
	public void evaluate(List<Var> vl1){
		LabelUnifier lu = LabelUnifierManager.getLabelUnifier();
		//System.out.println("LABEL evaluate "+lu.toString());
		Label l = lu.evaluate(this,vl1);
		if (l != null)
			labelEvaluation=l.getLabelTerm();
		//System.out.println("LABEL finito evaluate "+labelEvaluation);
		//return lu.evaluate(this);
	};
	
	public String toString(){
		String s;
		if(labelEvaluation !=null){
			//System.out.println("LABEL toString "+labelEvaluation);
			s=labelEvaluation.toString();
		}
		else
			s = labelTerm.toString();
		return s;
	}
	
	public boolean equals(Object t) {
        if (!(t instanceof Label))
            return false;
        return labelTerm.isEqual(((Label)t).getLabelTerm());
    }

	@Override
	public Term copyAndRetainFreeVar(AbstractMap<Var, Var> vMap, int idExecCtx) {
		// TODO Auto-generated method stub
		return null;
	}

}
