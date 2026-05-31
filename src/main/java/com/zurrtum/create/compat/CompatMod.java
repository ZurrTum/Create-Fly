package com.zurrtum.create.compat;

import com.zurrtum.create.compat.computercraft.AllComputerDisplaySource;
import com.zurrtum.create.compat.computercraft.AllComputerPeripherals;
import com.zurrtum.create.compat.computercraft.ComputerIntegration;
import com.zurrtum.create.compat.jei.JeiCommonPlugin;
import com.zurrtum.create.compat.trinkets.GoggleTrinket;

public class CompatMod {
    public static void register() {
        if (Mods.JEI.isLoaded()) {
            JeiCommonPlugin.register();
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
