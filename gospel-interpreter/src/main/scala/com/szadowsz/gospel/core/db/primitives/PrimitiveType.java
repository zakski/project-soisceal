package com.szadowsz.gospel.core.db.primitives;

public enum PrimitiveType {
    DIRECTIVE("directive"), FUNCTOR("functor"), PREDICATE("predicate");

    protected final String type;

    PrimitiveType(String t){
        type = t;
    }


    public static PrimitiveType forName(String id) {
        for (PrimitiveType t : values()){
            if (t.type.equals(id)){
                return t;
            }
        }
        throw new IllegalArgumentException(id + " is not a value Primitive Type");
    }
}
