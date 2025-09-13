package com.zurrtum.create.client.content.processing.burner;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlazeBurnerRenderer extends SafeBlockEntityRenderer<BlazeBurnerBlockEntity> {

    public BlazeBurnerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(
        BlazeBurnerBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider bufferSource,
        int light,
        int overlay
    ) {
        HeatLevel heatLevel = be.getHeatLevelFromBlock();
        if (heatLevel == HeatLevel.NONE)
            return;

        World level = be.getWorld();
        BlockState blockState = be.getCachedState();
        float animation = be.headAnimation.getValue(partialTicks) * .175f;
        float horizontalAngle = AngleHelper.rad(be.headAngle.getValue(partialTicks));
        boolean canDrawFlame = heatLevel.isAtLeast(HeatLevel.FADING);
        boolean drawGoggles = be.goggles;
        PartialModel drawHat = be.hat ? AllPartialModels.TRAIN_HAT : be.stockKeeper ? AllPartialModels.LOGISTICS_HAT : null;
        int hashCode = be.hashCode();

        renderShared(ms, null, bufferSource, level, blockState, heatLevel, animation, horizontalAngle, canDrawFlame, drawGoggles, drawHat, hashCode);
    }

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider bufferSource,
        LerpedFloat headAngle,
        boolean conductor
    ) {
        BlockState state = context.state;
        HeatLevel heatLevel = BlazeBurnerBlock.getHeatLevelOf(state);
        if (heatLevel == HeatLevel.NONE)
            return;

        if (!heatLevel.isAtLeast(HeatLevel.FADING))
            heatLevel = HeatLevel.FADING;

        World level = context.world;
        float horizontalAngle = AngleHelper.rad(headAngle.getValue(AnimationTickHolder.getPartialTicks(level)));
        boolean drawGoggles = context.blockEntityData.contains("Goggles");
        boolean drawHat = conductor || context.blockEntityData.contains("TrainHat");
        int hashCode = context.hashCode();

        renderShared(
            matrices.getViewProjection(),
            matrices.getModel(),
            bufferSource,
            level,
            state,
            heatLevel,
            0,
            horizontalAngle,
            false,
            drawGoggles,
            drawHat ? AllPartialModels.TRAIN_HAT : null,
            hashCode
        );
    }

    public static void renderShared(
        MatrixStack ms,
        @Nullable MatrixStack modelTransform,
        VertexConsumerProvider bufferSource,
        World level,
        BlockState blockState,
        HeatLevel heatLevel,
        float animation,
        float horizontalAngle,
        boolean canDrawFlame,
        boolean drawGoggles,
        PartialModel drawHat,
        int hashCode
    ) {

        boolean blockAbove = animation > 0.125f;
        float time = AnimationTickHolder.getRenderTime(level);
        float renderTick = time + (hashCode % 13) * 16f;
        float offsetMult = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
        float offset = MathHelper.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
        float offset1 = MathHelper.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
        float offset2 = MathHelper.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;
        float headY = offset - (animation * .75f);

        ms.push();

        var blazeModel = getBlazeModel(heatLevel, blockAbove);

        SuperByteBuffer blazeBuffer = CachedBuffers.partial(blazeModel, blockState);
        if (modelTransform != null)
            blazeBuffer.transform(modelTransform);
        blazeBuffer.translate(0, headY, 0);
        draw(blazeBuffer, horizontalAngle, ms, bufferSource.getBuffer(RenderLayer.getSolid()));

        if (drawGoggles) {
            PartialModel gogglesModel = blazeModel == AllPartialModels.BLAZE_INERT ? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES;

            SuperByteBuffer gogglesBuffer = CachedBuffers.partial(gogglesModel, blockState);
            if (modelTransform != null)
                gogglesBuffer.transform(modelTransform);
            gogglesBuffer.translate(0, headY + 8 / 16f, 0);
            draw(gogglesBuffer, horizontalAngle, ms, bufferSource.getBuffer(RenderLayer.getSolid()));
        }

        if (drawHat != null) {
            SuperByteBuffer hatBuffer = CachedBuffers.partial(drawHat, blockState);
            if (modelTransform != null)
                hatBuffer.transform(modelTransform);
            hatBuffer.translate(0, headY, 0);
            if (blazeModel == AllPartialModels.BLAZE_INERT) {
                hatBuffer.translateY(0.5f).center().scale(0.75f).uncenter();
            } else {
                hatBuffer.translateY(0.75f);
            }
            VertexConsumer cutout = bufferSource.getBuffer(RenderLayer.getCutoutMipped());
            hatBuffer.rotateCentered(horizontalAngle + MathHelper.PI, Direction.UP).translate(0.5f, 0, 0.5f)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(ms, cutout);
        }

        if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            PartialModel rodsModel = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS : AllPartialModels.BLAZE_BURNER_RODS;
            PartialModel rodsModel2 = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2;

            SuperByteBuffer rodsBuffer = CachedBuffers.partial(rodsModel, blockState);
            if (modelTransform != null)
                rodsBuffer.transform(modelTransform);
            rodsBuffer.translate(0, offset1 + animation + .125f, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .renderInto(ms, bufferSource.getBuffer(RenderLayer.getSolid()));

            SuperByteBuffer rodsBuffer2 = CachedBuffers.partial(rodsModel2, blockState);
            if (modelTransform != null)
                rodsBuffer2.transform(modelTransform);
            rodsBuffer2.translate(0, offset2 + animation - 3 / 16f, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .renderInto(ms, bufferSource.getBuffer(RenderLayer.getSolid()));
        }

        if (canDrawFlame && blockAbove) {
            SpriteShiftEntry spriteShift = heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

            float spriteWidth = spriteShift.getTarget().getMaxU() - spriteShift.getTarget().getMinU();

            float spriteHeight = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

            float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

            double vScroll = speed * time;
            vScroll = vScroll - Math.floor(vScroll);
            vScroll = vScroll * spriteHeight / 2;

            double uScroll = speed * time / 2;
            uScroll = uScroll - Math.floor(uScroll);
            uScroll = uScroll * spriteWidth / 2;

            SuperByteBuffer flameBuffer = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState);
            if (modelTransform != null)
                flameBuffer.transform(modelTransform);
            flameBuffer.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll);

            VertexConsumer cutout = bufferSource.getBuffer(RenderLayer.getCutoutMipped());
            draw(flameBuffer, horizontalAngle, ms, cutout);
        }

        ms.pop();
    }

    public static PartialModel getBlazeModel(HeatLevel heatLevel, boolean blockAbove) {
        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            return blockAbove ? AllPartialModels.BLAZE_SUPER_ACTIVE : AllPartialModels.BLAZE_SUPER;
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            return blockAbove && heatLevel.isAtLeast(HeatLevel.KINDLED) ? AllPartialModels.BLAZE_ACTIVE : AllPartialModels.BLAZE_IDLE;
        } else {
            return AllPartialModels.BLAZE_INERT;
        }
    }

    private static void draw(SuperByteBuffer buffer, float horizontalAngle, MatrixStack ms, VertexConsumer vc) {
        buffer.rotateCentered(horizontalAngle, Direction.UP).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(ms, vc);
    }

    public static void tickAnimation(BlazeBurnerBlockEntity be) {
        boolean active = be.getHeatLevelFromBlock().isAtLeast(HeatLevel.FADING) && be.isValidBlockAbove();

        if (!active) {
            float target = 0;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (be.isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (be.getPos().getX() + 0.5);
                double dz = z - (be.getPos().getZ() + 0.5);
                target = AngleHelper.deg(-MathHelper.atan2(dz, dx)) - 90;
            }
            target = be.headAngle.getValue() + AngleHelper.getShortestAngleDiff(be.headAngle.getValue(), target);
            be.headAngle.chase(target, .25f, Chaser.exp(5));
            be.headAngle.tickChaser();
        } else {
            be.headAngle.chase(
                (AngleHelper.horizontalAngle(be.getCachedState().getOrEmpty(BlazeBurnerBlock.FACING).orElse(Direction.SOUTH)) + 180) % 360,
                .125f,
                Chaser.EXP
            );
            be.headAngle.tickChaser();
        }

        be.headAnimation.chase(active ? 1 : 0, .25f, Chaser.exp(.25f));
        be.headAnimation.tickChaser();
    }
}
