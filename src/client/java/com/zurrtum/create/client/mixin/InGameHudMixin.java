package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.content.equipment.armor.CardboardArmorStealthOverlay;
import com.zurrtum.create.client.content.equipment.armor.RemainingAirOverlay;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.content.trains.TrainHUD;
import com.zurrtum.create.client.content.trains.track.TrackPlacementOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void renderCrosshair(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        PlacementClient.onRenderCrosshairOverlay(minecraft, context, AnimationTickHolder.getPartialTicksUI(tickCounter));
    }

    @Inject(method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void renderHotbar(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Create.VALUE_SETTINGS_HANDLER.render(minecraft, context);
        TrackPlacementOverlay.render(minecraft, context);
        GoggleOverlayRenderer.renderOverlay(minecraft, context, tickCounter);
        BlueprintOverlayRenderer.renderOverlay(minecraft, context);
        LinkedControllerClientHandler.renderOverlay(minecraft, context);
        Create.SCHEMATIC_HANDLER.render(minecraft, context, tickCounter);
        ToolboxHandlerClient.renderOverlay(minecraft, context);
    }

    @Inject(method = "renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V", at = @At("TAIL"))
    private void renderAirBubbles(GuiGraphics context, Player player, int heartCount, int top, int left, CallbackInfo ci) {
        RemainingAirOverlay.render(minecraft, context);
    }

    @Inject(method = "renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", shift = At.Shift.AFTER))
    private void renderMainHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        TrainHUD.renderOverlay(minecraft, context, tickCounter);
    }

    @WrapOperation(method = "renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;F)V", ordinal = 0))
    private void renderMiscOverlays(
        Gui instance,
        GuiGraphics context,
        ResourceLocation texture,
        float opacity,
        Operation<Void> original,
        @Local(argsOnly = true) DeltaTracker tickCounter,
        @Local ItemStack stack
    ) {
        if (stack.is(AllItems.CARDBOARD_HELMET)) {
            original.call(instance, context, texture, CardboardArmorStealthOverlay.getOverlayOpacity(tickCounter));
        } else {
            original.call(instance, context, texture, opacity);
        }
    }
}
