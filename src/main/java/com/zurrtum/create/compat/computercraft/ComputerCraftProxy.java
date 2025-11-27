package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.compat.Mods;
import com.zurrtum.create.compat.computercraft.implementation.ComputerBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import java.util.function.Function;

public class ComputerCraftProxy {
    public static void register() {
        fallbackFactory = FallbackComputerBehaviour::new;
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> ComputerCraftProxy::registerWithDependency);
    }

    private static void registerWithDependency() {
        /* Comment if computercraft.implementation is not in the source set */
        computerFactory = ComputerBehaviour::new;
        ComputerBehaviour.registerItemDetailProviders();
    }

    private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> fallbackFactory;
    private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> computerFactory;

    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        if (computerFactory == null)
            return fallbackFactory.apply(sbe);
        return computerFactory.apply(sbe);
    }
}
