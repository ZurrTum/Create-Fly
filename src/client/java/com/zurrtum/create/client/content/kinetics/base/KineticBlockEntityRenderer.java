package com.zurrtum.create.client.content.kinetics.base;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache.Compartment;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KineticRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class KineticBlockEntityRenderer<T extends KineticBlockEntity, S extends KineticRenderState> implements BlockEntityRenderer<T, S> {
    /**
     * @deprecated {@link Radians#DEG_90}
     */
    @Deprecated(forRemoval = true)
    public static final float RAD_90 = Mth.DEG_TO_RAD * 90;
    /**
     * @deprecated {@link Radians#DEG_180}
     */
    @Deprecated(forRemoval = true)
    public static final float RAD_180 = Mth.DEG_TO_RAD * 180;
    /**
     * @deprecated {@link Radians#DEG_270}
     */
    @Deprecated(forRemoval = true)
    public static final float RAD_270 = Mth.DEG_TO_RAD * 270;
    /**
     * @deprecated {@link Radians#DEG_360}
     */
    @Deprecated(forRemoval = true)
    public static final float RAD_360 = Mth.DEG_TO_RAD * 360;
    public static final Compartment<BlockState> KINETIC_BLOCK = new Compartment<>();
    public static boolean rainbowMode;

    public KineticBlockEntityRenderer(Context context) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new KineticRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        S state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        state.support = VisualizationManager.supportsVisualization(world);
        if (state.support) {
            return;
        }
        updateBaseRenderState(be, state, world, crumblingOverlay);
        state.angle = getDegrees(state.axis, be, state, world);
        state.model = getRotatedModel(be, state).cardinalLighting(state.cardinalLighting).light(state.lightCoords)
            .color(state.color).extractRenderState();
    }

    public void updateBaseRenderState(
        T be,
        S state,
        @Nullable Level level,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        state.blockPos = be.getBlockPos();
        state.blockState = getRenderedBlockState(be);
        state.blockEntityType = be.getType();
        state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
        state.breakProgress = crumblingOverlay;
        state.axis = getRotationAxisOf(state.blockState);
        state.direction = state.axis.getPositive();
        state.color = getTintColor(be);
        state.cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
    }

    @Override
    public void submit(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.model != null) {
            if (state.angle != 0) {
                matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            }
            state.model.submit(matrices, queue);
        }
    }

    protected BlockState getRenderedBlockState(T be) {
        return be.getBlockState();
    }

    protected SuperByteBuffer getRotatedModel(T be, S state) {
        return CachedBuffers.block(KINETIC_BLOCK, state.blockState);
    }

    public static float getAngleForBe(KineticBlockEntity be, final BlockPos pos, Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return (time * be.getSpeed() * 0.3f + offset) % 360 * Mth.DEG_TO_RAD;
    }

    public static float getDegrees(Axis axis, KineticBlockEntity be) {
        float progress = getProgress(be, be.getLevel());
        float offset = getRotationOffsetForPosition(be, be.getBlockPos(), axis);
        return (progress + offset) % 360;
    }

    public static float getDegreesWithoutBeOffset(Axis axis, KineticBlockEntity be) {
        float progress = getProgress(be, be.getLevel());
        float offset = KineticBlockEntityVisual.rotationOffset(be.getBlockState(), axis, be.getBlockPos());
        return (progress + offset) % 360;
    }

    public static float getDegrees(
        Axis axis,
        KineticBlockEntity be,
        BlockEntityRenderState state,
        @Nullable LevelAccessor level
    ) {
        float progress = getProgress(be, level);
        float offset = KineticBlockEntityVisual.rotationOffset(
            state.blockState,
            axis,
            state.blockPos
        ) + be.getRotationAngleOffset(axis);
        return (progress + offset) % 360;
    }

    public static float getDegreesWithoutBeOffset(
        Axis axis,
        KineticBlockEntity be,
        BlockEntityRenderState state,
        @Nullable LevelAccessor level
    ) {
        float progress = getProgress(be, level);
        float offset = KineticBlockEntityVisual.rotationOffset(state.blockState, axis, state.blockPos);
        return (progress + offset) % 360;
    }

    /**
     * @deprecated {@link getDegrees(Axis, KineticBlockEntity, BlockEntityRenderState, LevelAccessor)}
     * rotate(Axis, Mth.DEG_TO_RAD * degrees)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRotateAngleForBe(
        Axis axis,
        Direction direction,
        KineticBlockEntity be,
        BlockEntityRenderState state,
        @Nullable Level level
    ) {
        float offset = KineticBlockEntityVisual.rotationOffset(
            state.blockState,
            axis,
            state.blockPos
        ) + be.getRotationAngleOffset(axis);
        return getRotateAngle(getProgress(be, level), offset, direction);
    }

    /**
     * @deprecated {@link getDegreesWithoutBeOffset(Axis, KineticBlockEntity, BlockEntityRenderState, LevelAccessor)}
     * rotate(Axis, Mth.DEG_TO_RAD * degrees)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRotateAngleWithoutBeOffset(
        KineticBlockEntity be,
        BlockEntityRenderState state,
        @Nullable LevelAccessor level
    ) {
        return getRotateAngleWithoutBeOffset(getRotationAxisOf(state.blockState), be, state, level);
    }

    /**
     * @deprecated {@link getDegreesWithoutBeOffset(Axis, KineticBlockEntity, BlockEntityRenderState, LevelAccessor)}
     * rotate(Axis, Mth.DEG_TO_RAD * degrees)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRotateAngleWithoutBeOffset(
        Axis axis,
        KineticBlockEntity be,
        BlockEntityRenderState state,
        @Nullable LevelAccessor level
    ) {
        return getRotateAngle(
            getProgress(be, level),
            KineticBlockEntityVisual.rotationOffset(state.blockState, axis, state.blockPos),
            axis
        );
    }

    /**
     * @deprecated {@link getDegreesWithoutBeOffset(Axis, KineticBlockEntity, BlockEntityRenderState, LevelAccessor)}
     * rotate(Axis, Mth.DEG_TO_RAD * degrees)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRotateAngleWithoutBeOffset(
        Axis axis,
        Direction direction,
        KineticBlockEntity be,
        BlockEntityRenderState state,
        @Nullable LevelAccessor level
    ) {
        return getRotateAngle(
            getProgress(be, level),
            KineticBlockEntityVisual.rotationOffset(state.blockState, axis, state.blockPos),
            direction
        );
    }

    public static float getProgress(KineticBlockEntity be, @Nullable LevelAccessor level) {
        return AnimationTickHolder.getRenderTime(level) * be.getSpeed() * 0.3f;
    }

    public static float getProgress(float speed, @Nullable LevelAccessor level) {
        return AnimationTickHolder.getRenderTime(level) * speed * 0.3f;
    }

    public static float getProgress(float speed, float time) {
        return time * speed * 0.3f;
    }

    /**
     * @deprecated {@link PoseStack#rotate(Axis, float)} rotate(Axis, Mth.DEG_TO_RAD * ((progress + offset) % 360))
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRotateAngle(float progress, float offset, Axis axis) {
        float angle = (progress + offset) % 360;
        if (angle == 0) {
            return null;
        }
        Direction direction = axis.getPositive();
        return new Quaternionf().setAngleAxis(
            Mth.DEG_TO_RAD * angle,
            direction.getStepX(),
            direction.getStepY(),
            direction.getStepZ()
        );
    }

    /**
     * @deprecated {@link PoseStack#rotate(Direction, float)} rotate(Direction, Mth.DEG_TO_RAD * ((progress + offset) % 360))
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRotateAngle(float progress, float offset, Direction direction) {
        float angle = (progress + offset) % 360;
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(
            Mth.DEG_TO_RAD * angle,
            direction.getStepX(),
            direction.getStepY(),
            direction.getStepZ()
        );
    }

    /**
     * @deprecated {@link PoseStack#rotate(Direction, float)} rotate(Direction, Mth.DEG_TO_RAD * float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRotateAngle(float angle, Direction direction) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(
            Mth.DEG_TO_RAD * angle,
            direction.getStepX(),
            direction.getStepY(),
            direction.getStepZ()
        );
    }

    /**
     * @deprecated {@link PoseStack#rotate(Direction, float)}
     */
    @Deprecated
    @Nullable
    public static Quaternionf getRadiansRotateAngle(float angle, Direction direction) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(angle, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.YP, Mth.DEG_TO_RAD * float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getUpRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(Mth.DEG_TO_RAD * angle, 0, 1, 0);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.ZP, Mth.DEG_TO_RAD * float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getSouthRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(Mth.DEG_TO_RAD * angle, 0, 0, 1);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.XP, Mth.DEG_TO_RAD * float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getEastRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(Mth.DEG_TO_RAD * angle, 1, 0, 0);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.XP, Mth.DEG_TO_RAD * -float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getWestRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(Mth.DEG_TO_RAD * angle, -1, 0, 0);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.YP, float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getUpRadiansRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(angle, 0, 1, 0);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.XP, float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getEastRadiansRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().setAngleAxis(angle, 1, 0, 0);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.XP, Mth.DEG_TO_RAD * float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getXRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().rotationX(Mth.DEG_TO_RAD * angle);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.YP, Mth.DEG_TO_RAD * float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getYRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().rotationY(Mth.DEG_TO_RAD * angle);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.ZP, Mth.DEG_TO_RAD * float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getZRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().rotationZ(Mth.DEG_TO_RAD * angle);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.YP, float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getYRadiansRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().rotationY(angle);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.XP, float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getXRadiansRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().rotationX(angle);
    }

    /**
     * @deprecated {@link PoseStack#rotate(com.mojang.math.Axis, float)} rotate(Axis.ZP, float)
     */
    @Deprecated
    @Nullable
    public static Quaternionf getZRadiansRotateAngle(float angle) {
        if (angle == 0) {
            return null;
        }
        return new Quaternionf().rotationZ(angle);
    }

    public static Color getColor(KineticBlockEntity be) {
        if (KineticDebugger.isActive()) {
            rainbowMode = true;
            return be.network != null ? Color.generateFromLong(be.network) : Color.WHITE;
        }
        float overStressedEffect = be.effects.overStressedEffect;
        if (overStressedEffect != 0) {
            boolean overstressed = overStressedEffect > 0;
            Color color = overstressed ? Color.RED : Color.SPRING_GREEN;
            float weight = overstressed ? overStressedEffect : -overStressedEffect;
            return Color.WHITE.mixWith(color, weight);
        }
        return Color.WHITE;
    }

    public static int getTintColor(KineticBlockEntity be) {
        if (KineticDebugger.isActive()) {
            rainbowMode = true;
            if (be.network == null) {
                return -1;
            }
            int timeStep = Hashing.crc32().hashLong(be.network).asInt();
            int localTimeStep = Math.abs(timeStep) % 1536;
            int timeStepInPhase = localTimeStep % 256;
            int phaseBlue = localTimeStep / 256;
            int red = Color.colorInPhase(phaseBlue + 4, timeStepInPhase);
            int green = Color.colorInPhase(phaseBlue + 2, timeStepInPhase);
            int blue = Color.colorInPhase(phaseBlue, timeStepInPhase);
            red = (int) (red + (255 - red) * 0.5f);
            green = (int) (green + (255 - green) * 0.5f);
            blue = (int) (blue + (255 - blue) * 0.5f);
            return 0xFF000000 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
        float overStressedEffect = be.effects.overStressedEffect;
        if (overStressedEffect == 0) {
            return -1;
        }
        if (overStressedEffect > 0) {
            int green = (int) (255 - 255 * overStressedEffect);
            int blue = (int) (255 - 255 * overStressedEffect);
            return 0xFFFF0000 | (green & 0xFF) << 8 | blue & 0xFF;
        }
        int red = (int) (255 - 255 * -overStressedEffect);
        int blue = (int) (255 - 68 * -overStressedEffect);
        return 0xFF00FF00 | (red & 0xFF) << 16 | blue & 0xFF;
    }

    public static float getRotationOffsetForPosition(KineticBlockEntity be, final BlockPos pos, final Axis axis) {
        return KineticBlockEntityVisual.rotationOffset(be.getBlockState(), axis, pos) + be.getRotationAngleOffset(axis);
    }

    public static BlockState shaft(Axis axis) {
        return AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, axis);
    }

    public static Axis getRotationAxisOf(KineticBlockEntity be) {
        return ((IRotate) be.getBlockState().getBlock()).getRotationAxis(be.getBlockState());
    }

    public static Axis getRotationAxisOf(BlockState state) {
        return ((IRotate) state.getBlock()).getRotationAxis(state);
    }

    public static class KineticRenderState extends BlockEntityRenderState {
        public boolean support;
        public @Nullable SuperByteBufferRenderState model;
        public float angle;
        public @UnknownNullability Axis axis;
        public @UnknownNullability Direction direction;
        public @UnknownNullability int color;
        public @Nullable CardinalLighting cardinalLighting;
    }
}
