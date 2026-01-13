package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.compat.computercraft.implementation.ComputerBehaviour;
import com.zurrtum.create.compat.computercraft.implementation.luaObjects.PackageLuaObject;
import com.zurrtum.create.compat.computercraft.implementation.peripherals.*;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.minecraft.block.entity.BlockEntityType;

import java.util.function.Function;

public class AllComputerPeripherals {
    private static <T extends SmartBlockEntity> void registerPeripheral(BlockEntityType<T> type, Function<T, SyncedPeripheral<T>> factory) {
        BlockEntityBehaviour.add(type, ComputerBehaviour::new);
        PeripheralLookup.get().registerForBlockEntity(
            (blockEntity, direction) -> {
                if (blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE) instanceof ComputerBehaviour behaviour) {
                    if (behaviour.peripheral != null) {
                        return behaviour.peripheral;
                    }
                    return behaviour.peripheral = factory.apply(blockEntity);
                }
                return null;
            }, type
        );
    }

    public static void register() {
        registerPeripheral(AllBlockEntityTypes.MOTOR, CreativeMotorPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.DISPLAY_LINK, DisplayLinkPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.STRESSOMETER, StressGaugePeripheral::new);
        registerPeripheral(AllBlockEntityTypes.PACKAGE_FROGPORT, FrogportPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.NIXIE_TUBE, NixieTubePeripheral::new);
        registerPeripheral(AllBlockEntityTypes.PACKAGER, PackagerPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.PACKAGE_POSTBOX, PostboxPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.REDSTONE_REQUESTER, RedstoneRequesterPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.REPACKAGER, RepackagerPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.SEQUENCED_GEARSHIFT, SequencedGearshiftPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.TRACK_SIGNAL, SignalPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER, SpeedControllerPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.TRACK_STATION, StationPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.STICKER, StickerPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.TABLE_CLOTH, TableClothShopPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.SPEEDOMETER, SpeedGaugePeripheral::new);
        registerPeripheral(AllBlockEntityTypes.TRACK_OBSERVER, TrackObserverPeripheral::new);
        registerPeripheral(AllBlockEntityTypes.STOCK_TICKER, StockTickerPeripheral::new);
        VanillaDetailRegistries.ITEM_STACK.addProvider((out, stack) -> {
            if (PackageItem.isPackage(stack)) {
                PackageLuaObject packageLuaObject = new PackageLuaObject(null, stack);
                out.put("package", packageLuaObject);
            }
        });
    }
}
