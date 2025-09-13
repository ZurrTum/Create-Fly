package com.zurrtum.create.client.content.fluids.pipes;


import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.fluids.pipes.SmartFluidPipeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class SmartPipeFilterSlot extends ValueBoxTransform {

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        BlockFace face = state.get(SmartFluidPipeBlock.FACE);
        float y = face == BlockFace.CEILING ? 0.55f : face == BlockFace.WALL ? 11.4f : 15.45f;
        float z = face == BlockFace.CEILING ? 4.6f : face == BlockFace.WALL ? 0.55f : 4.625f;
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, y, z), angleY(state), Axis.Y);
    }

    @Override
    public float getScale() {
        return super.getScale() * 1.02f;
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        BlockFace face = state.get(SmartFluidPipeBlock.FACE);
        TransformStack.of(ms).rotateYDegrees(angleY(state)).rotateXDegrees(face == BlockFace.CEILING ? -45 : 45);
    }

    protected float angleY(BlockState state) {
        BlockFace face = state.get(SmartFluidPipeBlock.FACE);
        float horizontalAngle = AngleHelper.horizontalAngle(state.get(SmartFluidPipeBlock.FACING));
        if (face == BlockFace.WALL)
            horizontalAngle += 180;
        return horizontalAngle;
    }
}