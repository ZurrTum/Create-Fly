package com.zurrtum.create.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Locale;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
    COMPUTERCRAFT,
    TRINKETS,
    PACKETFIXER,
    MODERNUI,
    ACCESSORIES;

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

    public Identifier identifier(String name) {
        return Identifier.of(id, name);
    }

    public Block getBlock(String id) {
        return Registries.BLOCK.get(identifier(id));
    }

    public Item getItem(String id) {
        return Registries.ITEM.get(identifier(id));
    }

    /**
     * @return a boolean of whether the mod is loaded or not based on mod id
     */
    public boolean isLoaded() {
        return loaded;
    }
}
