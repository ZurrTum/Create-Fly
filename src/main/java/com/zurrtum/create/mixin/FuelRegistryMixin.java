package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllFuelTimes;
import net.minecraft.item.FuelRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FuelRegistry.class)
public class FuelRegistryMixin {
    @WrapOperation(method = "createDefault(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/resource/featuretoggle/FeatureSet;I)Lnet/minecraft/item/FuelRegistry;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FuelRegistry$Builder;build()Lnet/minecraft/item/FuelRegistry;"))
    private static FuelRegistry register(FuelRegistry.Builder builder, Operation<FuelRegistry> original) {
        AllFuelTimes.ALL.forEach(builder::add);
        return original.call(builder);
    }
}
