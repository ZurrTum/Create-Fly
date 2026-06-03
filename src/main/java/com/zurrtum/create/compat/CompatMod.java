package com.zurrtum.create.compat;

import com.zurrtum.create.compat.computercraft.AllComputerDisplaySource;
import com.zurrtum.create.compat.computercraft.AllComputerPeripherals;
import com.zurrtum.create.compat.computercraft.ComputerIntegration;
import com.zurrtum.create.compat.fabric.RecipeCommonPlugin;
import com.zurrtum.create.compat.trinkets.GoggleTrinket;

public class CompatMod {
    public static void register() {
        if (Mods.JEI.isLoaded() || Mods.RRV.isLoaded()) {
            RecipeCommonPlugin.register();
        }
        if (Mods.TRINKETS_UPDATED.isLoaded()) {
            GoggleTrinket.register();
        }
        if (Mods.COMPUTERCRAFT.isLoaded()) {
            ComputerIntegration.register();
            AllComputerPeripherals.register();
            AllComputerDisplaySource.register();
        }
    }
}
