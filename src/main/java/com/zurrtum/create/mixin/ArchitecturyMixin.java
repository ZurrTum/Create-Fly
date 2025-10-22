package com.zurrtum.create.mixin;

import dev.architectury.fluid.fabric.FluidStackImpl;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
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
    public MergedComponentMap components;

    @Inject(method = "getPatch()Lnet/minecraft/component/ComponentChanges;", at = @At("HEAD"), cancellable = true)
    private void getPatch(CallbackInfoReturnable<ComponentChanges> cir) {
        cir.setReturnValue(amount <= 0L || fluid == Fluids.EMPTY ? ComponentChanges.EMPTY : components.getChanges());
    }
}
