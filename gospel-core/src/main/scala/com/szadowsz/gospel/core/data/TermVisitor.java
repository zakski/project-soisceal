/*Castagna 06/2011*/
package com.szadowsz.gospel.core.data;

interface TermVisitor {
	void visit(Struct s);
	void visit(Var v);
	void visit(Number n);
}
/**/