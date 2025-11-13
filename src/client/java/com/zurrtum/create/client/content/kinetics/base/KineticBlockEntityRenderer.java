package com.zurrtum.create.client.content.kinetics.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class KineticBlockEntityRenderer<T extends KineticBlockEntity, S extends KineticBlockEntityRenderer.KineticRenderState> implements BlockEntityRenderer<T, S> {

    public static final SuperByteBufferCache.Compartment<BlockState> KINETIC_BLOCK = new SuperByteBufferCache.Compartment<>();
    public static boolean rainbowMode = false;

    public KineticBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        state.support = VisualizationManager.supportsVisualization(world);
        if (state.support) {
            return;
        }
        updateBaseRenderState(be, state, world, crumblingOverlay);
        state.model = getRotatedModel(be, state);
        state.angle = getAngleForBe(be, state.blockPos, state.axis);
    }

    public void updateBaseRenderState(T be, S state, Level world, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        state.blockPos = be.getBlockPos();
        state.blockState = getRenderedBlockState(be);
        state.blockEntityType = be.getType();
        state.lightCoords = world != null ? LevelRenderer.getLightColor(world, state.blockPos) : LightTexture.FULL_BRIGHT;
        state.breakProgress = crumblingOverlay;
        state.layer = getRenderType(be, state.blockState);
        state.axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.direction = Direction.fromAxisAndDirection(state.axis, AxisDirection.POSITIVE);
        state.color = getColor(be);
    }

    @Override
    public void submit(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.support) {
            return;
        }
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    protected BlockState getRenderedBlockState(T be) {
        return be.getBlockState();
    }

    protected RenderType getRenderType(T be, BlockState state) {
        return ItemBlockRenderTypes.getMovingBlockRenderType(state);
    }

    protected SuperByteBuffer getRotatedModel(T be, S state) {
        return CachedBuffers.block(KINETIC_BLOCK, state.blockState);
    }

    public static float getAngleForBe(KineticBlockEntity be, final BlockPos pos, Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
    }

    public static Color getColor(KineticBlockEntity be) {
        if (KineticDebugger.isActive()) {
            rainbowMode = true;
            return be.network != null ? Color.generateFromLong(be.network) : Color.WHITE;
        } else {
            float overStressedEffect = be.effects.overStressedEffect;
            if (overStressedEffect != 0) {
                boolean overstressed = overStressedEffect > 0;
                Color color = overstressed ? Color.RED : Color.SPRING_GREEN;
                float weight = overstressed ? overStressedEffect : -overStressedEffect;
                return Color.WHITE.mixWith(color, weight);
            } else {
                return Color.WHITE;
            }
        }
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

    public static class KineticRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public boolean support;
        public RenderType layer;
        public SuperByteBuffer model;
        public float angle;
        public Axis axis;
        public Direction direction;
        public Color color;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.light(lightCoords);
            model.rotateCentered(angle, direction);
            model.color(color);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
