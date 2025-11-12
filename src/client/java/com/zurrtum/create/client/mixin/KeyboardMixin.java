package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.contraptions.wrench.RadialWrenchHandler;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At(value = "RETURN", ordinal = 5))
    private void onKeyReleased(long window, int action, KeyEvent input, CallbackInfo ci) {
        onKey(input, false);
    }

    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At(value = "TAIL"))
    private void onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        onKey(input, true);
    }

    @Unique
    private void onKey(KeyEvent input, boolean pressed) {
        if (minecraft.screen != null) {
            return;
        }
        if (Create.SCHEMATIC_HANDLER.onKeyInput(input, pressed)) {
            return;
        }
        if (ToolboxHandlerClient.onKeyInput(minecraft, input)) {
            return;
        }
        RadialWrenchHandler.onKeyInput(minecraft, input, pressed);
    }
}
