package com.zurrtum.create.catnip.config;

import com.google.gson.JsonObject;

public class IntValue extends ConfigValue<Integer> {
    public int min;
    public int max;

    IntValue(Builder builder, JsonObject parent, String name, int def, int min, int max) {
        super(
            builder.comment(
                "Default: " + def,
                "Range: " + (max == Integer.MAX_VALUE ? "> " + min : min == Integer.MIN_VALUE ? "< " + max : min + " ~ " + max)
            ),
            parent,
            name,
            def
        );
        setMaxmin(max, min);
    }

    public void setMaxmin(int max, int min) {
        Integer value = get();
        if (value < min) {
            set(min);
        } else if (value > max) {
            set(max);
        }
        this.max = max;
        this.min = min;
    }
}
