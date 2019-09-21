package com.szadowsz.gospel.core.parser;

import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
public class PrologLexerTest {

    private static PrologLexer lexerForString(String input) {
        return new PrologLexer(CharStreams.fromString(input));
    }

    private static TokenStream tokenStreamFromLexer(DynamicLexer lexer) {
        return new BufferedTokenStream(lexer);
    }

    private static List<Token> tokenStreamToList(TokenStream stream) {
        final LinkedList<Token> result = new LinkedList<>();

        int i = 0;
        try {
            stream.consume();
        } catch (IllegalStateException e) {
            return result;
        }
        do {
            result.add(stream.get(i++));
            stream.consume();
        } while (stream.LA(1) != TokenStream.EOF);
        result.add(stream.get(i));

        return result;
    }

    private static void assertTokenIs(Token token, int type, String text) {
        Assert.assertEquals("'" + text + "'", "'" + token.getText() + "'");
        Assert.assertEquals(PrologLexer.VOCABULARY.getSymbolicName(type), PrologLexer.VOCABULARY.getSymbolicName(token.getType()));
    }

    @Test
    public void testPrologLexerDoesNotAcceptEmptyStrings() {
        final PrologLexer lexer = lexerForString("");
        final TokenStream tokenStream = tokenStreamFromLexer(lexer);
        final List<Token> tokens = tokenStreamToList(tokenStream);

        Assert.assertEquals(tokens.size(), 0);
    }

    @Test
    public void testPrologLexerRecognisesAtoms() {
        final PrologLexer lexer = lexerForString("1 + a + \"b\" + 'c'");
        final TokenStream tokenStream = tokenStreamFromLexer(lexer);
        final List<Token> tokens = tokenStreamToList(tokenStream);

        int i = 0;

        assertTokenIs(tokens.get(i++), PrologLexer.INTEGER, "1");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.ATOM, "a");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.DQ_STRING, "b");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.SQ_STRING, "c");
        Assert.assertEquals(tokens.size(), i);
    }

    @Test
    public void testPrologLexerRecognisesOperators() {
        final PrologLexer lexer = lexerForString("1 + a + \"b\" - 'c' dada a");
        lexer.addOperators("+", "-", "dada");
        final TokenStream tokenStream = tokenStreamFromLexer(lexer);
        final List<Token> tokens = tokenStreamToList(tokenStream);

        int i = 0;

        assertTokenIs(tokens.get(i++), PrologLexer.INTEGER, "1");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.ATOM, "a");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.DQ_STRING, "b");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "-");
        assertTokenIs(tokens.get(i++), PrologLexer.SQ_STRING, "c");
        assertTokenIs(tokens.get(i++), PrologLexer.OPERATOR, "dada");
        assertTokenIs(tokens.get(i++), PrologLexer.ATOM, "a");
        Assert.assertEquals(tokens.size(), i);
    }

    @Test
    public void testPrologLexerRecognisesVariables() {
        final PrologLexer lexer = lexerForString("_ + A + _B is _1 + _a + _+");
        lexer.addOperators("+", "is");
        final TokenStream tokenStream = tokenStreamFromLexer(lexer);
        final List<Token> tokens = tokenStreamToList(tokenStream);

        int i = 0;

        assertTokenIs(tokens.get(i++), PrologLexer.VARIABLE, "_");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.VARIABLE, "A");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.VARIABLE, "_B");
        assertTokenIs(tokens.get(i++), PrologLexer.OPERATOR, "is");
        assertTokenIs(tokens.get(i++), PrologLexer.VARIABLE, "_1");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.VARIABLE, "_a");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        assertTokenIs(tokens.get(i++), PrologLexer.VARIABLE, "_");
        assertTokenIs(tokens.get(i++), PrologLexer.SIGN, "+");
        Assert.assertEquals(tokens.size(), i);
    }
}
