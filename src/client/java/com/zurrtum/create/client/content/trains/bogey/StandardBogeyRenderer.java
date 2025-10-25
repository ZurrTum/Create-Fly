package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyBlockEntityRenderState;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class StandardBogeyRenderer implements BogeyRenderer {
    public static void updateRenderState(StandardBogeyRenderState data, BogeyBlockEntityRenderState state) {
        data.layer = RenderLayer.getCutoutMipped();
        data.shaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState().with(ShaftBlock.AXIS, Direction.Axis.Z));
        data.angle = MathHelper.RADIANS_PER_DEGREE * state.angle;
        data.light = state.lightmapCoordinates;
        data.offset = -1.5 - 1 / 128f;
    }

    @Override
    public BogeyRenderState getRenderData(
        AbstractBogeyBlockEntity be,
        BogeyBlockEntityRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        boolean inContraption
    ) {
        StandardBogeyRenderState data = new StandardBogeyRenderState();
        updateRenderState(data, state);
        return data;
    }

    public static class Small extends StandardBogeyRenderer {
        @Override
        public BogeyRenderState getRenderData(
            AbstractBogeyBlockEntity be,
            BogeyBlockEntityRenderState state,
            float tickProgress,
            Vec3d cameraPos,
            boolean inContraption
        ) {
            SmallBogeyRenderState data = new SmallBogeyRenderState();
            updateRenderState(data, state);
            BlockState air = Blocks.AIR.getDefaultState();
            data.frame = CachedBuffers.partial(AllPartialModels.BOGEY_FRAME, air);
            data.wheels = CachedBuffers.partial(AllPartialModels.SMALL_BOGEY_WHEELS, air);
            return data;
        }
    }

    public static class Large extends StandardBogeyRenderer {
        public static final float BELT_RADIUS_PX = 5f;
        public static final float BELT_RADIUS_IN_UV_SPACE = BELT_RADIUS_PX / 16f;

        @Override
        public BogeyRenderState getRenderData(
            AbstractBogeyBlockEntity be,
            BogeyBlockEntityRenderState state,
            float tickProgress,
            Vec3d cameraPos,
            boolean inContraption
        ) {
            LargeBogeyRenderState data = new LargeBogeyRenderState();
            updateRenderState(data, state);
            data.secondaryShaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState().with(ShaftBlock.AXIS, Direction.Axis.X));
            BlockState air = Blocks.AIR.getDefaultState();
            data.drive = CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE, air);
            data.belt = CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE_BELT, air);
            float spriteSize = AllSpriteShifts.BOGEY_BELT.getTarget().getMaxV() - AllSpriteShifts.BOGEY_BELT.getTarget().getMinV();
            float scroll = BELT_RADIUS_IN_UV_SPACE * MathHelper.RADIANS_PER_DEGREE * state.angle;
            scroll = scroll - MathHelper.floor(scroll);
            data.scroll = scroll * spriteSize * 0.5f;
            data.piston = CachedBuffers.partial(AllPartialModels.BOGEY_PISTON, air);
            data.pistonOffset = (float) (1 / 4f * Math.sin(AngleHelper.rad(state.angle)));
            data.wheels = CachedBuffers.partial(AllPartialModels.LARGE_BOGEY_WHEELS, air);
            data.pin = CachedBuffers.partial(AllPartialModels.BOGEY_PIN, air);
            return data;
        }
    }
}
