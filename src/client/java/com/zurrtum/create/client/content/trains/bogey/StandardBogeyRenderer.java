package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class StandardBogeyRenderer implements BogeyRenderer {
    public static void updateRenderState(
        StandardBogeyRenderState data,
        float wheelAngle,
        int light,
        @Nullable CardinalLighting cardinalLighting
    ) {
        data.shaft = CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState().setValue(ShaftBlock.AXIS, Axis.Z))
            .cardinalLighting(cardinalLighting).light(light).extractRenderState();
        data.wheelRadians = Mth.DEG_TO_RAD * wheelAngle;
        data.offset = -1.5 - 1 / 128.0f;
    }

    @Override
    public BogeyRenderState getRenderData(
        CompoundTag bogeyData,
        float wheelAngle,
        float tickProgress,
        int light,
        @Nullable CardinalLighting cardinalLighting,
        boolean inContraption
    ) {
        StandardBogeyRenderState data = new StandardBogeyRenderState();
        updateRenderState(data, wheelAngle, light, cardinalLighting);
        return data;
    }

    public static class Small extends StandardBogeyRenderer {
        @Override
        public BogeyRenderState getRenderData(
            CompoundTag bogeyData,
            float wheelAngle,
            float tickProgress,
            int light,
            @Nullable CardinalLighting cardinalLighting,
            boolean inContraption
        ) {
            SmallBogeyRenderState data = new SmallBogeyRenderState();
            updateRenderState(data, wheelAngle, light, cardinalLighting);
            BlockState air = Blocks.AIR.defaultBlockState();
            data.frame = CachedBuffers.partial(AllPartialModels.BOGEY_FRAME, air).cardinalLighting(cardinalLighting)
                .light(light).extractRenderState();
            data.wheels = CachedBuffers.partial(AllPartialModels.SMALL_BOGEY_WHEELS, air)
                .cardinalLighting(cardinalLighting).light(light).extractRenderState();
            return data;
        }
    }

    public static class Large extends StandardBogeyRenderer {
        public static final float BELT_RADIUS_PX = 5.0f;
        public static final float BELT_RADIUS_IN_UV_SPACE = BELT_RADIUS_PX / 16.0f;

        @Override
        public BogeyRenderState getRenderData(
            CompoundTag bogeyData,
            float wheelAngle,
            float tickProgress,
            int light,
            @Nullable CardinalLighting cardinalLighting,
            boolean inContraption
        ) {
            LargeBogeyRenderState data = new LargeBogeyRenderState();
            updateRenderState(data, wheelAngle, light, cardinalLighting);
            data.secondaryShaft = CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState()
                    .setValue(ShaftBlock.AXIS, Axis.X)).cardinalLighting(cardinalLighting).light(light)
                .extractRenderState();
            BlockState air = Blocks.AIR.defaultBlockState();
            data.drive = CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE, air).cardinalLighting(cardinalLighting)
                .light(light).extractRenderState();
            float spriteSize = AllSpriteShifts.BOGEY_BELT.getTarget().getV1() - AllSpriteShifts.BOGEY_BELT.getTarget()
                .getV0();
            float scroll = BELT_RADIUS_IN_UV_SPACE * data.wheelRadians;
            scroll = scroll - Mth.floor(scroll);
            scroll = scroll * spriteSize * 0.5f;
            data.belt = CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE_BELT, air)
                .shiftUVScrolling(AllSpriteShifts.BOGEY_BELT, scroll).cardinalLighting(cardinalLighting).light(light)
                .extractRenderState();
            data.piston = CachedBuffers.partial(AllPartialModels.BOGEY_PISTON, air).cardinalLighting(cardinalLighting)
                .light(light).extractRenderState();
            data.pistonOffset = (float) (1 / 4.0f * Math.sin(data.wheelRadians));
            data.wheels = CachedBuffers.partial(AllPartialModels.LARGE_BOGEY_WHEELS, air)
                .cardinalLighting(cardinalLighting).light(light).extractRenderState();
            data.pin = CachedBuffers.partial(AllPartialModels.BOGEY_PIN, air).cardinalLighting(cardinalLighting)
                .light(light).extractRenderState();
            return data;
        }
    }
}
