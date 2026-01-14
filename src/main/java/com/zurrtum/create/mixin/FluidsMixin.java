package com.zurrtum.create.mixin;

import com.zurrtum.create.api.registry.CreateRegisterPlugin;
import net.minecraft.fluid.Fluids;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fluids.class)
public class FluidsMixin {
    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/registry/Registries;FLUID:Lnet/minecraft/registry/DefaultedRegistry;", opcode = Opcodes.GETSTATIC))
    private static void register(CallbackInfo ci) {
        CreateRegisterPlugin.registerFluid();
    }
}
