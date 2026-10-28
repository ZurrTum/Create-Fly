package com.zurrtum.create.client.content.equipment.zapper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
        return Mth.lerp(
            partialTicks,
            rightHand ? lastRightHandAnimation : lastLeftHandAnimation,
            rightHand ? rightHandAnimation : leftHandAnimation
        );
    }

    protected float animationDecay() {
        return 0.8f;
    }

    public void shoot(InteractionHand hand, Vec3 location) {
        LocalPlayer player = Minecraft.getInstance().player;
        boolean rightHand = hand == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;
        if (rightHand) {
            rightHandAnimation = 0.2f;
            dontReequipRight = false;
        } else {
            leftHandAnimation = 0.2f;
            dontReequipLeft = false;
        }
        playSound(hand, location);
    }

    public abstract void playSound(InteractionHand hand, Vec3 position);

    protected abstract boolean appliesTo(ItemStack stack);

    protected abstract void transformTool(PoseStack ms, float flip, float equipProgress, float recoil, float pt);

    protected abstract void transformHand(PoseStack ms, float flip, float equipProgress, float recoil, float pt);

    public boolean onRenderPlayerHand(
        ItemStack heldItem,
        FirstPersonHandsAndItemsRenderer renderer,
        PlayerRenderState playerState,
        FirstPersonHandsAndItemsRenderState state,
        PoseStack ms,
        SubmitNodeCollector queue,
        int light,
        float pt,
        InteractionHand hand,
        float equipProgress,
        float swingProgress
    ) {
        if (!appliesTo(heldItem)) {
            return false;
        }

        boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        boolean rightHand = isMainHand ^ playerState.avatarRenderState.mainArm == HumanoidArm.LEFT;
        float recoil = rightHand ? Mth.lerp(pt, lastRightHandAnimation, rightHandAnimation) :
            Mth.lerp(pt, lastLeftHandAnimation, leftHandAnimation);

        if (rightHand && (rightHandAnimation > 0.01f || dontReequipRight)) {
            equipProgress = 0;
        }
        if (!rightHand && (leftHandAnimation > 0.01f || dontReequipLeft)) {
            equipProgress = 0;
        }

        // Render arm
        float flip = rightHand ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(swingProgress);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2.0F));
        float f4 = -0.4F * Mth.sin(swingProgress * (float) Math.PI);
        float f5 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);

        ms.pushPose();
        Pose pose = ms.last();
        pose.translate(flip * (f2 + 0.64F - 0.1f), f3 + -0.4F + equipProgress * -0.6F, f4 + -0.72F + 0.3f + recoil);
        pose.rotate(Axis.YP, flip * Radians.DEG_75);
        pose.rotate(Axis.YP, flip * f6 * Radians.DEG_70);
        pose.rotate(Axis.ZP, flip * f5 * -Radians.DEG_20);
        pose.translate(flip * -1.0F, 3.6F, 3.5F);
        pose.rotate(Axis.ZP, flip * Radians.DEG_120);
        pose.rotate(Axis.XP, Radians.DEG_200);
        pose.rotate(Axis.YP, flip * Radians.DEG_225);
        pose.translate(flip * 5.6F, 0.0F, 0.0F);
        pose.rotate(Axis.YP, flip * Radians.DEG_40);
        transformHand(ms, flip, equipProgress, recoil, pt);
        renderer.renderPlayerHand(ms, queue, light, rightHand ? HumanoidArm.RIGHT : HumanoidArm.LEFT, playerState);
        ms.popPose();

        // Render gadget
        ms.pushPose();
        pose = ms.last();
        pose.translate(flip * (f2 + 0.64F - 0.1f), f3 + -0.4F + equipProgress * -0.6F, f4 + -0.72F - 0.1f + recoil);
        pose.rotate(Axis.YP, flip * f6 * Radians.DEG_70);
        pose.rotate(Axis.ZP, flip * f5 * -Radians.DEG_20);
        transformTool(ms, flip, equipProgress, recoil, pt);
        (isMainHand ? state.mainHandRenderState : state.offHandRenderState).submit(
            ms,
            queue,
            light,
            OverlayTexture.NO_OVERLAY,
            0
        );
        ms.popPose();
        return true;
    }

    public void dontAnimateItem(InteractionHand hand) {
        LocalPlayer player = Minecraft.getInstance().player;
        boolean rightHand = hand == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;
        dontReequipRight |= rightHand;
        dontReequipLeft |= !rightHand;
    }

}
