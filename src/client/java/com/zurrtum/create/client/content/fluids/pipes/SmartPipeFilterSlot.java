package com.zurrtum.create.client.content.fluids.pipes;


import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.fluids.pipes.SmartFluidPipeBlock;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

public class SmartPipeFilterSlot extends ValueBoxTransform {

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
        float y = face == AttachFace.CEILING ? 0.55f : face == AttachFace.WALL ? 11.4f : 15.45f;
        float z = face == AttachFace.CEILING ? 4.6f : face == AttachFace.WALL ? 0.55f : 4.625f;
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, y, z), angleY(state), Axis.Y);
    }

    @Override
    public float getScale() {
        return super.getScale() * 1.02f;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
        TransformStack.of(ms).rotateYDegrees(angleY(state)).rotateXDegrees(face == AttachFace.CEILING ? -45 : 45);
    }

    protected float angleY(BlockState state) {
        AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
        float horizontalAngle = AngleHelper.horizontalAngle(state.getValue(SmartFluidPipeBlock.FACING));
        if (face == AttachFace.WALL)
            horizontalAngle += 180;
        return horizontalAngle;
    }
}