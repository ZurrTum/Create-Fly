package com.zurrtum.create.client.content.kinetics.base;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class KineticBlockEntityRenderer<T extends KineticBlockEntity, S extends KineticBlockEntityRenderer.KineticRenderState> implements BlockEntityRenderer<T, S> {

    public static final SuperByteBufferCache.Compartment<BlockState> KINETIC_BLOCK = new SuperByteBufferCache.Compartment<>();
    public static boolean rainbowMode = false;

    public KineticBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new KineticRenderState();
    }

    @Override
    public void updateRenderState(
        T be,
        S state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        World world = be.getWorld();
        state.support = VisualizationManager.supportsVisualization(world);
        if (state.support) {
            return;
        }
        state.pos = be.getPos();
        state.blockState = getRenderedBlockState(be);
        state.type = be.getType();
        state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
            world,
            state.pos
        ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        state.crumblingOverlay = crumblingOverlay;
        state.layer = getRenderType(be, state.blockState);
        state.axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.direction = Direction.from(state.axis, AxisDirection.POSITIVE);
        state.model = getRotatedModel(be, state);
        state.angle = getAngleForBe(be, state.pos, state.axis);
        state.color = getColor(be);
    }

    @Override
    public void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.support) {
            return;
        }
        queue.submitCustom(matrices, state.layer, state);
    }

    protected BlockState getRenderedBlockState(T be) {
        return be.getCachedState();
    }

    protected RenderLayer getRenderType(T be, BlockState state) {
        return RenderLayers.getMovingBlockLayer(state);
    }

    protected SuperByteBuffer getRotatedModel(T be, S state) {
        return CachedBuffers.block(KINETIC_BLOCK, state.blockState);
    }

    public static void renderRotatingKineticBlock(KineticBlockEntity be, BlockState renderedState, MatrixStack ms, VertexConsumer buffer, int light) {
        SuperByteBuffer superByteBuffer = CachedBuffers.block(KINETIC_BLOCK, renderedState);
        renderRotatingBuffer(be, superByteBuffer, ms, buffer, light);
    }

    public static void renderRotatingBuffer(KineticBlockEntity be, SuperByteBuffer superBuffer, MatrixStack ms, VertexConsumer buffer, int light) {
        standardKineticRotationTransform(superBuffer, be, light).renderInto(ms.peek(), buffer);
    }

    public static float getAngleForBe(KineticBlockEntity be, final BlockPos pos, Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
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
            if (overStressedEffect != 0)
                if (overStressedEffect > 0)
                    buffer.color(Color.WHITE.mixWith(Color.RED, overStressedEffect));
                else
                    buffer.color(Color.WHITE.mixWith(Color.SPRING_GREEN, -overStressedEffect));
            else
                buffer.color(Color.WHITE);
        }

        return buffer;
    }

    public static Color getColor(KineticBlockEntity be) {
        if (KineticDebugger.isActive()) {
            rainbowMode = true;
            return be.network != null ? Color.generateFromLong(be.network) : Color.WHITE;
        } else {
            float overStressedEffect = be.effects.overStressedEffect;
            if (overStressedEffect == 0) {
                return Color.WHITE;
            }
            if (overStressedEffect > 0) {
                return Color.WHITE.mixWith(Color.RED, overStressedEffect);
            }
            return Color.WHITE.mixWith(Color.SPRING_GREEN, -overStressedEffect);
        }
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

    public static class KineticRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public boolean support;
        public RenderLayer layer;
        public SuperByteBuffer model;
        public float angle;
        public Axis axis;
        public Direction direction;
        public Color color;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.light(lightmapCoordinates);
            model.rotateCentered(angle, direction);
            model.color(color);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
