package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class KineticBlockEntityRenderer<T extends KineticBlockEntity> extends SafeBlockEntityRenderer<T> {

    public static final SuperByteBufferCache.Compartment<BlockState> KINETIC_BLOCK = new SuperByteBufferCache.Compartment<>();
    public static boolean rainbowMode = false;

    public KineticBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(T be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState state = getRenderedBlockState(be);
        RenderLayer type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
    }

    protected BlockState getRenderedBlockState(T be) {
        return be.getCachedState();
    }

    protected RenderLayer getRenderType(T be, BlockState state) {
        return RenderLayers.getMovingBlockLayer(state);
    }

    protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
        return CachedBuffers.block(KINETIC_BLOCK, state);
    }

    public static void renderRotatingKineticBlock(KineticBlockEntity be, BlockState renderedState, MatrixStack ms, VertexConsumer buffer, int light) {
        SuperByteBuffer superByteBuffer = CachedBuffers.block(KINETIC_BLOCK, renderedState);
        renderRotatingBuffer(be, superByteBuffer, ms, buffer, light);
    }

    public static void renderRotatingBuffer(KineticBlockEntity be, SuperByteBuffer superBuffer, MatrixStack ms, VertexConsumer buffer, int light) {
        standardKineticRotationTransform(superBuffer, be, light).renderInto(ms, buffer);
    }

    public static float getAngleForBe(KineticBlockEntity be, final BlockPos pos, Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        float angle = ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
        return angle;
    }

    public static SuperByteBuffer standardKineticRotationTransform(SuperByteBuffer buffer, KineticBlockEntity be, int light) {
        final BlockPos pos = be.getPos();
        Axis axis = ((IRotate) be.getCachedState().getBlock()).getRotationAxis(be.getCachedState());
        return kineticRotationTransform(buffer, be, axis, getAngleForBe(be, pos, axis), light);
    }

    public static SuperByteBuffer kineticRotationTransform(SuperByteBuffer buffer, KineticBlockEntity be, Axis axis, float angle, int light) {
        buffer.light(light);
        buffer.rotateCentered(angle, Direction.get(AxisDirection.POSITIVE, axis));

        if (KineticDebugger.isActive()) {
            rainbowMode = true;
            buffer.color(be.hasNetwork() ? Color.generateFromLong(be.network) : Color.WHITE);
        } else {
            float overStressedEffect = be.effects.overStressedEffect;
            if (overStressedEffect != 0) {
                boolean overstressed = overStressedEffect > 0;
                Color color = overstressed ? Color.RED : Color.SPRING_GREEN;
                float weight = overstressed ? overStressedEffect : -overStressedEffect;

                buffer.color(Color.WHITE.mixWith(color, weight));
            } else {
                buffer.color(Color.WHITE);
            }
        }

        return buffer;
    }

    public static float getRotationOffsetForPosition(KineticBlockEntity be, final BlockPos pos, final Axis axis) {
        return KineticBlockEntityVisual.rotationOffset(be.getCachedState(), axis, pos) + be.getRotationAngleOffset(axis);
    }

    public static BlockState shaft(Axis axis) {
        return AllBlocks.SHAFT.getDefaultState().with(Properties.AXIS, axis);
    }

    public static Axis getRotationAxisOf(KineticBlockEntity be) {
        return ((IRotate) be.getCachedState().getBlock()).getRotationAxis(be.getCachedState());
    }

}
