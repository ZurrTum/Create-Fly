package com.zurrtum.create.client.content.contraptions.elevator;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyRenderer;
import com.zurrtum.create.client.content.contraptions.pulley.PulleyRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlock;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ElevatorPulleyRenderer extends KineticBlockEntityRenderer<ElevatorPulleyBlockEntity, ElevatorPulleyRenderer.ElevatorPulleyRenderState> {
    public ElevatorPulleyRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public ElevatorPulleyRenderState createRenderState() {
        return new ElevatorPulleyRenderState();
    }

    @Override
    public void updateRenderState(
        ElevatorPulleyBlockEntity be,
        ElevatorPulleyRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        World world = be.getWorld();
        BlockState blockState = be.getCachedState();
        float offset = PulleyRenderer.getBlockEntityOffset(tickProgress, be);
        boolean running = PulleyRenderer.isPulleyRunning(be);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * (180 + AngleHelper.horizontalAngle(blockState.get(ElevatorPulleyBlock.HORIZONTAL_FACING)));
        if (running || offset == 0) {
            state.magnet = CachedBuffers.partial(AllPartialModels.ELEVATOR_MAGNET, blockState);
            state.magnetOffset = -offset;
            state.magnetLight = WorldRenderer.getLightmapCoordinates(world, state.pos.down((int) offset));
        }
        state.rotatedCoil = getRotatedCoil(be);
        if (offset == 0) {
            return;
        }
        state.coilShift = AllSpriteShifts.ELEVATOR_COIL;
        state.coilScroll = AbstractPulleyRenderer.getCoilVScroll(state.coilShift, offset, 2);
        float f = offset % 1;
        if (f < .25f || f > .75f) {
            state.halfRope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT_HALF, blockState);
            updateHalfShift(state, offset);
            float down = f > .75f ? f - 1 : f;
            state.halfRopeOffset = -down;
            state.halfRopeLight = WorldRenderer.getLightmapCoordinates(world, state.pos.down((int) down));
        }
        if (!running) {
            return;
        }
        if (state.halfRope == null) {
            updateHalfShift(state, offset);
        }
        state.rope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT, blockState);
        int size = (int) Math.ceil(offset - .25f);
        float[] offsets = new float[size];
        int[] lights = new int[size];
        for (int i = 0; i < size; i++) {
            float down = offset - i;
            int light = WorldRenderer.getLightmapCoordinates(world, state.pos.down((int) down));
            offsets[i] = -down;
            lights[i] = light;
        }
        state.offsets = offsets;
        state.lights = lights;
    }

    @Override
    public void render(ElevatorPulleyRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    private static void updateHalfShift(ElevatorPulleyRenderState state, float offset) {
        state.halfShift = AllSpriteShifts.ELEVATOR_BELT;
        double beltScroll = (-(offset + .5) - Math.floor(-(offset + .5))) / 2;
        Sprite target = state.halfShift.getTarget();
        float spriteSize = target.getMaxV() - target.getMinV();
        state.halfScroll = (float) beltScroll * spriteSize;
    }

    @Override
    protected RenderLayer getRenderType(ElevatorPulleyBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected BlockState getRenderedBlockState(ElevatorPulleyBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    protected SuperByteBuffer getRotatedCoil(KineticBlockEntity be) {
        BlockState blockState = be.getCachedState();
        return CachedBuffers.partialFacing(AllPartialModels.ELEVATOR_COIL, blockState, blockState.get(ElevatorPulleyBlock.HORIZONTAL_FACING));
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    public static class ElevatorPulleyRenderState extends KineticRenderState {
        public float yRot;
        public SuperByteBuffer magnet;
        public float magnetOffset;
        public int magnetLight;
        public SuperByteBuffer rotatedCoil;
        public SpriteShiftEntry coilShift;
        public float coilScroll;
        public SuperByteBuffer halfRope;
        public SpriteShiftEntry halfShift;
        public float halfScroll;
        public float halfRopeOffset;
        public int halfRopeLight;
        public SuperByteBuffer rope;
        public float[] offsets;
        public int[] lights;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (model != null) {
                super.render(matricesEntry, vertexConsumer);
            }
            if (magnet != null) {
                magnet.center().rotateY(yRot).uncenter().translate(0, magnetOffset, 0).light(magnetLight).renderInto(matricesEntry, vertexConsumer);
            }
            if (coilScroll != 0) {
                rotatedCoil.shiftUVScrolling(coilShift, coilScroll);
            }
            rotatedCoil.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            if (halfRope != null) {
                halfRope.center().rotateY(yRot).uncenter().translate(0, halfRopeOffset, 0).shiftUVScrolling(halfShift, halfScroll)
                    .light(halfRopeLight).renderInto(matricesEntry, vertexConsumer);
            }
            if (rope != null) {
                for (int i = 0, size = offsets.length; i < size; i++) {
                    rope.center().rotateY(yRot).uncenter().translate(0, offsets[i], 0).shiftUVScrolling(halfShift, halfScroll).light(lights[i])
                        .renderInto(matricesEntry, vertexConsumer);
                }
            }
        }
    }
}
