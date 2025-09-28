package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.ponder.enums.PonderKeybinds;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @WrapOperation(method = "updateKeysByCode()V", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <K, V> V skip(Map<K, V> map, K k, V v, Operation<V> original) {
        if (v == PonderKeybinds.PONDER) {
            return null;
        }
        return original.call(map, k, v);
    }
}
