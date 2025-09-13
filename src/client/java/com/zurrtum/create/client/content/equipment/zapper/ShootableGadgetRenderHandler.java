package com.zurrtum.create.client.content.equipment.zapper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public abstract class ShootableGadgetRenderHandler {

    protected float leftHandAnimation;
    protected float rightHandAnimation;
    protected float lastLeftHandAnimation;
    protected float lastRightHandAnimation;
    protected boolean dontReequipLeft;
    protected boolean dontReequipRight;

    public void tick() {
        lastLeftHandAnimation = leftHandAnimation;
        lastRightHandAnimation = rightHandAnimation;
        leftHandAnimation *= animationDecay();
        rightHandAnimation *= animationDecay();
    }

    public float getAnimation(boolean rightHand, float partialTicks) {
        return MathHelper.lerp(
            partialTicks,
            rightHand ? lastRightHandAnimation : lastLeftHandAnimation,
            rightHand ? rightHandAnimation : leftHandAnimation
        );
    }

    protected float animationDecay() {
        return 0.8f;
    }

    public void shoot(Hand hand, Vec3d location) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        boolean rightHand = hand == Hand.MAIN_HAND ^ player.getMainArm() == Arm.LEFT;
        if (rightHand) {
            rightHandAnimation = .2f;
            dontReequipRight = false;
        } else {
            leftHandAnimation = .2f;
            dontReequipLeft = false;
        }
        playSound(hand, location);
    }

    public abstract void playSound(Hand hand, Vec3d position);

    protected abstract boolean appliesTo(ItemStack stack);

    protected abstract void transformTool(MatrixStack ms, float flip, float equipProgress, float recoil, float pt);

    protected abstract void transformHand(MatrixStack ms, float flip, float equipProgress, float recoil, float pt);

    public boolean onRenderPlayerHand(
        ItemStack heldItem,
        MinecraftClient mc,
        EntityRenderDispatcher entityRenderDispatcher,
        HeldItemRenderer firstPersonRenderer,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        float pt,
        Hand hand,
        float equipProgress,
        float swingProgress
    ) {
        if (!appliesTo(heldItem))
            return false;

        AbstractClientPlayerEntity player = mc.player;
        PlayerEntityRenderer playerrenderer = (PlayerEntityRenderer) entityRenderDispatcher.getRenderer(player);

        boolean rightHand = hand == Hand.MAIN_HAND ^ player.getMainArm() == Arm.LEFT;
        float recoil = rightHand ? MathHelper.lerp(pt, lastRightHandAnimation, rightHandAnimation) : MathHelper.lerp(
            pt,
            lastLeftHandAnimation,
            leftHandAnimation
        );

        if (rightHand && (rightHandAnimation > .01f || dontReequipRight))
            equipProgress = 0;
        if (!rightHand && (leftHandAnimation > .01f || dontReequipLeft))
            equipProgress = 0;

        // Render arm
        float flip = rightHand ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(swingProgress);
        float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
        float f4 = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
        float f5 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f6 = MathHelper.sin(f1 * (float) Math.PI);

        ms.push();
        ms.translate(flip * (f2 + 0.64F - .1f), f3 + -0.4F + equipProgress * -0.6F, f4 + -0.72F + .3f + recoil);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * 75.0F));
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * f6 * 70.0F));
        ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flip * f5 * -20.0F));
        ms.translate(flip * -1.0F, 3.6F, 3.5F);
        ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flip * 120.0F));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0F));
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * -135.0F));
        ms.translate(flip * 5.6F, 0.0F, 0.0F);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * 40.0F));
        transformHand(ms, flip, equipProgress, recoil, pt);
        Identifier texture = player.getSkinTextures().texture();
        if (rightHand)
            playerrenderer.renderRightArm(ms, buffer, light, texture, player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE));
        else
            playerrenderer.renderLeftArm(ms, buffer, light, texture, player.isPartVisible(PlayerModelPart.LEFT_SLEEVE));
        ms.pop();

        // Render gadget
        ms.push();
        ms.translate(flip * (f2 + 0.64F - .1f), f3 + -0.4F + equipProgress * -0.6F, f4 + -0.72F - 0.1f + recoil);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * f6 * 70.0F));
        ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flip * f5 * -20.0F));
        transformTool(ms, flip, equipProgress, recoil, pt);
        firstPersonRenderer.renderItem(
            player,
            heldItem,
            rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            ms,
            buffer,
            light
        );
        ms.pop();
        return true;
    }

    public void dontAnimateItem(Hand hand) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        boolean rightHand = hand == Hand.MAIN_HAND ^ player.getMainArm() == Arm.LEFT;
        dontReequipRight |= rightHand;
        dontReequipLeft |= !rightHand;
    }

}
