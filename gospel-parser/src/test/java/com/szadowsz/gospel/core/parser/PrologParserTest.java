package com.szadowsz.gospel.core.parser;

import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
public class PrologParserTest {

    private static PrologLexer lexerForString(String input) {
        return new PrologLexer(new ANTLRInputStream(input));
    }

    private static TokenStream tokenStreamFromLexer(DynamicLexer lexer) {
        return new BufferedTokenStream(lexer);
    }

    private static List<Token> tokenStreamToList(TokenStream stream) {
        final LinkedList<Token> result = new LinkedList<>();

        int i = 0;
        stream.consume();
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

    private static PrologParser.SingletonTermContext parseTerm(String string) {
        final PrologParser parser = createParser(string);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw e;
            }
        });
        return parser.singletonTerm();
    }

    private static PrologParser.SingletonExpressionContext parseExpression(String string) {
        final PrologParser parser = createParser(string);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw e;
            }
        });
        return parser.singletonExpression();
    }

    private static PrologParser createParser(String string) {
        return new PrologParser(tokenStreamFromLexer(lexerForString(string)));
    }

    public final static class AssertionOn<T> {
        private final T object;

        public AssertionOn(T object) {
            this.object = object;
        }

        public <U> AssertionOn<U> andThenAssert(Function<T, U> getter, Consumer<U> asserter) {
            final U property = getter.apply(object);
            asserter.accept(property);
            return new AssertionOn<>(property);
        }

        public <U> AssertionOn<U> andThenAssert(Function<T, U> getter, Predicate<U> asserter) {
            final U property = getter.apply(object);
            Assert.assertTrue(asserter.test(property));
            return new AssertionOn<>(property);
        }
    }

    public static <T, U> AssertionOn<U> assertionOn(T object, Function<T, U> getter, Consumer<U> asserter) {
        return new AssertionOn<>(object).andThenAssert(getter, asserter);
    }

    public static <T, U> AssertionOn<U> assertionOn(T object, Function<T, U> getter, Predicate<U> asserter) {
        return new AssertionOn<>(object).andThenAssert(getter, asserter);
    }

    @Test
    public void testInteger() {
        assertionOn(
                parseTerm("1"),
                PrologParser.SingletonTermContext::term,
                t -> t.isNum && !t.isStruct && !t.isExpr && !t.isList && !t.isVar
        ).andThenAssert(
                PrologParser.TermContext::number,
                n -> n.isInt && !n.isReal
        ).andThenAssert(
                PrologParser.NumberContext::integer,
                i -> Integer.parseInt(i.value.getText()) == 1
        );
    }

    @Test
    public void testReal() {
        assertionOn(
                parseTerm("1.1"),
                PrologParser.SingletonTermContext::term,
                t -> t.isNum && !t.isStruct && !t.isExpr && !t.isList && !t.isVar
        ).andThenAssert(
                PrologParser.TermContext::number,
                n -> n.isReal && !n.isInt
        ).andThenAssert(
                PrologParser.NumberContext::real,
                i -> Double.parseDouble(i.value.getText()) == 1.1
        );
    }

    @Test
    public void testAtom() {
        assertionOn(
                parseTerm("a"),
                PrologParser.SingletonTermContext::term,
                t -> t.isStruct && !t.isNum && !t.isExpr && !t.isList && !t.isVar
        ).andThenAssert(
                PrologParser.TermContext::structure,
                n -> n.arity == n.args.size()
                     && n.arity == 0
                     && !n.isList && !n.isString && !n.isTruth
                     && n.functor.getText().equals("a")
                     && n.functor.getType() == PrologLexer.ATOM
        );
    }

    @Test
    public void testString() {
        Stream.of("'a'", "\"a\"").forEach(term -> {
            assertionOn(
                    parseTerm(term),
                    PrologParser.SingletonTermContext::term,
                    t -> t.isStruct && !t.isNum && !t.isExpr && !t.isList && !t.isVar
            ).andThenAssert(
                    PrologParser.TermContext::structure,
                    n -> n.arity == n.args.size()
                         && n.arity == 0
                         && n.isString && !n.isList && !n.isTruth
                         && n.functor.getText().equals("a")
                         && (n.functor.getType() == PrologLexer.DQ_STRING || n.functor.getType() == PrologLexer.SQ_STRING)
            );
        });

    }

    @Test
    public void testTrue() {
        assertionOn(
                parseTerm("true"),
                PrologParser.SingletonTermContext::term,
                t -> t.isStruct && !t.isNum && !t.isExpr && !t.isList && !t.isVar
        ).andThenAssert(
                PrologParser.TermContext::structure,
                n -> n.arity == n.args.size()
                     && n.arity == 0
                     && n.isTruth && !n.isList && !n.isString
                     && n.functor.getText().equals("true")
                     && n.functor.getType() == PrologLexer.BOOL
        );
    }

    @Test
    public void testFalse() {
        assertionOn(
                parseTerm("fail"),
                PrologParser.SingletonTermContext::term,
                t -> t.isStruct && !t.isNum && !t.isExpr && !t.isList && !t.isVar
        ).andThenAssert(
                PrologParser.TermContext::structure,
                n -> n.arity == n.args.size()
                     && n.arity == 0
                     && n.isTruth && !n.isList && !n.isString
                     && n.functor.getText().equals("fail")
                     && n.functor.getType() == PrologLexer.BOOL
        );
    }

    @Test
    public void testEmptyList() {
        Stream.of("[]", "[ ]", "[   ]").forEach(term -> {
            assertionOn(
                    parseTerm(term),
                    PrologParser.SingletonTermContext::term,
                    t -> t.isStruct && !t.isNum && !t.isExpr && !t.isList && !t.isVar
            ).andThenAssert(
                    PrologParser.TermContext::structure,
                    n -> n.arity == n.args.size()
                         && n.arity == 0
                         && n.isList && !n.isTruth && !n.isString
                         && n.functor.getType() == PrologLexer.EMPTY_LIST
            );
        });
    }

    @Test
    public void testVar() {
        Stream.of("A", "_A", "_A1", "_1A", "A_").forEach(term -> {
            assertionOn(
                    parseTerm(term),
                    PrologParser.SingletonTermContext::term,
                    t -> t.isVar && !t.isList && !t.isStruct && !t.isNum && !t.isExpr
            ).andThenAssert(
                    PrologParser.TermContext::variable,
                    n -> !n.isAnonymous
                         && n.value.getText().contains("A")
                         && n.value.getType() == PrologLexer.VARIABLE
            );
        });
    }

    @Test
    public void testSingletonList() {
        Stream.of("[1]", "[1 ]", "[ 1]", "[ 1 ]").forEach(term -> {
            assertionOn(
                    parseTerm(term),
                    PrologParser.SingletonTermContext::term,
                    t -> t.isList && !t.isStruct && !t.isNum && !t.isExpr && !t.isVar
            ).andThenAssert(
                    PrologParser.TermContext::list,
                    n -> n.length == n.items.size()
                         && n.length == 1
                         && !n.hasTail && n.tail == null
            ).andThenAssert(
                    list -> list.items.get(0),
                    t -> t.isTerm && t.left != null && t.operators.size() == 0 && t.right.size() == 0
            ).andThenAssert(
                    e -> e.left,
                    t -> t.isNum && !t.isVar && !t.isList && !t.isStruct && !t.isExpr
            ).andThenAssert(
                    PrologParser.TermContext::number,
                    n -> n.isInt && !n.isReal
            ).andThenAssert(
                    PrologParser.NumberContext::integer,
                    i -> Integer.parseInt(i.value.getText()) == 1
            );
        });
    }
}
