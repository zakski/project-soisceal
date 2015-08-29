package com.szadowsz.gospel.core.interfaces;

import com.szadowsz.gospel.core.OperatorManager;
import com.szadowsz.gospel.core.Parser;
import com.szadowsz.gospel.core.data.Term;

import java.util.HashMap;

public class ParserFactory {
	
	/**
     * Creating a parser with default operator interpretation
     */
	public static IParser createParser(String theory) {
		return new Parser(theory);
	}
	
	/**
     * creating a parser with default operator interpretation
     */
    public static IParser createParser(String theory, HashMap<Term, Integer> mapping) {
    	return new Parser(theory, mapping);
    }    
	
	/**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public static IParser createParser(IOperatorManager op, String theory) {
    	return new Parser((OperatorManager)op, theory);
    }
    
    /**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public static IParser createParser(IOperatorManager op, String theory, HashMap<Term, Integer> mapping) {
    	return new Parser((OperatorManager)op, theory, mapping);
    }

}
