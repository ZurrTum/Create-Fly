package com.zurrtum.create.mixin;

import dev.architectury.fluid.fabric.FluidStackImpl;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FluidStackImpl.Pair.class)
public class ArchitecturyMixin {
    @Shadow(remap = false)
    public long amount;

    @Shadow
    public Fluid fluid;

    @Shadow
    public PatchedDataComponentMap components;

    @Inject(method = "getPatch()Lnet/minecraft/core/component/DataComponentPatch;", at = @At("HEAD"), cancellable = true)
    private void getPatch(CallbackInfoReturnable<DataComponentPatch> cir) {
        cir.setReturnValue(amount <= 0L || fluid == Fluids.EMPTY ? DataComponentPatch.EMPTY : components.asPatch());
    }
}
