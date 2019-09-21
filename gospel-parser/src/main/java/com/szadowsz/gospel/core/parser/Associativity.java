package com.szadowsz.gospel.core.parser;

import java.util.EnumSet;
import java.util.stream.Stream;

public enum Associativity {
    XF, YF, XFX, XFY, YFX, FX, FY;

    public static Associativity values(int i) {
        return values()[i];
    }

    public static EnumSet<Associativity> X_FIRST = EnumSet.of(XF, XFX, XFY, FX);
    public static EnumSet<Associativity> Y_FIRST = EnumSet.of(YF, YFX, FY);

    public static EnumSet<Associativity> INFIX = EnumSet.of(XFX, YFX, XFY);
    public static EnumSet<Associativity> PREFIX = EnumSet.of(FX, FY);
    public static EnumSet<Associativity> POSTFIX = EnumSet.of(YF, XF);

    public static EnumSet<Associativity> NON_PREFIX = EnumSet.complementOf(PREFIX);

    public static boolean isAssociativity(String value) {
        return Stream.of(values()).anyMatch(it -> it.toString().equalsIgnoreCase(value));
    }

    static {
        if (!NON_PREFIX.equals(EnumSet.of(XFX, YFX, XFY, YF, XF))) {
            throw new IllegalStateException();
        }
    }
}
