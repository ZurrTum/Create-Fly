package com.zurrtum.create.client.compat.trinkets;

import com.zurrtum.create.AllItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class GoggleTrinketRenderer implements TrinketRenderer {
    private BipedEntityModel<BipedEntityRenderState> model;

    @Override
    public void render(
        ItemStack stack,
        SlotReference slotReference,
        EntityModel<? extends LivingEntityRenderState> contextModel,
        MatrixStack matrices,
        VertexConsumerProvider buffers,
        int light,
        LivingEntityRenderState state,
        float headYaw,
        float headPitch
    ) {
        if (stack.isOf(AllItems.GOGGLES) && contextModel instanceof PlayerEntityModel entityModel) {
            matrices.push();
            entityModel.getRootPart().applyTransform(matrices);
            entityModel.getHead().applyTransform(matrices);
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            matrices.scale(0.625F, -0.625F, -0.625F);
            if (headOccupied((PlayerEntityRenderState) state, slotReference)) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
                matrices.translate(0.0F, -0.25F, 0.0F);
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.getItemRenderer().renderItem(stack, ItemDisplayContext.HEAD, light, OverlayTexture.DEFAULT_UV, matrices, buffers, mc.world, 0);
            matrices.pop();
        }
    }

    public static boolean headOccupied(PlayerEntityRenderState state, SlotReference slotReference) {
        if (!state.equippedHeadStack.isEmpty()) {
            return true;
        }
        TrinketInventory inv = slotReference.inventory().getComponent().getInventory().get("head").get("hat");
        return inv != null && !inv.isEmpty();
    }
}
