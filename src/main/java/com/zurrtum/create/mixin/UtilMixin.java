package com.zurrtum.create.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Util.class)
public class UtilMixin {
    @Inject(method = "doFetchChoiceType(Lcom/mojang/datafixers/DSL$TypeReference;Ljava/lang/String;)Lcom/mojang/datafixers/types/Type;", at = @At("HEAD"), cancellable = true)
    private static void ignoreError(DSL.TypeReference typeReference, String id, CallbackInfoReturnable<Type<?>> cir) {
        if (id.startsWith("create:")) {
            cir.setReturnValue(null);
        }
    }
}
