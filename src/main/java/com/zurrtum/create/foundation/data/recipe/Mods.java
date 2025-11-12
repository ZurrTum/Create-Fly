package com.zurrtum.create.foundation.data.recipe;

import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

public enum Mods {
    ARS_N("ars_nouveau"),
    BTN("botania", Builder::omitWoodSuffix),
    DD("deeperdarker");

    private final String id;

    public boolean reversedMetalPrefix;
    public boolean strippedIsSuffix;
    public boolean omitWoodSuffix;

    Mods(String id) {
        this(
            id, b -> {
            }
        );
    }

    Mods(String id, Consumer<Builder> props) {
        props.accept(new Builder());
        this.id = id;
    }

    public Identifier asResource(String id) {
        return Identifier.fromNamespaceAndPath(this.id, id);
    }

    public String getId() {
        return id;
    }

    class Builder {
        void omitWoodSuffix() {
            omitWoodSuffix = true;
        }

        void reverseMetalPrefix() {
            reversedMetalPrefix = true;
        }

        void strippedWoodIsSuffix() {
            strippedIsSuffix = true;
        }
    }
}
