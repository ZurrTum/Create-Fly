package com.zurrtum.create.client.compat;

import com.zurrtum.create.client.compat.accessories.GoggleAccessoryRenderer;
import com.zurrtum.create.client.compat.trinkets.GoggleTrinketRenderer;
import com.zurrtum.create.compat.Mods;

public class CompatMod {
    public static void register() {
        if (Mods.TRINKETS.isLoaded()) {
            GoggleTrinketRenderer.register();
        }
        if (Mods.ACCESSORIES.isLoaded()) {
            GoggleAccessoryRenderer.register();
        }
    }
}
