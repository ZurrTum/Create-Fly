package com.zurrtum.create;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.impl.effect.*;
import net.minecraft.registry.tag.FluidTags;

public class AllOpenPipeEffectHandlers {
    public static void register() {
        OpenPipeEffectHandler.REGISTRY.registerProvider(SimpleRegistry.Provider.forFluidTag(FluidTags.WATER, new WaterEffectHandler()));
        OpenPipeEffectHandler.REGISTRY.registerProvider(SimpleRegistry.Provider.forFluidTag(FluidTags.LAVA, new LavaEffectHandler()));
        OpenPipeEffectHandler.REGISTRY.registerProvider(SimpleRegistry.Provider.forFluidTag(AllFluidTags.MILK, new MilkEffectHandler()));
        OpenPipeEffectHandler.REGISTRY.register(AllFluids.POTION, new PotionEffectHandler());
        OpenPipeEffectHandler.REGISTRY.register(AllFluids.TEA, new TeaEffectHandler());
    }
}
