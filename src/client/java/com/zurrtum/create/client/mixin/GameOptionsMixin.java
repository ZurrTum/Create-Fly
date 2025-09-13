package com.zurrtum.create.client.mixin;

import com.google.common.collect.Lists;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.OptionsUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.List;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Mutable
    @Final
    @Shadow
    public KeyBinding[] allKeys;

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Ljava/io/File;)V", at = @At("TAIL"))
    private void wrapAddAll(MinecraftClient client, File optionsFile, CallbackInfo ci) {
        int index = KeyBinding.CATEGORY_ORDER_MAP.values().stream().max(Integer::compareTo).orElse(0) + 1;
        KeyBinding.CATEGORY_ORDER_MAP.put(AllKeys.CATEGORY, index);
        List<KeyBinding> keys = Lists.newArrayList(allKeys);
        keys.removeAll(AllKeys.ALL);
        keys.addAll(AllKeys.ALL);
        allKeys = keys.toArray(KeyBinding[]::new);
    }

    @Inject(method = "load()V", at = @At("RETURN"))
    private void flywheel$onLoad(CallbackInfo ci) {
        OptionsUniforms.update((GameOptions) (Object) this);
    }

    @Inject(method = "write()V", at = @At("HEAD"))
    private void flywheel$onSave(CallbackInfo ci) {
        OptionsUniforms.update((GameOptions) (Object) this);
    }
}
