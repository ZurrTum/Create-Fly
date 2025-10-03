package com.zurrtum.create.catnip.config;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ConfigBase {
    protected int depth;
    protected List<CValue<?>> allValues = new ArrayList<>();
    protected List<ConfigBase> children = new ArrayList<>();

    public void registerAll(final Builder builder) {
        for (CValue<?> cValue : allValues)
            cValue.register(builder);
    }

    public abstract String getName();

    protected ConfigString s(String current, String name, String... comment) {
        return new ConfigString(name, current, comment);
    }

    protected ConfigBool b(boolean current, String name, String... comment) {
        return new ConfigBool(name, current, comment);
    }

    protected ConfigFloat f(float current, float min, float max, String name, String... comment) {
        return new ConfigFloat(name, current, min, max, comment);
    }

    protected ConfigFloat f(float current, float min, String name, String... comment) {
        return f(current, min, Float.MAX_VALUE, name, comment);
    }

    protected ConfigInt i(int current, int min, int max, String name, String... comment) {
        return new ConfigInt(name, current, min, max, comment);
    }

    protected ConfigInt i(int current, int min, String name, String... comment) {
        return i(current, min, Integer.MAX_VALUE, name, comment);
    }

    protected ConfigInt i(int current, String name, String... comment) {
        return i(current, Integer.MIN_VALUE, Integer.MAX_VALUE, name, comment);
    }

    protected <T extends Enum<T>> ConfigEnum<T> e(T defaultValue, String name, String... comment) {
        return new ConfigEnum<>(name, defaultValue, comment);
    }

    protected ConfigGroup group(int depth, String name, String... comment) {
        return new ConfigGroup(name, depth, comment);
    }

    protected <T extends ConfigBase> T nested(int depth, Supplier<T> constructor, String... comment) {
        T config = constructor.get();
        new ConfigGroup(config.getName(), depth, comment);
        new CValue<Boolean>(
            config.getName(), builder -> {
            config.depth = depth;
            config.registerAll(builder);
            if (config.depth > depth)
                builder.pop(config.depth - depth);
            return null;
        }
        );
        children.add(config);
        return config;
    }

    public class CValue<V> {
        private final Function<Builder, ConfigValue<V>> provider;
        @Nullable
        protected ConfigValue<V> value;
        protected String name;

        public CValue(String name, Function<Builder, ConfigValue<V>> provider, String... comment) {
            this.name = name;
            this.provider = builder -> {
                addComments(builder, comment);
                return provider.apply(builder);
            };
            allValues.add(this);
        }

        public void addComments(Builder builder, String... comment) {
            if (comment.length > 0) {
                builder.comment(comment);
            }
        }

        public void register(Builder builder) {
            value = provider.apply(builder);
        }

        public V get() {
            if (value == null)
                throw new AssertionError("Config " + getName() + " was accessed, but not registered before!");

            return value.get();
        }

        public void set(V value) {
            if (this.value == null)
                throw new AssertionError("Config " + getName() + " was accessed, but not registered before!");

            this.value.set(value);
            this.value.save();
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Marker for config subgroups
     */
    public class ConfigGroup extends CValue<Boolean> {

        private final int groupDepth;
        private final String[] comment;

        public ConfigGroup(String name, int depth, String... comment) {
            super(name, builder -> null, comment);
            groupDepth = depth;
            this.comment = comment;
        }

        @Override
        public void register(Builder builder) {
            if (depth > groupDepth)
                builder.pop(depth - groupDepth);
            depth = groupDepth;
            addComments(builder, comment);
            builder.push(getName());
            depth++;
        }

    }

    public class ConfigBool extends CValue<Boolean> {

        public ConfigBool(String name, boolean def, String... comment) {
            super(name, builder -> builder.define(name, def), comment);
        }
    }

    public class ConfigString extends CValue<String> {

        public ConfigString(String name, String def, String... comment) {
            super(name, builder -> builder.define(name, def), comment);
        }
    }

    public class ConfigEnum<T extends Enum<T>> extends CValue<T> {

        public ConfigEnum(String name, T defaultValue, String[] comment) {
            super(name, builder -> builder.defineEnum(name, defaultValue), comment);
        }

    }

    public class ConfigFloat extends CValue<Float> {

        public ConfigFloat(String name, float current, float min, float max, String... comment) {
            super(name, builder -> builder.defineInRange(name, current, min, max), comment);
        }

        public float getF() {
            return get();
        }
    }

    public class ConfigInt extends CValue<Integer> {

        public ConfigInt(String name, int current, int min, int max, String... comment) {
            super(name, builder -> builder.defineInRange(name, current, min, max), comment);
        }
    }

}