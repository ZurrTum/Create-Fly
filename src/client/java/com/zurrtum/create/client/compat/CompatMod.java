package com.zurrtum.create.client.compat;

import com.zurrtum.create.client.compat.fabric.PotionRenderHandler;
import com.zurrtum.create.client.compat.trinkets.GoggleTrinketRenderer;
import com.zurrtum.create.compat.Mods;
import net.fabricmc.loader.api.FabricLoader;

public class CompatMod {
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1")) {
            PotionRenderHandler.register();
        }
        if (Mods.TRINKETS_UPDATED.isLoaded()) {
            GoggleTrinketRenderer.register();
        }
    }
}
