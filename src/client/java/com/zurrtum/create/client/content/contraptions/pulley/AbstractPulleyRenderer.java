package com.zurrtum.create.client.content.contraptions.pulley;

import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPulleyRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T, AbstractPulleyRenderer.PulleyRenderState> {
    private final PartialModel halfRope;
    private final PartialModel halfMagnet;

    public AbstractPulleyRenderer(BlockEntityRendererFactory.Context context, PartialModel halfRope, PartialModel halfMagnet) {
        super(context);
        this.halfRope = halfRope;
        this.halfMagnet = halfMagnet;
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public PulleyRenderState createRenderState() {
        return new PulleyRenderState();
    }

    @Override
    public void updateRenderState(
        T be,
        PulleyRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            return;
        }
        float offset = getOffset(be, tickProgress);
        boolean running = isRunning(be);
        state.coil = getRotatedCoil(be);
        state.coilShift = getCoilShift();
        state.coilScroll = getCoilVScroll(state.coilShift, offset, 1);
        World world = be.getWorld();
        BlockState blockState = be.getCachedState();
        if (running || offset == 0) {
            state.magnet = offset > .25f ? renderMagnet(be) : CachedBuffers.partial(halfMagnet, blockState);
            state.magnetOffset = -offset;
            state.magnetLight = WorldRenderer.getLightmapCoordinates(world, state.pos.down((int) offset));
        }
        if (offset > .75f) {
            float f = offset % 1;
            if (f < .25f || f > .75f) {
                state.halfRope = CachedBuffers.partial(halfRope, blockState);
                float down = f > .75f ? f - 1 : f;
                state.halfRopeOffset = -down;
                state.halfRopeLight = WorldRenderer.getLightmapCoordinates(world, state.pos.down((int) down));
            }
        }
        if (!running || offset <= 1.25f) {
            return;
        }
        state.rope = renderRope(be);
        int size = (int) Math.ceil(offset - 1.25f);
        float[] offsets = new float[size];
        int[] lights = new int[size];
        for (int i = 0; i < size; i++) {
            float down = offset - i - 1;
            int light = WorldRenderer.getLightmapCoordinates(world, state.pos.down((int) down));
            offsets[i] = -down;
            lights[i] = light;
        }
        state.offsets = offsets;
        state.lights = lights;
    }

    @Override
    protected RenderLayer getRenderType(T be, BlockState state) {
        return RenderLayer.getSolid();
    }

    protected abstract Axis getShaftAxis(T be);

    protected abstract PartialModel getCoil();

    protected abstract SpriteShiftEntry getCoilShift();

    protected abstract SuperByteBuffer renderRope(T be);

    protected abstract SuperByteBuffer renderMagnet(T be);

    protected abstract float getOffset(T be, float partialTicks);

    protected abstract boolean isRunning(T be);

    @Override
    protected BlockState getRenderedBlockState(T be) {
        return shaft(getShaftAxis(be));
    }

    protected SuperByteBuffer getRotatedCoil(T be) {
        BlockState blockState = be.getCachedState();
        return CachedBuffers.partialFacing(getCoil(), blockState, Direction.get(AxisDirection.POSITIVE, getShaftAxis(be)));
    }

    public static float getCoilVScroll(SpriteShiftEntry coilShift, float offset, float speedModifier) {
        if (offset == 0) {
            return 0;
        }
        float spriteSize = coilShift.getTarget().getMaxV() - coilShift.getTarget().getMinV();
        offset *= speedModifier / 2;
        double coilScroll = -(offset + 3 / 16f) - Math.floor((offset + 3 / 16f) * -2) / 2;
        return (float) coilScroll * spriteSize;
    }

    @Override
    public int getRenderDistance() {
        return AllConfigs.server().kinetics.maxRopeLength.get();
    }

    public static class PulleyRenderState extends KineticRenderState {
        public SuperByteBuffer coil;
        public SpriteShiftEntry coilShift;
        public float coilScroll;
        public SuperByteBuffer magnet;
        public float magnetOffset;
        public int magnetLight;
        public SuperByteBuffer halfRope;
        public float halfRopeOffset;
        public int halfRopeLight;
        public SuperByteBuffer rope;
        public float[] offsets;
        public int[] lights;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            if (coilScroll != 0) {
                coil.shiftUVScrolling(coilShift, coilScroll);
            }
            coil.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            if (magnet != null) {
                magnet.translate(0, magnetOffset, 0).light(magnetLight).renderInto(matricesEntry, vertexConsumer);
            }
            if (halfRope != null) {
                halfRope.translate(0, halfRopeOffset, 0).light(halfRopeLight).renderInto(matricesEntry, vertexConsumer);
            }
            if (rope != null) {
                for (int i = 0, size = offsets.length; i < size; i++) {
                    rope.translate(0, offsets[i], 0).light(lights[i]).renderInto(matricesEntry, vertexConsumer);
                }
            }
        }
    }
}
