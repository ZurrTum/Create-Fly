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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("TAIL"))
    private void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        PlacementClient.onRenderCrosshairOverlay(client, context, AnimationTickHolder.getPartialTicksUI(tickCounter));
    }

    @Inject(method = "renderHotbar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("TAIL"))
    private void renderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Create.VALUE_SETTINGS_HANDLER.render(client, context);
        TrackPlacementOverlay.render(client, context);
        GoggleOverlayRenderer.renderOverlay(client, context, tickCounter);
        BlueprintOverlayRenderer.renderOverlay(client, context);
        LinkedControllerClientHandler.renderOverlay(client, context);
        Create.SCHEMATIC_HANDLER.render(client, context, tickCounter);
        ToolboxHandlerClient.renderOverlay(client, context);
    }

    @Inject(method = "renderAirBubbles(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;III)V", at = @At("TAIL"))
    private void renderAirBubbles(DrawContext context, PlayerEntity player, int heartCount, int top, int left, CallbackInfo ci) {
        RemainingAirOverlay.render(client, context);
    }

    @Inject(method = "renderMainHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountHealth(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))
    private void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        TrainHUD.renderOverlay(client, context, tickCounter);
    }

    @WrapOperation(method = "renderMiscOverlays(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal = 0))
    private void renderMiscOverlays(
        InGameHud instance,
        DrawContext context,
        Identifier texture,
        float opacity,
        Operation<Void> original,
        @Local(argsOnly = true) RenderTickCounter tickCounter,
        @Local ItemStack stack
    ) {
        if (stack.isOf(AllItems.CARDBOARD_HELMET)) {
            original.call(instance, context, texture, CardboardArmorStealthOverlay.getOverlayOpacity(tickCounter));
        } else {
            original.call(instance, context, texture, opacity);
        }
    }
}
