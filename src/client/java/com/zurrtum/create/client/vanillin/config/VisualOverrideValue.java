package com.zurrtum.create.client.vanillin.config;

import org.jspecify.annotations.Nullable;

public enum VisualOverrideValue {
    DEFAULT,
    DISABLE;

    @Nullable
    public static VisualOverrideValue parse(String string) {
        if (string.equals("default")) {
            return DEFAULT;
        } else if (string.equals("disable")) {
            return DISABLE;
        }
        return null;
    }
}
