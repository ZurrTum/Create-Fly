package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorControlsHandler;
import com.zurrtum.create.client.content.trains.TrainHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0), cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        if (action == 0) {
            return;
        }
        int button = input.button();
        if (Create.SCHEMATIC_HANDLER.onMouseInput(minecraft, button) || Create.SCHEMATIC_AND_QUILL_HANDLER.onMouseInput(minecraft, button)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci, @Local(ordinal = 4) double delta) {
        if (Create.SCHEMATIC_HANDLER.mouseScrolled(delta) || Create.SCHEMATIC_AND_QUILL_HANDLER.mouseScrolled(minecraft, delta) || TrainHUD.onScroll(
            delta) || ElevatorControlsHandler.onScroll(minecraft, delta)) {
            ci.cancel();
        }
    }
}
