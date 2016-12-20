/*Castagna 06/2011*/
package alice.tuprolog.interfaces;

import alice.tuprolog.Number;
import alice.tuprolog.Struct;
import alice.tuprolog.Var;

public interface TermVisitor {
	void visit(Struct s);
	void visit(Var v);
	void visit(Number n);
}
/**/