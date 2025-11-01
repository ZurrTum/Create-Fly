package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.ponder.enums.PonderKeybinds;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @WrapOperation(method = "updateKeysByCode()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;registerBinding(Lnet/minecraft/client/util/InputUtil$Key;)V"))
    private static void skip(KeyBinding instance, InputUtil.Key key, Operation<Void> original) {
        if (instance == PonderKeybinds.PONDER) {
            return;
        }
        original.call(instance, key);
    }
}
