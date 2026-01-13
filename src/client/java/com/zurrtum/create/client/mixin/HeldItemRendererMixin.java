package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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
    private EntityRenderDispatcher entityRenderDispatcher;

    @WrapOperation(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
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
        VertexConsumerProvider vertexConsumers,
        int light,
        Operation<Void> original
    ) {
        if (Create.ZAPPER_RENDER_HANDLER.onRenderPlayerHand(
            item,
            client,
            entityRenderDispatcher,
            instance,
            matrices,
            vertexConsumers,
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
            vertexConsumers,
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
            vertexConsumers,
            light,
            hand,
            equipProgress,
            swingProgress
        )) {
            return;
        }
        original.call(instance, player, tickProgress, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
    }

    @WrapOperation(method = "renderArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Arm;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;renderRightArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Z)V"))
    private void renderArmRight(
        PlayerEntityRenderer instance,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        Identifier skinTexture,
        boolean sleeveVisible,
        Operation<Void> original
    ) {
        original.call(instance, matrices, vertexConsumers, light, skinTexture, sleeveVisible);
        Identifier id = NetheriteBacktankFirstPersonRenderer.getHandTexture(client.player);
        if (id != null) {
            original.call(instance, matrices, vertexConsumers, light, id, sleeveVisible);
        }
    }

    @WrapOperation(method = "renderArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Arm;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;renderLeftArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Z)V"))
    private void renderArmLeft(
        PlayerEntityRenderer instance,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        Identifier skinTexture,
        boolean sleeveVisible,
        Operation<Void> original
    ) {
        original.call(instance, matrices, vertexConsumers, light, skinTexture, sleeveVisible);
        Identifier id = NetheriteBacktankFirstPersonRenderer.getHandTexture(client.player);
        if (id != null) {
            original.call(instance, matrices, vertexConsumers, light, id, sleeveVisible);
        }
    }

    @WrapOperation(method = "renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;renderRightArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Z)V"))
    private void renderArmHoldingItemRight(
        PlayerEntityRenderer instance,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        Identifier skinTexture,
        boolean sleeveVisible,
        Operation<Void> original
    ) {
        original.call(instance, matrices, vertexConsumers, light, skinTexture, sleeveVisible);
        Identifier id = NetheriteBacktankFirstPersonRenderer.getHandTexture(client.player);
        if (id != null) {
            original.call(instance, matrices, vertexConsumers, light, id, sleeveVisible);
        }
    }

    @WrapOperation(method = "renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;renderLeftArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Z)V"))
    private void renderArmHoldingItemLeft(
        PlayerEntityRenderer instance,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        Identifier skinTexture,
        boolean sleeveVisible,
        Operation<Void> original
    ) {
        original.call(instance, matrices, vertexConsumers, light, skinTexture, sleeveVisible);
        Identifier id = NetheriteBacktankFirstPersonRenderer.getHandTexture(client.player);
        if (id != null) {
            original.call(instance, matrices, vertexConsumers, light, id, sleeveVisible);
        }
    }
}
