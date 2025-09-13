package com.zurrtum.create.catnip.config;

import com.google.gson.JsonObject;

public class BooleanValue extends ConfigValue<Boolean> {
    BooleanValue(Builder builder, JsonObject parent, String name, Boolean def) {
        super(builder, parent, name, def);
    }
}
