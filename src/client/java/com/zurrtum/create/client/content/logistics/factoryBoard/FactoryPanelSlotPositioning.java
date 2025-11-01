package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class FactoryPanelSlotPositioning extends ValueBoxTransform {

    public PanelSlot slot;

    public FactoryPanelSlotPositioning(PanelSlot slot) {
        this.slot = slot;
    }

    @Override
    public Vec3d getLocalOffset(BlockState state) {
        return getCenterOfSlot(state, slot);
    }

    public static Vec3d getCenterOfSlot(BlockState state, PanelSlot slot) {
        Vec3d vec = new Vec3d(.25 + slot.xOffset * .5, 1.5 / 16f, .25 + slot.yOffset * .5);
        vec = VecHelper.rotateCentered(vec, 180, Axis.Y);
        vec = VecHelper.rotateCentered(vec, MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getXRot(state) + 90, Axis.X);
        vec = VecHelper.rotateCentered(vec, MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getYRot(state), Axis.Y);
        return vec;
    }

    @Override
    public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
        Vec3d offset = getLocalOffset(state);
        if (offset == null)
            return false;
        return localHit.distanceTo(offset) < scale / 2;
    }

    @Override
    public float getScale() {
        return super.getScale();
    }

    @Override
    public void rotate(BlockState state, MatrixStack ms) {
        TransformStack.of(ms).rotate(FactoryPanelBlock.getYRot(state) + MathHelper.PI, Direction.UP)
            .rotate(-FactoryPanelBlock.getXRot(state), Direction.EAST);
    }

}
