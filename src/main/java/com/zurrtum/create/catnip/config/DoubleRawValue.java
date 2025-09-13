package com.zurrtum.create.catnip.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DoubleRawValue {
    private final Builder builder;
    private final double defaultValue;
    private final JsonObject parent;
    private final String name;
    private double value;

    DoubleRawValue(Builder builder, JsonObject parent, String name, double defaultValue) {
        this.builder = builder;
        this.parent = parent;
        this.name = name;
        this.defaultValue = defaultValue;
        read();
    }

    public double get() {
        return value;
    }

    public double getDefault() {
        return defaultValue;
    }

    public void set(double value) {
        this.value = value;
        parent.addProperty(name, value);
    }

    public void read() {
        JsonPrimitive primitive = parent.getAsJsonPrimitive(name);
        if (primitive == null) {
            set(defaultValue);
        } else {
            value = primitive.getAsDouble();
        }
    }

    public void save() {
        builder.save();
    }
}
