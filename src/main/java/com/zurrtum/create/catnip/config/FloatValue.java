package com.zurrtum.create.catnip.config;

import com.google.gson.JsonObject;

public class FloatValue extends ConfigValue<Float> {
    public float min;
    public float max;

    FloatValue(Builder builder, JsonObject parent, String name, float def, float min, float max) {
        super(builder.comment("Default: " + def, "Range: " + min + " ~ " + max), parent, name, def);
        float value = get();
        if (value < min) {
            set(min);
        } else if (value > max) {
            set(max);
        }
        this.min = min;
        this.max = max;
    }
}
