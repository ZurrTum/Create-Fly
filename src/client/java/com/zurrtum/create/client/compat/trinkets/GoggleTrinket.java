package com.zurrtum.create.client.compat.trinkets;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.compat.Mods;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;

public class GoggleTrinket {
    public static void register() {
        if (Mods.TRINKETS.isLoaded()) {
            TrinketRendererRegistry.registerRenderer(AllItems.GOGGLES, new GoggleTrinketRenderer());
        }
    }
}
