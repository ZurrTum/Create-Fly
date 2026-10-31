package com.zurrtum.create.mixin;

import com.zurrtum.create.api.registry.CreateRegisterPlugin;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Blocks.class)
public class BlocksMixin {
    @Inject(method = "lambda$static$429()V", at = @At("HEAD"))
    private static void register(CallbackInfo ci) {
        CreateRegisterPlugin.registerBlock();
    }
}
