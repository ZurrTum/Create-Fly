package com.zurrtum.create.catnip.config;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
    EnumValue(Builder builder, JsonObject parent, String name, T def) {
        super(
            builder.comment("Allowed Values: " + Arrays.stream(def.getDeclaringClass().getEnumConstants()).map(Enum::name)
                .collect(Collectors.joining(", "))), parent, name, def
        );
    }
}
