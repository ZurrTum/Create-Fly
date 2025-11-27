package com.zurrtum.create.compat;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.compat.trinkets.GoggleTrinket;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import dan200.computercraft.api.peripheral.PeripheralLookup;

public class CompatMod {
    public static void register() {
        if (Mods.TRINKETS.isLoaded()) {
            GoggleTrinket.register();
        }

        if (Mods.COMPUTERCRAFT.isLoaded()) {
            ComputerCraftProxy.register();
            PeripheralLookup.get().registerForBlockEntity((blockEntity, direction) -> blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE).getPeripheralCapability(), AllBlockEntityTypes.STRESSOMETER);
            PeripheralLookup.get().registerForBlockEntity((blockEntity, direction) -> blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE).getPeripheralCapability(), AllBlockEntityTypes.MOTOR);
        }
    }
}
