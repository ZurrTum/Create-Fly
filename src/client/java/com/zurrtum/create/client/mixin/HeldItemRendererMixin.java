package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private EntityRenderManager entityRenderDispatcher;

    @WrapOperation(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V"))
    private void renderItem(
        HeldItemRenderer instance,
        AbstractClientPlayerEntity player,
        float tickProgress,
        float pitch,
        Hand hand,
        float swingProgress,
        ItemStack item,
        float equipProgress,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        int light,
        Operation<Void> original
    ) {
        if (Create.ZAPPER_RENDER_HANDLER.onRenderPlayerHand(
            item,
            client,
            entityRenderDispatcher,
            instance,
            matrices,
            queue,
            light,
            tickProgress,
            hand,
            equipProgress,
            swingProgress
        ) || Create.POTATO_CANNON_RENDER_HANDLER.onRenderPlayerHand(
            item,
            client,
            entityRenderDispatcher,
            instance,
            matrices,
            queue,
            light,
            tickProgress,
            hand,
            equipProgress,
            swingProgress
        ) || ExtendoGripRenderHandler.onRenderPlayerHand(
            item,
            client,
            entityRenderDispatcher,
            matrices,
            queue,
            light,
            hand,
            equipProgress,
            swingProgress
        )) {
            return;
        }
        original.call(instance, player, tickProgress, pitch, hand, swingProgress, item, equipProgress, matrices, queue, light);
    }
}
