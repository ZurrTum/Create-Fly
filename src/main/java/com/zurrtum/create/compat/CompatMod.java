package com.zurrtum.create.compat;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.compat.trinkets.GoggleTrinket;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.minecraft.block.entity.BlockEntityType;

public class CompatMod {
    private static <T extends SmartBlockEntity> void registerPeripheral(BlockEntityType<T> type) {
        PeripheralLookup.get().registerForBlockEntity((blockEntity, direction) -> blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE).getPeripheralCapability(), type);
    }

    public static void register() {
        if (Mods.TRINKETS.isLoaded()) {
            GoggleTrinket.register();
        }

        if (Mods.COMPUTERCRAFT.isLoaded()) {
            ComputerCraftProxy.register();
            registerPeripheral(AllBlockEntityTypes.MOTOR);
            registerPeripheral(AllBlockEntityTypes.DISPLAY_LINK);
            registerPeripheral(AllBlockEntityTypes.STRESSOMETER);
            registerPeripheral(AllBlockEntityTypes.PACKAGE_FROGPORT);
            registerPeripheral(AllBlockEntityTypes.NIXIE_TUBE);
        }
    }
}
