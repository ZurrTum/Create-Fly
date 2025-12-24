package com.zurrtum.create.client.compat;

import com.zurrtum.create.client.compat.trinkets.GoggleTrinketRenderer;
import com.zurrtum.create.compat.Mods;
import com.zurrtum.create.compat.computercraft.AllComputerDisplaySource;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;

public class CompatMod {
    public static void register() {
        if (Mods.TRINKETS.isLoaded()) {
            GoggleTrinketRenderer.register();
        }
        if (Mods.COMPUTERCRAFT.isLoaded()) {
            ComputerCraftProxy.register();
            AllComputerDisplaySource.register();
        }
    }
}
