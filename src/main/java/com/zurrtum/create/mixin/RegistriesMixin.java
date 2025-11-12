package com.zurrtum.create.mixin;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class RegistriesMixin {
    @Inject(method = "freeze()V", at = @At("HEAD"))
    private static void onInitialize(CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("fabric-api")) {
            Create.register();
            AllBlocks.ALL.forEach(block -> block.getStateDefinition().getPossibleStates().forEach(state -> {
                Block.BLOCK_STATE_REGISTRY.add(state);
                state.initCache();
            }));
            AllFluids.ALL.forEach(fluid -> fluid.getStateDefinition().getPossibleStates().forEach(Fluid.FLUID_STATE_REGISTRY::add));
        }
    }

    @Inject(method = "freeze()V", at = @At("TAIL"))
    private static void afterFreeze(CallbackInfo ci) {
        ArmInteractionPointType.register();
        FanProcessingTypeRegistry.register();
    }
}
