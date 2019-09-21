package com.szadowsz.gospel.core.parser;

import com.szadowsz.gospel.core.exception.InvalidTermException;
import com.szadowsz.gospel.core.exception.InvalidTheoryException;
import org.antlr.v4.runtime.Token;

public class ParseException extends RuntimeException {
    private Object input;
    private int line;
    private int column;
    private String offendingSymbol;
    private int clauseIndex = -1;

    public ParseException(Object input, String offendingSymbol, int line, int column, String message, Throwable throwable) {
        super(message, throwable);
        this.input = input;
        this.line = line;
        this.column = column;
        this.offendingSymbol = offendingSymbol;
    }

    public ParseException(Object input, Token token, String message, Throwable throwable) {
        this(input, token.getText(), token.getLine(), token.getCharPositionInLine(), message, throwable);
    }

    public ParseException(Token token, String message, Throwable throwable) {
        this(null, token.getText(), token.getLine(), token.getCharPositionInLine(), message, throwable);
    }

    public ParseException(Token token, String message) {
        this(null, token.getText(), token.getLine(), token.getCharPositionInLine(), message, null);
    }

    public ParseException(Token token, Throwable throwable) {
        this(null, token.getText(), token.getLine(), token.getCharPositionInLine(), "", throwable);
    }

    public Object getInput() {
        return input;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getOffendingSymbol() {
        return offendingSymbol;
    }

    public void setInput(final Object input) {
        this.input = input;
    }

    public void setLine(final int line) {
        this.line = line;
    }

    public void setColumn(final int column) {
        this.column = column;
    }

    public void setOffendingSymbol(final String offendingSymbol) {
        this.offendingSymbol = offendingSymbol;
    }

    public int getClauseIndex() {
        return clauseIndex;
    }

    public void setClauseIndex(final int clauseIndex) {
        this.clauseIndex = clauseIndex;
    }

    @Override
    public String toString() {
        return "ParseException{" +
               "message='" + getMessage().replace("\\n", "\\\\n") + '\'' +
               ", line=" + line +
               ", column=" + column +
               ", offendingSymbol='" + offendingSymbol + '\'' +
               '}';
    }

    public InvalidTermException toInvalidTermException() {
        return new InvalidTermException(getMessage(), this, getOffendingSymbol(),getLine(), getColumn());
    }

    public InvalidTheoryException toInvalidTheoryException() {
        return toInvalidTheoryException(getClauseIndex());
    }

    public InvalidTheoryException toInvalidTheoryException(int clause) {
        return new InvalidTheoryException(getMessage(), this, getOffendingSymbol(), clause,getLine(), getColumn());
    }
}
