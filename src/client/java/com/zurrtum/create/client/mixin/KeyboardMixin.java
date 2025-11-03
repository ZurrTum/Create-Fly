package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.contraptions.wrench.RadialWrenchHandler;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey(JILnet/minecraft/client/input/KeyInput;)V", at = @At(value = "RETURN", ordinal = 5))
    private void onKeyReleased(long window, int action, KeyInput input, CallbackInfo ci) {
        onKey(input, false);
    }

    @Inject(method = "onKey(JILnet/minecraft/client/input/KeyInput;)V", at = @At(value = "TAIL"))
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        onKey(input, true);
    }

    @Unique
    private void onKey(KeyInput input, boolean pressed) {
        if (client.currentScreen != null) {
            return;
        }
        if (Create.SCHEMATIC_HANDLER.onKeyInput(input, pressed)) {
            return;
        }
        if (ToolboxHandlerClient.onKeyInput(client, input)) {
            return;
        }
        RadialWrenchHandler.onKeyInput(client, input, pressed);
    }
}
