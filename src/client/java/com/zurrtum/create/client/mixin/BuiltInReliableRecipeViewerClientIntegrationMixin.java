package com.zurrtum.create.client.mixin;

import cc.cassian.rrv.client.builtin.BuiltInReliableRecipeViewerClientIntegration;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInReliableRecipeViewerClientIntegration.class)
public class BuiltInReliableRecipeViewerClientIntegrationMixin {
    @Inject(method = "lambda$addWorldInteractionRecipes$7", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"), cancellable = true)
    private static void checkBucket(CallbackInfo ci, @Local Item bucket) {
        if (bucket == Items.AIR) {
            ci.cancel();
        }
    }
}
