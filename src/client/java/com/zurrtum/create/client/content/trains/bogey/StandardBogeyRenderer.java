package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class StandardBogeyRenderer implements BogeyRenderer {
    @Override
    public void render(
        NbtCompound bogeyData,
        float wheelAngle,
        float partialTick,
        MatrixStack poseStack,
        VertexConsumerProvider bufferSource,
        int light,
        int overlay,
        boolean inContraption
    ) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderLayer.getCutoutMipped());

        SuperByteBuffer shaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState().with(ShaftBlock.AXIS, Direction.Axis.Z));
        for (int i : Iterate.zeroAndOne) {
            shaft.translate(-.5f, .25f, i * -1).center().rotateZDegrees(wheelAngle).uncenter().light(light).overlay(overlay)
                .renderInto(poseStack, buffer);
        }
    }

    public static class Small extends StandardBogeyRenderer {
        @Override
        public void render(
            NbtCompound bogeyData,
            float wheelAngle,
            float partialTick,
            MatrixStack poseStack,
            VertexConsumerProvider bufferSource,
            int light,
            int overlay,
            boolean inContraption
        ) {
            super.render(bogeyData, wheelAngle, partialTick, poseStack, bufferSource, light, overlay, inContraption);

            VertexConsumer buffer = bufferSource.getBuffer(RenderLayer.getCutoutMipped());

            CachedBuffers.partial(AllPartialModels.BOGEY_FRAME, Blocks.AIR.getDefaultState()).scale(1 - 1 / 512f).light(light).overlay(overlay)
                .renderInto(poseStack, buffer);

            SuperByteBuffer wheels = CachedBuffers.partial(AllPartialModels.SMALL_BOGEY_WHEELS, Blocks.AIR.getDefaultState());
            for (int side : Iterate.positiveAndNegative) {
                wheels.translate(0, 12 / 16f, side).rotateXDegrees(wheelAngle).light(light).overlay(overlay).renderInto(poseStack, buffer);
            }
        }
    }

    public static class Large extends StandardBogeyRenderer {
        public static final float BELT_RADIUS_PX = 5f;
        public static final float BELT_RADIUS_IN_UV_SPACE = BELT_RADIUS_PX / 16f;

        @Override
        public void render(
            NbtCompound bogeyData,
            float wheelAngle,
            float partialTick,
            MatrixStack poseStack,
            VertexConsumerProvider bufferSource,
            int light,
            int overlay,
            boolean inContraption
        ) {
            super.render(bogeyData, wheelAngle, partialTick, poseStack, bufferSource, light, overlay, inContraption);

            VertexConsumer buffer = bufferSource.getBuffer(RenderLayer.getCutoutMipped());

            SuperByteBuffer secondaryShaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState().with(ShaftBlock.AXIS, Direction.Axis.X));
            for (int i : Iterate.zeroAndOne) {
                secondaryShaft.translate(-.5f, .25f, .5f + i * -2).center().rotateXDegrees(wheelAngle).uncenter().light(light).overlay(overlay)
                    .renderInto(poseStack, buffer);
            }

            CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE, Blocks.AIR.getDefaultState()).scale(1 - 1 / 512f).light(light).overlay(overlay)
                .renderInto(poseStack, buffer);

            float spriteSize = AllSpriteShifts.BOGEY_BELT.getTarget().getMaxV() - AllSpriteShifts.BOGEY_BELT.getTarget().getMinV();

            float scroll = BELT_RADIUS_IN_UV_SPACE * MathHelper.RADIANS_PER_DEGREE * wheelAngle;
            scroll = scroll - MathHelper.floor(scroll);
            scroll = scroll * spriteSize * 0.5f;

            CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE_BELT, Blocks.AIR.getDefaultState()).scale(1 - 1 / 512f).light(light).overlay(overlay)
                .shiftUVScrolling(AllSpriteShifts.BOGEY_BELT, scroll).renderInto(poseStack, buffer);

            CachedBuffers.partial(AllPartialModels.BOGEY_PISTON, Blocks.AIR.getDefaultState())
                .translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle))).light(light).overlay(overlay).renderInto(poseStack, buffer);

            CachedBuffers.partial(AllPartialModels.LARGE_BOGEY_WHEELS, Blocks.AIR.getDefaultState()).translate(0, 1, 0).rotateXDegrees(wheelAngle)
                .light(light).overlay(overlay).renderInto(poseStack, buffer);

            CachedBuffers.partial(AllPartialModels.BOGEY_PIN, Blocks.AIR.getDefaultState()).translate(0, 1, 0).rotateXDegrees(wheelAngle)
                .translate(0, 1 / 4f, 0).rotateXDegrees(-wheelAngle).light(light).overlay(overlay).renderInto(poseStack, buffer);
        }
    }
}
