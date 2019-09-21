//package com.szadowsz.gospel.core.parser;
//
//import com.szadowsz.gospel.core.data.Struct;
//import com.szadowsz.gospel.core.db.operators.Operator;
//
//import java.lang.ref.WeakReference;
//import java.util.Objects;
//import java.util.function.Consumer;
//
//class DynamicOpListener extends PrologParserBaseListener {
//
//    private final WeakReference<PrologParser> parser;
//    private final Consumer<Operator> operatorDefinedCallback;
//
//    public static DynamicOpListener of(PrologParser parser) {
//        return new DynamicOpListener(parser, null);
//    }
//
//    public static DynamicOpListener of(PrologParser parser, Consumer<Operator> operatorDefinedCallback) {
//        return new DynamicOpListener(parser, operatorDefinedCallback);
//    }
//
//    private DynamicOpListener(PrologParser parser, final Consumer<Operator> operatorDefinedCallback) {
//        this.parser = new WeakReference<>(Objects.requireNonNull(parser));
//        this.operatorDefinedCallback = operatorDefinedCallback;
//    }
//
//    @Override
//    public void exitClause(PrologParser.ClauseContext ctx) {
//
//        final PrologParser.ExpressionContext expr = ctx.expression();
//
//        if (ctx.exception != null) {
//            return;
//        }
//
//        if (expr.op != null && ":-".equals(expr.op.symbol.getText()) && Associativity.PREFIX.contains(expr.associativity)) {
//            final Struct directive = ctx.accept(PrologExpressionVisitor.get()).castTo(Struct.class);
//
//            if (directive.getArity() == 1 && directive.getArg(0) instanceof Struct) {
//                final Struct op = directive.getArg(0).castTo(Struct.class);
//
//                if ("op".equals(op.getName()) && op.getArity() == 3
//                        && op.getArg(0) instanceof Number && op.getArg(1).isAtom() && op.getArg(2).isAtom()) {
//
//                    final int priority = min(
//                            PrologParser.TOP,
//                            max(
//                                    PrologParser.BOTTOM,
//                                    op.getArg(0).castTo(Number.class).intValue()
//                            )
//                    );
//
//                    final Associativity associativity = Associativity.valueOf(op.getArg(1).castTo(Struct.class).getName().toUpperCase());
//
//                    final String functor = op.getArg(2).castTo(Struct.class).getName();
//
//                    Objects.requireNonNull(parser.get()).addOperator(functor, associativity, priority);
//
//                    onOperatorDefined(Operator.of(functor, associativity, priority));
//                }
//            }
//        }
//    }
//
//    public void onOperatorDefined(Operator operator) {
//        if (operatorDefinedCallback != null) {
//            operatorDefinedCallback.accept(operator);
//        }
//    }
//}
//
