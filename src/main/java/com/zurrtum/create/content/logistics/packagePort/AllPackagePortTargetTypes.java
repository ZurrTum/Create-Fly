package com.zurrtum.create.content.logistics.packagePort;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget.ChainConveyorFrogportTarget;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget.TrainStationFrogportTarget;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPackagePortTargetTypes {
    public static final PackagePortTargetType CHAIN_CONVEYOR = register("chain_conveyor", ChainConveyorFrogportTarget.Type::new);
    public static final PackagePortTargetType TRAIN_STATION = register("train_station", TrainStationFrogportTarget.Type::new);

    public static PackagePortTargetType register(String name, Supplier<PackagePortTargetType> factory) {
        Identifier id = Identifier.of(MOD_ID, name);
        PackagePortTargetType type = factory.get();
        Registry.register(CreateRegistries.PACKAGE_PORT_TARGET_TYPE, id, type);
        return type;
    }

    public static void register() {
    }
}
