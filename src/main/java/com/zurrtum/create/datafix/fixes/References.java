package com.zurrtum.create.datafix.fixes;

import com.mojang.datafixers.DSL.TypeReference;

public class References {
    public static final TypeReference CONTRAPTION = reference("contraption");

    private static TypeReference reference(final String id) {
        return new TypeReference() {
            @Override
            public String typeName() {
                return "create:" + id;
            }

            @Override
            public String toString() {
                return "@create:" + id;
            }
        };
    }
}
