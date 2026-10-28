package com.zurrtum.create.client.content.contraptions.pulley;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyRenderer.PulleyRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public abstract class AbstractPulleyRenderer<T extends KineticBlockEntity> implements BlockEntityRenderer<T, PulleyRenderState> {
    private final Int2ObjectMap<SuperByteBufferRenderState> cache = new Int2ObjectOpenHashMap<>();
    private final PartialModel halfRope;
    private final PartialModel halfMagnet;

    public AbstractPulleyRenderer(Context context, PartialModel halfRope, PartialModel halfMagnet) {
        this.halfRope = halfRope;
        this.halfMagnet = halfMagnet;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public PulleyRenderState createRenderState() {
        return new PulleyRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        PulleyRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getShaftAxis(be);
        Direction direction = axis.getPositive();
        int color = KineticBlockEntityRenderer.getTintColor(be);
        state.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(color).extractRenderState();
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        float offset = getOffset(be, tickProgress);
        boolean running = isRunning(be);
        SuperByteBuffer coil = CachedBuffers.partialFacing(getCoil(), state.blockState, direction);
        SpriteShiftEntry coilShift = getCoilShift();
        float coilScroll = getCoilVScroll(coilShift, offset, 1);
        if (coilScroll != 0) {
            coil.shiftUVScrolling(coilShift, coilScroll);
        }
        state.coil = coil.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        BlockPos blockPos = state.blockPos;
        if (running || offset == 0) {
            SuperByteBuffer magnet =
                offset > 0.25f ? renderMagnet(be) : CachedBuffers.partial(halfMagnet, state.blockState);
            int magnetLight = LightCoordsUtil.getLightCoords(level, blockPos.below((int) offset));
            state.magnet = magnet.cardinalLighting(cardinalLighting).light(magnetLight).extractRenderState();
            state.magnetOffset = -offset;
        }
        if (offset > 0.75f) {
            float f = offset % 1;
            if (f < 0.25f || f > 0.75f) {
                float down = f > 0.75f ? f - 1 : f;
                int halfRopeLight = LightCoordsUtil.getLightCoords(level, blockPos.below((int) down));
                state.halfRope = CachedBuffers.partial(halfRope, state.blockState).cardinalLighting(cardinalLighting)
                    .light(halfRopeLight).extractRenderState();
                state.halfRopeOffset = -down;
            }
        }
        if (!running || offset <= 1.25f) {
            return;
        }
        SuperByteBuffer rope = renderRope(be);
        int size = (int) Math.ceil(offset - 1.25f);
        SuperByteBufferRenderState[] ropes = new SuperByteBufferRenderState[size];
        state.ropeOffset = -offset;
        for (int i = 0, down = (int) offset - 1; i < size; i++, down--) {
            ropes[i] = cache.computeIfAbsent(
                LightCoordsUtil.getLightCoords(level, blockPos.below(down)),
                l -> rope.cardinalLighting(cardinalLighting).light(l).extractRenderState()
            );
        }
        cache.clear();
        state.ropes = ropes;
    }

    @Override
    public void submit(
        PulleyRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.shaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.shaft.submit(matrices, queue);
        }
        state.coil.submit(matrices, queue);
        if (state.magnet != null) {
            matrices.pushPose();
            matrices.translate(0, state.magnetOffset, 0);
            state.magnet.submit(matrices, queue);
            matrices.popPose();
        }
        if (state.halfRope != null) {
            matrices.pushPose();
            matrices.translate(0, state.halfRopeOffset, 0);
            state.halfRope.submit(matrices, queue);
            matrices.popPose();
        }
        if (state.ropes != null) {
            Pose pose = matrices.last();
            pose.translate(0, state.ropeOffset, 0);
            for (SuperByteBufferRenderState rope : state.ropes) {
                pose.translate(0, 1, 0);
                rope.submit(matrices, queue);
            }
        }
    }

    protected abstract Axis getShaftAxis(T be);

    protected abstract PartialModel getCoil();

    protected abstract SpriteShiftEntry getCoilShift();

    protected abstract SuperByteBuffer renderRope(T be);

    protected abstract SuperByteBuffer renderMagnet(T be);

    protected abstract float getOffset(T be, float partialTicks);

    protected abstract boolean isRunning(T be);

    public static float getCoilVScroll(SpriteShiftEntry coilShift, float offset, float speedModifier) {
        if (offset == 0) {
            return 0;
        }
        float spriteSize = coilShift.getTarget().getV1() - coilShift.getTarget().getV0();
        offset = offset * speedModifier / 2 + 0.1875f;
        double coilScroll = -offset - Math.floor(offset * -2) / 2;
        return (float) coilScroll * spriteSize;
    }

    @Override
    public int getViewDistance() {
        return AllConfigs.server().kinetics.maxRopeLength.get();
    }

    public static class PulleyRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @UnknownNullability Axis axis;
        public float angle;
        public @UnknownNullability SuperByteBufferRenderState coil;
        public @Nullable SuperByteBufferRenderState magnet;
        public float magnetOffset;
        public @Nullable SuperByteBufferRenderState halfRope;
        public float halfRopeOffset;
        public SuperByteBufferRenderState @Nullable [] ropes;
        public float ropeOffset;
    }
}
