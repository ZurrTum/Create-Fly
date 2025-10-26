package com.zurrtum.create.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Locale;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
    COMPUTERCRAFT;

    private final String id;
    private final boolean loaded;

    Mods() {
        id = name().toLowerCase(Locale.ROOT);
        loaded = FabricLoader.getInstance().isModLoaded(id);
    }

    /**
     * @return the mod id
     */
    public String id() {
        return id;
    }

    /**
     * @return a boolean of whether the mod is loaded or not based on mod id
     */
    public boolean isLoaded() {
        return loaded;
    }
}
