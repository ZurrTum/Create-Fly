package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StandardBogeyRenderer implements BogeyRenderer {
    public static void updateRenderState(StandardBogeyRenderState data, float wheelAngle, int light) {
        data.layer = RenderType.cutoutMipped();
        data.shaft = CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState().setValue(ShaftBlock.AXIS, Direction.Axis.Z));
        data.angle = Mth.DEG_TO_RAD * wheelAngle;
        data.light = light;
        data.offset = -1.5 - 1 / 128f;
    }

    @Override
    public BogeyRenderState getRenderData(@Nullable CompoundTag bogeyData, float wheelAngle, float tickProgress, int light, boolean inContraption) {
        StandardBogeyRenderState data = new StandardBogeyRenderState();
        updateRenderState(data, wheelAngle, light);
        return data;
    }

    public static class Small extends StandardBogeyRenderer {
        @Override
        public BogeyRenderState getRenderData(
            @Nullable CompoundTag bogeyData,
            float wheelAngle,
            float tickProgress,
            int light,
            boolean inContraption
        ) {
            SmallBogeyRenderState data = new SmallBogeyRenderState();
            updateRenderState(data, wheelAngle, light);
            BlockState air = Blocks.AIR.defaultBlockState();
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
            @Nullable CompoundTag bogeyData,
            float wheelAngle,
            float tickProgress,
            int light,
            boolean inContraption
        ) {
            LargeBogeyRenderState data = new LargeBogeyRenderState();
            updateRenderState(data, wheelAngle, light);
            data.secondaryShaft = CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState().setValue(ShaftBlock.AXIS, Direction.Axis.X));
            BlockState air = Blocks.AIR.defaultBlockState();
            data.drive = CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE, air);
            data.belt = CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE_BELT, air);
            float spriteSize = AllSpriteShifts.BOGEY_BELT.getTarget().getV1() - AllSpriteShifts.BOGEY_BELT.getTarget().getV0();
            float scroll = BELT_RADIUS_IN_UV_SPACE * Mth.DEG_TO_RAD * wheelAngle;
            scroll = scroll - Mth.floor(scroll);
            data.scroll = scroll * spriteSize * 0.5f;
            data.piston = CachedBuffers.partial(AllPartialModels.BOGEY_PISTON, air);
            data.pistonOffset = (float) (1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)));
            data.wheels = CachedBuffers.partial(AllPartialModels.LARGE_BOGEY_WHEELS, air);
            data.pin = CachedBuffers.partial(AllPartialModels.BOGEY_PIN, air);
            return data;
        }
    }
}
