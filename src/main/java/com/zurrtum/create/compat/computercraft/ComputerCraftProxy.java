package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.compat.computercraft.implementation.ComputerBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public class ComputerCraftProxy {
    private static <T extends SmartBlockEntity> void registerPeripheral(BlockEntityType<T> type) {
        PeripheralLookup.get()
            .registerForBlockEntity(
                (blockEntity, direction) -> blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE).getPeripheralCapability(),
                type
            );
    }

    public static void register() {
        /* Comment if computercraft.implementation is not in the source set */
        computerFactory = ComputerBehaviour::new;
        ComputerBehaviour.registerItemDetailProviders();
        registerPeripheral(AllBlockEntityTypes.MOTOR);
        registerPeripheral(AllBlockEntityTypes.DISPLAY_LINK);
        registerPeripheral(AllBlockEntityTypes.STRESSOMETER);
        registerPeripheral(AllBlockEntityTypes.PACKAGE_FROGPORT);
        registerPeripheral(AllBlockEntityTypes.NIXIE_TUBE);
        registerPeripheral(AllBlockEntityTypes.PACKAGER);
        registerPeripheral(AllBlockEntityTypes.PACKAGE_POSTBOX);
        registerPeripheral(AllBlockEntityTypes.REDSTONE_REQUESTER);
        registerPeripheral(AllBlockEntityTypes.REPACKAGER);
        registerPeripheral(AllBlockEntityTypes.SEQUENCED_GEARSHIFT);
        registerPeripheral(AllBlockEntityTypes.TRACK_SIGNAL);
        registerPeripheral(AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER);
        registerPeripheral(AllBlockEntityTypes.TRACK_STATION);
        registerPeripheral(AllBlockEntityTypes.STICKER);
        registerPeripheral(AllBlockEntityTypes.TABLE_CLOTH);
        registerPeripheral(AllBlockEntityTypes.SPEEDOMETER);
        registerPeripheral(AllBlockEntityTypes.TRACK_OBSERVER);
        registerPeripheral(AllBlockEntityTypes.STOCK_TICKER);
    }

    private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> computerFactory;

    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        if (computerFactory == null)
            return new FallbackComputerBehaviour(sbe);
        return computerFactory.apply(sbe);
    }
}
