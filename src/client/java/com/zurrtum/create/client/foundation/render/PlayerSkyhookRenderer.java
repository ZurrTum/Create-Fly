package com.zurrtum.create.client.foundation.render;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerSkyhookRenderer {

    private static final Set<UUID> hangingPlayers = new HashSet<>();

    public static void updatePlayerList(Collection<UUID> uuids) {
        hangingPlayers.clear();
        hangingPlayers.addAll(uuids);
    }

    public static void afterSetupAnim(UUID uuid, Arm mainArm, ItemStack stack, PlayerEntityModel model) {
        if (hangingPlayers.contains(uuid))
            setHangingPose(mainArm == Arm.LEFT ^ !stack.isIn(AllItemTags.CHAIN_RIDEABLE), model);
    }

    private static void setHangingPose(boolean isLeftArmMain, PlayerEntityModel model) {
        model.head.originX = 0;
        model.hat.originX = 0;
        model.body.resetTransform();
        model.leftArm.resetTransform();
        model.rightArm.resetTransform();
        model.leftLeg.resetTransform();
        model.rightLeg.resetTransform();

        float time = AnimationTickHolder.getTicks() + AnimationTickHolder.getPartialTicks();
        float mainCycle = MathHelper.sin(((float) ((time + 10) * 0.3f / Math.PI)));
        float limbCycle = MathHelper.sin(((float) (time * 0.3f / Math.PI)));
        float bodySwing = AngleHelper.rad(15 + (mainCycle * 10));
        float limbSwing = AngleHelper.rad(limbCycle * 15);
        if (isLeftArmMain)
            bodySwing = -bodySwing;
        model.body.roll = bodySwing;
        model.head.roll = bodySwing;

        ModelPart hangingArm = isLeftArmMain ? model.leftArm : model.rightArm;
        ModelPart otherArm = isLeftArmMain ? model.rightArm : model.leftArm;
        hangingArm.originY -= 3;

        float offsetX = hangingArm.originX;
        float offsetY = hangingArm.originY;
        //		model.rightArm.x = offsetX * Mth.cos(bodySwing) - offsetY * Mth.sin(bodySwing);
        //		model.rightArm.y = offsetX * Mth.sin(bodySwing) + offsetY * Mth.cos(bodySwing);
        float armPivotX = offsetX * MathHelper.cos(bodySwing) - offsetY * MathHelper.sin(bodySwing) + (isLeftArmMain ? -1 : 1) * 4.5f;
        float armPivotY = offsetX * MathHelper.sin(bodySwing) + offsetY * MathHelper.cos(bodySwing) + 2;
        hangingArm.pitch = -AngleHelper.rad(150);
        hangingArm.roll = (isLeftArmMain ? -1 : 1) * AngleHelper.rad(15);

        offsetX = otherArm.originX;
        offsetY = otherArm.originY;
        otherArm.originX = offsetX * MathHelper.cos(bodySwing) - offsetY * MathHelper.sin(bodySwing);
        otherArm.originY = offsetX * MathHelper.sin(bodySwing) + offsetY * MathHelper.cos(bodySwing);
        otherArm.roll = (isLeftArmMain ? -1 : 1) * (-AngleHelper.rad(20)) + 0.5f * bodySwing + limbSwing;

        ModelPart leadingLeg = isLeftArmMain ? model.leftLeg : model.rightLeg;
        ModelPart trailingLeg = isLeftArmMain ? model.rightLeg : model.leftLeg;

        leadingLeg.originY -= 0.2f;
        offsetX = leadingLeg.originX;
        offsetY = leadingLeg.originY;
        leadingLeg.originX = offsetX * MathHelper.cos(bodySwing) - offsetY * MathHelper.sin(bodySwing);
        leadingLeg.originY = offsetX * MathHelper.sin(bodySwing) + offsetY * MathHelper.cos(bodySwing);
        leadingLeg.pitch = -AngleHelper.rad(25);
        leadingLeg.roll = (isLeftArmMain ? -1 : 1) * (AngleHelper.rad(10)) + 0.5f * bodySwing + limbSwing;
        trailingLeg.originY -= 0.8f;
        offsetX = trailingLeg.originX;
        offsetY = trailingLeg.originY;
        trailingLeg.originX = offsetX * MathHelper.cos(bodySwing) - offsetY * MathHelper.sin(bodySwing);
        trailingLeg.originY = offsetX * MathHelper.sin(bodySwing) + offsetY * MathHelper.cos(bodySwing);
        trailingLeg.pitch = AngleHelper.rad(10);
        trailingLeg.roll = (isLeftArmMain ? -1 : 1) * (-AngleHelper.rad(10)) + 0.5f * bodySwing + limbSwing;
        model.head.originX -= armPivotX;
        model.body.originX -= armPivotX;
        otherArm.originX -= armPivotX;
        trailingLeg.originX -= armPivotX;
        leadingLeg.originX -= armPivotX;

        model.head.originY -= armPivotY;
        model.body.originY -= armPivotY;
        otherArm.originY -= armPivotY;
        trailingLeg.originY -= armPivotY;
        leadingLeg.originY -= armPivotY;
    }

}
