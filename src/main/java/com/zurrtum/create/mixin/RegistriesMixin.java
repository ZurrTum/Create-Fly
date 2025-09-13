package com.zurrtum.create.mixin;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registries.class)
public class RegistriesMixin {
    @Inject(method = "freezeRegistries()V", at = @At("HEAD"))
    private static void onInitialize(CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("fabric-api")) {
            Create.register();
            AllBlocks.ALL.forEach(block -> block.getStateManager().getStates().forEach(state -> {
                Block.STATE_IDS.add(state);
                state.initShapeCache();
            }));
            AllFluids.ALL.forEach(fluid -> fluid.getStateManager().getStates().forEach(Fluid.STATE_IDS::add));
        }
    }

    @Inject(method = "freezeRegistries()V", at = @At("TAIL"))
    private static void afterFreeze(CallbackInfo ci) {
        ArmInteractionPointType.register();
        FanProcessingTypeRegistry.register();
    }
}
