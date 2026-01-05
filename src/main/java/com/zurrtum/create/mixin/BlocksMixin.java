package com.zurrtum.create.mixin;

import com.zurrtum.create.api.registry.CreateRegisterPlugin;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Blocks.class)
public class BlocksMixin {
    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "net/minecraft/core/DefaultedRegistry.iterator()Ljava/util/Iterator;"))
    private static void register(CallbackInfo ci) {
        CreateRegisterPlugin.run("create_block");
    }
}
