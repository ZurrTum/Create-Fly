package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey(JIIII)V", at = @At(value = "RETURN", ordinal = 5))
    private void onKeyReleased(long window, int keycode, int scancode, int action, int modifiers, CallbackInfo ci) {
        onKey(window, keycode, scancode, action, modifiers, ci);
    }

    @Inject(method = "onKey(JIIII)V", at = @At(value = "TAIL"))
    private void onKey(long window, int keycode, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (client.currentScreen != null) {
            return;
        }
        boolean pressed = action != 0;
        InputUtil.Key key = InputUtil.fromKeyCode(keycode, scancode);
        if (Create.SCHEMATIC_HANDLER.onKeyInput(key, pressed)) {
            return;
        }
        ToolboxHandlerClient.onKeyInput(client, key);
    }
}
