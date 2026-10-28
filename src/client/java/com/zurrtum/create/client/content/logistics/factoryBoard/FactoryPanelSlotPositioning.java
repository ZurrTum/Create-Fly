package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelSlotPositioning extends ValueBoxTransform {

    public PanelSlot slot;

    public FactoryPanelSlotPositioning(PanelSlot slot) {
        this.slot = slot;
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        return getCenterOfSlot(state, slot);
    }

    public static Vec3 getCenterOfSlot(BlockState state, PanelSlot slot) {
        Vec3 vec = new Vec3(0.25 + slot.xOffset * 0.5, 1.5 / 16.0f, 0.25 + slot.yOffset * 0.5);
        vec = VecHelper.rotateCentered(vec, 180, Direction.Axis.Y);
        vec = VecHelper.rotateCentered(vec, Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state) + 90, Direction.Axis.X);
        vec = VecHelper.rotateCentered(vec, Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state), Direction.Axis.Y);
        return vec;
    }

    @Override
    public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
        return localHit.distanceTo(getLocalOffset(state)) < scale / 2;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Pose pose = ms.last();
        pose.rotate(Axis.YP, FactoryPanelBlock.getYRot(state) + Radians.DEG_180);
        pose.rotate(Axis.XP, -FactoryPanelBlock.getXRot(state));
    }

}
