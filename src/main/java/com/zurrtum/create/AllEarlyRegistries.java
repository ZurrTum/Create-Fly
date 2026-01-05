package com.zurrtum.create;

import com.zurrtum.create.api.registry.CreateRegisterPlugin;

public class AllEarlyRegistries implements CreateRegisterPlugin {
    @Override
    public void onBlockRegister() {
        AllBlocks.register();
    }

    @Override
    public void onFluidRegister() {
        AllFluids.register();
    }

    @Override
    public void onDataLoaderRegister() {
        AllDynamicRegistries.register();
    }

    @Override
    public void onEntityAttributeRegister() {
        AllEntityAttributes.register();
    }
}
