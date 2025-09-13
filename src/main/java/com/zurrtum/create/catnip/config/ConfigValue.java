package com.zurrtum.create.catnip.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class ConfigValue<T> {
    private final Builder builder;
    private final T defaultValue;
    private JsonObject object;
    private T value;

    ConfigValue(Builder builder, JsonObject parent, String name, T def) {
        this.builder = builder;
        defaultValue = def;
        object = parent.getAsJsonObject(name);
        if (object == null) {
            object = new JsonObject();
            builder.addComments(object);
            set(defaultValue);
            parent.add(name, object);
        } else {
            builder.addComments(object);
            read();
        }
    }

    public T get() {
        return value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public void set(T value) {
        this.value = value;
        object.add("value", Builder.GSON.toJsonTree(value));
    }

    @SuppressWarnings("unchecked")
    public void read() {
        JsonElement element = object.get("value");
        if (element == null) {
            set(defaultValue);
        } else {
            value = Builder.GSON.fromJson(element, (Class<T>) defaultValue.getClass());
        }
    }

    public void save() {
        builder.save();
    }
}
