package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CTSpriteShifter {

    private static final Map<String, CTSpriteShiftEntry> ENTRY_CACHE = new HashMap<>();

    public static CTSpriteShiftEntry getCT(CTType type, Identifier blockTexture, Identifier connectedTexture) {
        String key = blockTexture + "->" + connectedTexture + "+" + type.getId();
        if (ENTRY_CACHE.containsKey(key))
            return ENTRY_CACHE.get(key);

        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(type);
        entry.set(blockTexture, connectedTexture);
        ENTRY_CACHE.put(key, entry);
        return entry;
    }

}
