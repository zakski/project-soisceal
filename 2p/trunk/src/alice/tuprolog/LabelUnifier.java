package alice.tuprolog;

import java.util.List;

public abstract class LabelUnifier {
	
	public abstract Label unifyLabel(Label label, Label labelToUnify);
	 
	public abstract boolean isCompatible(Label label, Term term);
	
	public abstract boolean isEvaluable(Label label);
	
	public abstract Label evaluate(Label label, List<Var> vl1);
	
	public String toString(){
		return this.getClass().getName();
	}

}
