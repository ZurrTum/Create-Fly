package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorControlsHandler;
import com.zurrtum.create.client.content.trains.TrainHUD;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseButton(JIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getOverlay()Lnet/minecraft/client/gui/screen/Overlay;", ordinal = 0), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 0) {
            return;
        }
        if (Create.SCHEMATIC_HANDLER.onMouseInput(client, button) || Create.SCHEMATIC_AND_QUILL_HANDLER.onMouseInput(client, button)) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci, @Local(ordinal = 4) double delta) {
        if (Create.SCHEMATIC_HANDLER.mouseScrolled(delta) || Create.SCHEMATIC_AND_QUILL_HANDLER.mouseScrolled(client, delta) || TrainHUD.onScroll(
            delta) || ElevatorControlsHandler.onScroll(client, delta)) {
            ci.cancel();
        }
    }
}
