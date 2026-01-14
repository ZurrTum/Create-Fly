package com.zurrtum.create.mixin;

import com.zurrtum.create.Create;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.fabricmc.loader.api.FabricLoader;
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
        }
    }

    @Inject(method = "freezeRegistries()V", at = @At("TAIL"))
    private static void afterFreeze(CallbackInfo ci) {
        ArmInteractionPointType.register();
        FanProcessingTypeRegistry.register();
    }
}
