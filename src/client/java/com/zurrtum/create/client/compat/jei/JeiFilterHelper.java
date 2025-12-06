package com.zurrtum.create.client.compat.jei;

import mezz.jei.api.runtime.IJeiRuntime;

import java.util.function.Consumer;

public class JeiFilterHelper {
    private static Consumer<String> setText;

    static void setRuntime(IJeiRuntime runtime) {
        setText = runtime.getIngredientFilter()::setFilterText;
    }

    public static void setText(String string) {
        if (setText != null) {
            setText.accept(string);
        }
    }
}
