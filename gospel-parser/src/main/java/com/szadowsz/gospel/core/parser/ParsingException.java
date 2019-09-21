package com.szadowsz.gospel.core.parser;

public class ParsingException extends RuntimeException {
    private final String input;
    private final int line;
    private final int column;
    private final String offendingSymbol;

    public ParsingException(String input, String offendingSymbol, int line, int column, String message, Throwable throwable) {
        super(message, throwable);
        this.input = input;
        this.line = line;
        this.column = column;
        this.offendingSymbol = offendingSymbol;
    }

    public String getInput() {
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

    @Override
    public String toString() {
        return "ParsingException{" +
               "message='" + getMessage().replace("\\n", "\\\\n") + '\'' +
               ", line=" + line +
               ", column=" + column +
               ", offendingSymbol='" + offendingSymbol + '\'' +
               '}';
    }
}
