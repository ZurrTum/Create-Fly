package com.zurrtum.create.catnip.config;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class Builder {
    private static final Splitter DOT_SPLITTER = Splitter.on(".");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public final File configDir;
    public final String type;
    private final List<JsonObject> stack = new LinkedList<>();
    private final List<String> comments = new LinkedList<>();
    private JsonObject object;

    Builder(String id, String type, boolean single) {
        if (single) {
            this.configDir = FabricLoader.getInstance().getConfigDir().toFile();
        } else {
            this.configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), id);
        }
        this.type = single ? id + "-" + type : type;
        if (configDir.exists()) {
            File file = new File(configDir, this.type + ".json");
            try {
                final String fileContents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                object = GSON.fromJson(fileContents, JsonObject.class);
            } catch (IOException e) {
                System.err.println("Failed to read config file: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
        if (object == null) {
            object = new JsonObject();
        }
    }

    public static <T extends ConfigBase> T create(Supplier<T> factory, String id, String type) {
        return create(factory, id, type, false);
    }

    public static <T extends ConfigBase> T create(Supplier<T> factory, String id, String type, boolean single) {
        T config = factory.get();
        Builder builder = new Builder(id, type, single);
        config.registerAll(builder);
        builder.save();
        return config;
    }

    public void pop(int depth) {
        if (depth > stack.size()) {
            throw new IllegalArgumentException("Cannot pop more than the current depth!");
        }
        for (int i = 0; i < depth; i++) {
            object = stack.removeLast();
        }
    }

    public void pop() {
        pop(1);
    }

    public Builder comment(String... value) {
        comments.addAll(Arrays.asList(value));
        return this;
    }

    public void addComments(JsonObject object) {
        switch (comments.size()) {
            case 0:
                return;
            case 1:
                object.addProperty("comment", comments.getFirst());
                break;
            default:
                object.add("comment", GSON.toJsonTree(comments));
                break;
        }
        comments.clear();
    }

    public void push(String path) {
        for (String name : DOT_SPLITTER.split(path)) {
            stack.add(object);
            JsonObject value = object.getAsJsonObject(name);
            if (value == null) {
                value = new JsonObject();
                addComments(value);
                object.add(name, value);
            } else {
                addComments(value);
            }
            object = value;
        }
    }

    public StringValue define(String name, String defaultValue) {
        return new StringValue(this, object, name, defaultValue);
    }

    public BooleanValue define(String name, boolean defaultValue) {
        return new BooleanValue(this, object, name, defaultValue);
    }

    public DoubleRawValue define(String name, double defaultValue) {
        return new DoubleRawValue(this, object, name, defaultValue);
    }

    public <T extends Enum<T>> ConfigValue<T> defineEnum(String name, T defaultValue) {
        return new EnumValue<>(this, object, name, defaultValue);
    }

    public FloatValue defineInRange(String name, float defaultValue, float min, float max) {
        return new FloatValue(this, object, name, defaultValue, min, max);
    }

    public IntValue defineInRange(String name, int defaultValue, int min, int max) {
        return new IntValue(this, object, name, defaultValue, min, max);
    }

    public void save() {
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new RuntimeException("Failed to write config file: " + configDir.getAbsolutePath());
        }
        pop(stack.size());
        String jsonStr = GSON.toJson(object);
        File file = new File(configDir, type + ".json");
        try {
            FileUtils.writeStringToFile(file, jsonStr, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file: " + file.getAbsolutePath(), e);
        }
    }
}
