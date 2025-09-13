package com.zurrtum.create.client.catnip.render;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SpriteShifter {

    private static final Map<String, SpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

    public static SpriteShiftEntry get(Identifier originalLocation, Identifier targetLocation) {
        String key = originalLocation + "->" + targetLocation;
        if (ENTRY_CACHE.containsKey(key))
            return ENTRY_CACHE.get(key);

        SpriteShiftEntry entry = new SpriteShiftEntry();
        entry.set(originalLocation, targetLocation);
        ENTRY_CACHE.put(key, entry);
        return entry;
    }
}
