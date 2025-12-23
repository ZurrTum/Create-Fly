package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.compat.eiv.EivClientPlugin;
import de.crafty.eiv.fabric.FabricEIV;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FabricEIV.class)
public class FabricEIVMixin {
    @Inject(method = "onInitialize()V", at = @At("TAIL"), remap = false)
    private void onInitialize(CallbackInfo ci) {
        new EivClientPlugin().onIntegrationInitialize();
    }
}
