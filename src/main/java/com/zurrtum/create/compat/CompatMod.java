package com.zurrtum.create.compat;

import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.compat.trinkets.GoggleTrinket;

public class CompatMod {
    public static void register() {
        if (Mods.TRINKETS.isLoaded()) {
            GoggleTrinket.register();
        }

        if (Mods.COMPUTERCRAFT.isLoaded()) {
            ComputerCraftProxy.register();
        }
    }
}
