package com.zurrtum.create.client.mixin;

import com.google.common.collect.Lists;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.OptionsUniforms;
import com.zurrtum.create.client.ponder.enums.PonderKeybinds;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

@Mixin(Options.class)
public class GameOptionsMixin {
    @Mutable
    @Final
    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;load()V"))
    private void wrapAddAll(Minecraft client, File optionsFile, CallbackInfo ci) {
        List<KeyMapping> keys = Lists.newArrayList(keyMappings);
        keys.removeAll(AllKeys.ALL);
        keys.addAll(AllKeys.ALL);
        keys.remove(PonderKeybinds.PONDER);
        keys.add(PonderKeybinds.PONDER);
        keyMappings = keys.toArray(KeyMapping[]::new);
    }

    @Inject(method = "load()V", at = @At("RETURN"))
    private void flywheel$onLoad(CallbackInfo ci) {
        OptionsUniforms.update((Options) (Object) this);
    }

    @Inject(method = "save()V", at = @At("HEAD"))
    private void flywheel$onSave(CallbackInfo ci) {
        OptionsUniforms.update((Options) (Object) this);
    }
}
